package cn.showcodes.raft.impl.frame;

import cn.showcodes.raft.RaftFrame;
import cn.showcodes.raft.RaftFrameType;
import cn.showcodes.raft.impl.RaftFrameService;

public class RaftFrameServiceImpl implements RaftFrameService {
    @Override
    public RaftFrameVoteRequest voteRequest(RaftFrame frame) {
        if (frame.getType() != RaftFrameType.voteRequest) {
            throw new IllegalArgumentException("");
        }
        return RaftFrameVoteRequest.from(frame.getData());
    }

    @Override
    public RaftFrame from(RaftFrameVoteRequest request) {
        RaftFrame raftFrame = new RaftFrame();
        raftFrame.setType(RaftFrameType.voteRequest);
        raftFrame.setData(request.toBytes());
        return raftFrame;
    }


    @Override
    public RaftFrameVoteResponse voteResponse(RaftFrame frame) {
        return null;
    }

    @Override
    public RaftFrame from(RaftFrameVoteResponse response) {
        return null;
    }

    @Override
    public RaftFrameAppendEntriesRequest appendEntriesRequest(RaftFrame frame) {
        return null;
    }

    @Override
    public RaftFrame from(RaftFrameAppendEntriesRequest request) {
        return null;
    }

    @Override
    public RaftFrameAppendEntriesResponse appendEntriesResponse(RaftFrame frame) {
        return null;
    }

    @Override
    public RaftFrame from(RaftFrameAppendEntriesResponse response) {
        return null;
    }
}
