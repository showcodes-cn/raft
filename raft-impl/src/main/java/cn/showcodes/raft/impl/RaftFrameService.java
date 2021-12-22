package cn.showcodes.raft.impl;

import cn.showcodes.raft.RaftFrame;
import cn.showcodes.raft.impl.frame.RaftFrameAppendEntriesRequest;
import cn.showcodes.raft.impl.frame.RaftFrameAppendEntriesResponse;
import cn.showcodes.raft.impl.frame.RaftFrameVoteRequest;
import cn.showcodes.raft.impl.frame.RaftFrameVoteResponse;

public interface RaftFrameService {
    RaftFrameVoteRequest voteRequest(RaftFrame frame);
    RaftFrame from(RaftFrameVoteRequest request);

    RaftFrameVoteResponse voteResponse(RaftFrame frame);
    RaftFrame from(RaftFrameVoteResponse response);

    RaftFrameAppendEntriesRequest appendEntriesRequest(RaftFrame frame);
    RaftFrame from(RaftFrameAppendEntriesRequest request);

    RaftFrameAppendEntriesResponse appendEntriesResponse(RaftFrame frame);
    RaftFrame from(RaftFrameAppendEntriesResponse response);
}
