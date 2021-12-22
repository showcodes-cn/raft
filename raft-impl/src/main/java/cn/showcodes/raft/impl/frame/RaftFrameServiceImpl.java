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
        return RaftFrameVoteResponse.from(frame.getData());
    }

    @Override
    public RaftFrame from(RaftFrameVoteResponse response) {
        RaftFrame frame = new RaftFrame();
        frame.setType(RaftFrameType.voteResponse);
        frame.setData(response.toBytes());
        return frame;
    }

    @Override
    public RaftFrameAppendEntriesRequest appendEntriesRequest(RaftFrame frame) {
        RaftFrameAppendEntriesRequest request = new RaftFrameAppendEntriesRequest();
        request.from(frame.getData());
        return request;
    }

    @Override
    public RaftFrame from(RaftFrameAppendEntriesRequest request) {
        RaftFrame frame = new RaftFrame();
        frame.setType(RaftFrameType.appendEntriesRequest);
        frame.setData(request.toBytes());
        return frame;
    }

    @Override
    public RaftFrameAppendEntriesResponse appendEntriesResponse(RaftFrame frame) {
        RaftFrameAppendEntriesResponse response = new RaftFrameAppendEntriesResponse();
        response.from(frame.getData());
        return response;
    }

    @Override
    public RaftFrame from(RaftFrameAppendEntriesResponse response) {
        RaftFrame frame = new RaftFrame();
        frame.setType(RaftFrameType.appendEntriesResponse);
        frame.setData(response.toBytes());
        return frame;
    }
}
