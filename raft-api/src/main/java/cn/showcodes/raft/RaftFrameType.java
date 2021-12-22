package cn.showcodes.raft;

public enum RaftFrameType {
    none, voteRequest, voteResponse, appendEntriesRequest, appendEntriesResponse,
    roleChange
}
