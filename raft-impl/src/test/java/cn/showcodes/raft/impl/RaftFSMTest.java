package cn.showcodes.raft.impl;

import cn.showcodes.raft.*;
import cn.showcodes.raft.impl.frame.RaftFrameServiceImpl;
import cn.showcodes.raft.impl.frame.RaftFrameVoteRequest;
import cn.showcodes.raft.impl.frame.RaftFrameVoteResponse;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class RaftFSMTest {

    RaftFSM testFSM() {
        RaftFSM raftFSM = new RaftFSM();
        RaftConfig raftConfig = new RaftConfig();
        raftConfig.nodeId = "nodeA";
        raftFSM.raftConfig = raftConfig;
        raftConfig.peers = Arrays.asList(
                new CommunicationNode("nodeB", 101),
                new CommunicationNode("nodeC", 102)
        );
        RaftRequestSupplier supplier = new RaftRequestInMemorySupplier();
        raftFSM.requestSupplier = supplier;
        raftFSM.raftFrameService = new RaftFrameServiceImpl();
        raftFSM.communicationService = new FakeCommunicationService();
        raftFSM.raftLogService = new RaftLogInMemoryServiceImpl();
        return raftFSM;
    }

    @Test
    public void testElectionTimeout() throws InterruptedException {
        RaftFSM raftFSM = testFSM();
        Assert.assertEquals(RaftRole.follower, raftFSM.role);

        CountDownLatch countDownLatch = new CountDownLatch(2);

        raftFSM.addHandler((request, context) -> {
            if (request.getFrame().getType() == RaftFrameType.roleChange) {
                RaftFrame raftFrame = request.getFrame();
                if (countDownLatch.getCount() == 2) {
                    Assert.assertEquals(RaftRole.follower, raftFrame.getAttributes().get("role"));
                } else {
                    Assert.assertEquals(RaftRole.candidate, raftFrame.getAttributes().get("role"));
                }
                countDownLatch.countDown();
            }
        });

        raftFSM.start();
        countDownLatch.await();
        Assert.assertEquals(RaftRole.candidate, raftFSM.role);
    }

    @Test
    public void testRequestVote() throws InterruptedException {
        RaftFSM raftFSM = testFSM();
        CountDownLatch countDownLatch = new CountDownLatch(raftFSM.raftConfig.peers.size());

        raftFSM.start();

        FakeCommunicationService fakeCommunicationService = (FakeCommunicationService)raftFSM.communicationService;

        for(int i = 0; i < raftFSM.raftConfig.peers.size(); i++) {
            RaftProtocol protocol = fakeCommunicationService.outgoing.take();
            RaftFrameVoteRequest voteRequest = raftFSM.raftFrameService.voteRequest((RaftFrame) protocol);
            Assert.assertEquals(raftFSM.raftConfig.nodeId, voteRequest.getCandidateId());
            countDownLatch.countDown();
        }
        countDownLatch.await();
    }

    @Test
    public void testLeaderWin() throws InterruptedException {
        RaftFSM raftFSM = testFSM();
        RaftRequestSupplier supplier = raftFSM.requestSupplier;
        CountDownLatch countDownLatch = new CountDownLatch(3);

        FakeCommunicationService fakeCommunicationService = (FakeCommunicationService)raftFSM.communicationService;

        raftFSM.addHandler((request, context) -> {
            if (request.getFrame().getType() == RaftFrameType.roleChange) {
                RaftFrame frame = request.getFrame();
                if (frame.getAttributes().get("role") == RaftRole.leader) {
                    countDownLatch.countDown();
                }
            }
        });

        raftFSM.start();

        for(int i = 0; i < raftFSM.raftConfig.peers.size(); i++) {
            RaftProtocol protocol = fakeCommunicationService.outgoing.take();
            RaftFrameVoteRequest voteRequest = raftFSM.raftFrameService.voteRequest((RaftFrame) protocol);
            CommunicationNode node = raftFSM.raftConfig.peers.get(i);
            RaftFrameVoteResponse voteResponse = new RaftFrameVoteResponse();
            voteResponse.setTerm(voteRequest.getTerm());
            voteResponse.setVoteGranted(voteRequest.getCandidateId());
            voteResponse.setSuccess(true);
            voteResponse.setFollowerId(node.toString());

            RaftRequest voteResponseRequest = new RaftRequest();
            voteResponseRequest.setCommunicationNode(node);
            voteResponseRequest.setFrame(raftFSM.raftFrameService.from(voteResponse));
            supplier.append(voteResponseRequest);
        }

        for( ; ;) {
            RaftFrame frame = (RaftFrame) fakeCommunicationService.outgoing.take();
            if (frame.getType() == RaftFrameType.appendEntriesRequest) {
                countDownLatch.countDown();
                if (countDownLatch.getCount() == 0 ) {
                    break;
                }
            }
        }
        countDownLatch.await();

    }
}
