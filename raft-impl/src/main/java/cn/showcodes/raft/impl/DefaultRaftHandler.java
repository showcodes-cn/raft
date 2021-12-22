package cn.showcodes.raft.impl;

import cn.showcodes.raft.RaftRequest;
import cn.showcodes.raft.RaftRole;
import cn.showcodes.raft.impl.frame.BecomeCandidate;
import cn.showcodes.raft.impl.frame.BecomeFollower;
import cn.showcodes.raft.impl.frame.BecomeLeader;

public class DefaultRaftHandler implements RaftHandler{
    RaftFSM raftFSM;

    DefaultRaftHandler(RaftFSM raftFSM) {
        this.raftFSM = raftFSM;
    }

    @Override
    public void handle(RaftRequest transition, RaftHandleContext context) {
        RaftFrameService raftFrameService = raftFSM.raftFrameService;
        switch (transition.getFrame().getType()) {
            case voteRequest:
                raftFSM.handleVoteRequest(transition.getCommunicationNode(), raftFrameService.voteRequest(transition.getFrame()), context);
                break;
            case voteResponse:
                raftFSM.handleVoteResponse(transition.getCommunicationNode(), raftFrameService.voteResponse(transition.getFrame()), context);
                break;
            case appendEntriesRequest:
                raftFSM.handleAppendEntriesRequest(transition.getCommunicationNode(), raftFrameService.appendEntriesRequest(transition.getFrame()), context);
                break;
            case appendEntriesResponse:
                raftFSM.handleAppendEntriesResponse(transition.getCommunicationNode(), raftFrameService.appendEntriesResponse(transition.getFrame()));
                break;
            case roleChange:
                switch ((RaftRole)transition.getFrame().getAttributes().get("role")) {
                    case follower:
                        BecomeFollower becomeFollower = (BecomeFollower)transition.getFrame().getAttributes().get("roleData");
                        raftFSM.handleBecomeFollower(becomeFollower, context);
                        break;
                    case candidate:
                        BecomeCandidate becomeCandidate = (BecomeCandidate)transition.getFrame().getAttributes().get("roleData");
                        raftFSM.handleBecomeCandidate(becomeCandidate, context);
                        break;
                    case leader:
                        BecomeLeader becomeLeader = (BecomeLeader) transition.getFrame().getAttributes().get("roleData");
                        raftFSM.handleBecomeLeader(becomeLeader, context);
                        break;
                }
                break;
        }
    }
}
