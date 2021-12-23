package cn.showcodes.raft.impl;

import cn.showcodes.raft.*;
import cn.showcodes.raft.impl.frame.*;
import cn.showcodes.state.FiniteStateMachine;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class RaftFSM implements FiniteStateMachine<RaftRequest> {

    protected CommunicationService communicationService;

    ReadWriteLock stateLock = new ReentrantReadWriteLock();

    RaftRole role = RaftRole.follower;
    long currentTerm = 0;

    long lastLogIndex = 0;
    long lastApplied = 0;

    long lastVoteTerm = 0;
    String votedFor;
    String leaderId;

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    ScheduledFuture electionTimeoutFuture;

    Map<CommunicationNode, FollowerRef> followers;
    RaftFrameService raftFrameService;
    RaftLogService raftLogService;

    RaftConfig raftConfig;
    Voter voter;
    ExecutorService leadingService;
    Map<CommunicationNode, Semaphore> appendingSemaphoreMap = new ConcurrentHashMap<>();
    ConcurrentLinkedQueue<RaftHandler> handlers = new ConcurrentLinkedQueue<>();
    RaftRequestSupplier requestSupplier;
    AtomicBoolean running = new AtomicBoolean();
    ExecutorService requestExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    public RaftFSM() {
        addHandler(new DefaultRaftHandler(this));
    }

    public void start() {
        start(this.raftConfig);
    }

    public void start(RaftConfig raftConfig) {
        this.raftConfig = raftConfig;
        RaftRequest request = new RaftRequest();
        request.setCommunicationNode(null);

        RaftFrame raftFrame = new RaftFrame();
        raftFrame.setType(RaftFrameType.roleChange);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("role", RaftRole.follower);
        BecomeFollower becomeFollower = new BecomeFollower();
        becomeFollower.setCurrentTerm(0);
        becomeFollower.setElectionTimeout(500);
        attributes.put("roleData", becomeFollower);
        raftFrame.setAttributes(attributes);
        request.setFrame(raftFrame);

        requestSupplier.append(request);

        running.set(true);
        requestExecutor.execute(this::run);
    }

    void run() {
        if (running.get() == true) {
            try {
                RaftRequest request = requestSupplier.take();
                transmit(request);
            } catch (InterruptedException e) {
                log.error("interrupted {}", e.getMessage());
            } finally {
                requestExecutor.execute(this::run);
            }
        } else {
            log.warn("not running now");
        }
    }

    public boolean addHandler(RaftHandler handler) {
        return handlers.contains(handler) ? false : handlers.add(handler);
    }

    public boolean removeHandler(RaftHandler handler) {
        return handlers.remove(handler);
    }

    public final void transmit(RaftRequest request) {
        RaftHandleContext handleContext = this.handlerContext();
        handleContext.request = request;
        handlers.forEach(handler -> {
            handler.handle(request, handleContext);
        });
    }

    RaftHandleContext handlerContext() {
        RaftHandleContext handleContext = new RaftHandleContext();
        handleContext.communicationService = communicationService;
        handleContext.requestSupplier = requestSupplier;
        return handleContext;
    }

    void handleBecomeFollower(final BecomeFollower becomeFollower, RaftHandleContext handleContext) {
        stateLock.writeLock().lock();

        switch (role) {
            case leader:
                leadingService.shutdownNow();
                leadingService = null;
                break;
            case candidate:
                voter.stop();
                voter = null;
                break;
        }

        role = RaftRole.follower;
        currentTerm = becomeFollower.getCurrentTerm();
        votedFor = null;
        leaderId = becomeFollower.getLeaderId();
        electionTimeout(becomeFollower.getCurrentTerm() + 1, becomeFollower.getElectionTimeout(), handleContext);
        stateLock.writeLock().unlock();

        log.info("become follower");
    }

    void electionTimeout(long term, long timeout, RaftHandleContext handleContext) {
        if (electionTimeoutFuture != null) {
            electionTimeoutFuture.cancel(true);
        }
        electionTimeoutFuture = scheduledThreadPoolExecutor.schedule(() -> {
            log.info("node={} election timeout", raftConfig.nodeId);
            BecomeCandidate candidate = new BecomeCandidate();
            candidate.setTerm(term);
            RaftFrame raftFrame = new RaftFrame();
            raftFrame.setType(RaftFrameType.roleChange);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("role", RaftRole.candidate);
            attributes.put("roleData", candidate);
            raftFrame.setAttributes(attributes);

            handleContext.fireRequest(raftFrame);
        }, timeout, TimeUnit.MILLISECONDS);
    }

    long randomElectionTimeout() {
        return Math.round(Math.random() * (raftConfig.electionMaxTime - raftConfig.electionMinTime))  + raftConfig.electionMinTime;
    }

    void leading() {
        for(CommunicationNode node : raftConfig.peers) {
            leadingService.execute(() -> this.sync(node));
        }
    }

    void sync(CommunicationNode node) {
        FollowerRef ref = followers.computeIfAbsent(node, k -> {
            FollowerRef followerRef = new FollowerRef();
            followerRef.communicationNode = node;
            followerRef.createTime = System.currentTimeMillis();
            RaftLog last = raftLogService.lastAvailableItem();
            followerRef.nextIndex = last.getIndex();
            return followerRef;
        });

        RaftFrameAppendEntriesRequest request = new RaftFrameAppendEntriesRequest();
        request.setLeaderId(raftConfig.nodeId);
        request.setTerm(currentTerm);
        request.setCommitIndex(raftLogService.getCommitIndex());
        RaftLog prev = raftLogService.get(ref.nextIndex - 1);
        if (prev != null) {
            request.setPrevLogIndex(prev.getIndex());
            request.setPrevLogTerm(prev.getTerm());
        }
        RaftLog[] entries = raftLogService.fetch(ref.nextIndex, raftConfig.batchLogCount);
        request.setEntries(entries);
        Semaphore semaphore = appendingSemaphoreMap.computeIfAbsent(node, k -> new Semaphore(0));
        communicationService.request(node, raftFrameService.from(request));
        try {
            semaphore.acquire();
            leadingService.execute(() -> this.sync(node));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class Voter {
        long term;
        ScheduledFuture timeoutFuture;
        AtomicInteger successCount = new AtomicInteger(0);

        Voter(long term) {
            this.term = term;
        }

        void start() {
            timeoutFuture = scheduledThreadPoolExecutor.schedule(this::timeout, raftConfig.voteTimeout, TimeUnit.MILLISECONDS);
        }

        void stop() {
            timeoutFuture.cancel(true);
        }

        void apply(CommunicationNode node, RaftHandleContext handleContext) {
            if (successCount.incrementAndGet() >= (raftConfig.peers.size() / 2)) {
                timeoutFuture.cancel(true);
                BecomeLeader becomeLeader = new BecomeLeader();
                becomeLeader.setTerm(term);

                RaftFrame frame = new RaftFrame();
                frame.setType(RaftFrameType.roleChange);
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("role", RaftRole.leader);
                attributes.put("roleData", becomeLeader);
                frame.setAttributes(attributes);
                handleContext.fireRequest(frame);
                log.info("receive accept response from {}", node);
            } else {
                log.debug("wait for more followers since now={}/total={}", successCount.get(), raftConfig.peers.size());
            }
        }

        void timeout() {
            BecomeCandidate becomeCandidate = new BecomeCandidate();
            becomeCandidate.setTerm(term + 1);
            RaftFrame frame = new RaftFrame();
            frame.setType(RaftFrameType.roleChange);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("role", RaftRole.candidate);
            attributes.put("roleData", becomeCandidate);
            frame.setAttributes(attributes);

            RaftRequest request = new RaftRequest();
            request.setFrame(frame);
            requestSupplier.append(request);
        }
    }

    void handleBecomeCandidate(BecomeCandidate becomeCandidate, RaftHandleContext handleContext) {
        stateLock.writeLock().lock();
        role = RaftRole.candidate;
        currentTerm = becomeCandidate.getTerm();
        votedFor = raftConfig.nodeId;
        leaderId = null;
        if (followers != null) {
            followers.clear();
        } else {
            followers = new ConcurrentHashMap<>();
        }
        voter = new Voter(currentTerm);
        RaftFrameVoteRequest voteRequest = new RaftFrameVoteRequest();
        voteRequest.setCandidateId(votedFor);
        voteRequest.setTerm(currentTerm);
        RaftLog lastCommit = raftLogService.lastCommitItem();
        if (lastCommit != null) {
            voteRequest.setLastLogIndex(lastCommit.getIndex());
            voteRequest.setLastLogTerm(lastCommit.getTerm());
        } else {
            voteRequest.setLastLogTerm(0);
            voteRequest.setLastLogIndex(0);
        }


        stateLock.writeLock().unlock();
        RaftFrame data = raftFrameService.from(voteRequest);
        raftConfig.peers.forEach(node -> {
            handleContext.sendRequest(node, data);
        });
        voter.start();
    }

    void handleBecomeLeader(BecomeLeader becomeLeader, RaftHandleContext handleContext) {
        stateLock.writeLock().lock();
        currentTerm = becomeLeader.getTerm();
        voter = null;
        votedFor = null;
        leaderId = raftConfig.nodeId;
        lastLogIndex = raftLogService.getCommitIndex();
        appendingSemaphoreMap.clear();
        leadingService = new ThreadPoolExecutor(
                1,
                raftConfig.peers.size() - 1,
                1,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        stateLock.writeLock().unlock();

        raftLogService.append(RaftLogType.leader, currentTerm, null);
        this.leading();
    }

    void handleVoteRequest(CommunicationNode node, RaftFrameVoteRequest request, RaftHandleContext handleContext) {
        if (request.getTerm() == lastVoteTerm && !request.getCandidateId().equals(votedFor)) {
            log.info("has vote to {} in term={}, ignore {}", votedFor, lastVoteTerm, request.getCandidateId());
            return;
        }
        RaftLog lastCommit = raftLogService.lastCommitItem();
        if (lastCommit == null) {
            lastCommit = new RaftLog();
            lastCommit.setTerm(0);
            lastCommit.setIndex(0);
        }
        RaftFrameVoteResponse response = new RaftFrameVoteResponse();
        if (request.getTerm() > currentTerm && request.getLastLogIndex() >= lastCommit.getIndex() && request.getLastLogTerm() >= lastCommit.getTerm()) {

            BecomeFollower becomeFollower = new BecomeFollower();
            becomeFollower.setLeaderId(request.getCandidateId());
            becomeFollower.setCurrentTerm(request.getTerm());

            RaftFrame frame = new RaftFrame();
            frame.setType(RaftFrameType.roleChange);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("role", RaftRole.follower);
            attributes.put("roleData", becomeFollower);
            frame.setAttributes(attributes);
            handleContext.fireRequest(frame);

            lastVoteTerm = request.getTerm();
            votedFor = request.getCandidateId();
            response.setSuccess(true);
            response.setFollowerId(raftConfig.nodeId);
            response.setTerm(currentTerm);
            response.setVoteGranted(request.getCandidateId());
        } else {
            response.setSuccess(false);
            response.setFollowerId(raftConfig.nodeId);
            response.setTerm(currentTerm);
            if (votedFor != null) {
                response.setVoteGranted(votedFor);
            }
        }

        handleContext.sendResponse(raftFrameService.from(response));
    }

    void handleVoteResponse(CommunicationNode node, RaftFrameVoteResponse response, RaftHandleContext handleContext) {

        switch (role) {
            case candidate:
            {
                if (response.isSuccess() && (response.getTerm() == currentTerm)) {
                    this.voter.apply(node,  handleContext);
                } else {
                   if (response.getTerm() >= currentTerm && response.getVoteGranted() !=null
                           && !response.getVoteGranted().isEmpty()
                           && !response.getVoteGranted().equals(raftConfig.nodeId)) {
                        BecomeFollower becomeFollower = new BecomeFollower();
                        becomeFollower.setCurrentTerm(response.getTerm());
                        becomeFollower.setElectionTimeout(randomElectionTimeout());
                        becomeFollower.setLeaderId(response.getVoteGranted());

                        RaftFrame frame = new RaftFrame();
                        frame.setType(RaftFrameType.roleChange);
                        Map<String, Object> attributes = new HashMap<>();
                        attributes.put("role", RaftRole.follower);
                        attributes.put("roleData", becomeFollower);
                        frame.setAttributes(attributes);

                        handleContext.fireRequest(frame);
                   }
                }
                return;
            }
            case leader:
            case follower:
            {
                log.info("ignore vote response");
            }
            break;
        }

    }

    void handleAppendEntriesRequest(CommunicationNode node, RaftFrameAppendEntriesRequest request, RaftHandleContext handleContext) {
        if (request.getTerm() > currentTerm) {
            BecomeFollower becomeFollower = new BecomeFollower();
            becomeFollower.setLeaderId(request.getLeaderId());
            becomeFollower.setCurrentTerm(request.getTerm());
            becomeFollower.setElectionTimeout(randomElectionTimeout());

            RaftFrame raftFrame = new RaftFrame();
            raftFrame.setType(RaftFrameType.roleChange);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("role", RaftRole.follower);
            attributes.put("roleData", becomeFollower);
            raftFrame.setAttributes(attributes);

            handleContext.fireRequest(raftFrame);

            RaftFrameAppendEntriesResponse response = new RaftFrameAppendEntriesResponse();
            response.setSuccess(false);
            response.setTerm(currentTerm);
            handleContext.sendResponse(raftFrameService.from(response));

        } else if (request.getTerm() < currentTerm) {
            RaftFrameAppendEntriesResponse response = new RaftFrameAppendEntriesResponse();
            response.setSuccess(false);
            response.setTerm(currentTerm);
            handleContext.sendResponse(raftFrameService.from(response));
        } else {

            if (request.getPrevLogTerm() > 0) {
                RaftLog previous = raftLogService.get(request.getPrevLogIndex());
                // 当前节点不存在服务器发过来的commit位置，需要找更早的数据
                if (previous == null) {
                    RaftFrameAppendEntriesResponse response = new RaftFrameAppendEntriesResponse();
                    response.setSuccess(false);
                    response.setTerm(currentTerm);
                    handleContext.sendResponse(raftFrameService.from(response));
                    return;
                } else {
                    // 数据冲突
                    if (previous.getTerm() != request.getTerm()) {
                        RaftFrameAppendEntriesResponse response = new RaftFrameAppendEntriesResponse();
                        response.setSuccess(false);
                        response.setTerm(currentTerm);
                        handleContext.sendResponse(raftFrameService.from(response));
                        raftLogService.removeTo(previous);
                        return;
                    }

                    raftLogService.removeTo(previous);
                }
            }


            RaftLog[] items = request.getEntries();
            RaftLog last = null;
            if (items != null && items.length > 0) {
                for(RaftLog item : items) {
                    last = raftLogService.append(item);
                }
            }
            raftLogService.commitTo(request.getCommitIndex());
            RaftFrameAppendEntriesResponse response = new RaftFrameAppendEntriesResponse();
            response.setTerm(currentTerm);
            response.setSuccess(true);
            response.setLastIndex(last.getIndex());
            handleContext.sendResponse(raftFrameService.from(response));
        }

    }

    void handleAppendEntriesResponse(CommunicationNode node, RaftFrameAppendEntriesResponse response) {
        if (role != RaftRole.leader) {
            log.warn("{} is not leader", raftConfig.nodeId);
            return;
        }

        Semaphore semaphore = appendingSemaphoreMap.get(node);
        if (semaphore != null) {
            if (semaphore.availablePermits() == 0) {
                semaphore.release();
            } else {
                log.warn("semaphore great than 0");
            }
        } else {
            log.warn("ignore request");
        }

        FollowerRef followerRef = followers.computeIfAbsent(node, (k) -> {
            FollowerRef ref = new FollowerRef();
            ref.communicationNode = node;
            ref.createTime = System.currentTimeMillis();
            ref.nextIndex = raftLogService.lastAvailableItem().getIndex();
            return ref;
        });

        synchronized (followerRef) {
            followerRef.lastAlive = System.currentTimeMillis();

            if (response.isSuccess()) {
                followerRef.nextIndex = response.getLastIndex() + 1;
                log.warn("follower {} accept append nextIndex = {}", followerRef.communicationNode, followerRef.nextIndex);
            } else {
                followerRef.nextIndex -= 1;
                log.warn("follower {} deny append request nextIndex = {}", followerRef.communicationNode, followerRef.nextIndex);
            }
        }

    }
}
