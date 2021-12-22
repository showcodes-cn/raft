package cn.showcodes.raft.impl.frame;

import cn.showcodes.raft.RaftLog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaftFrameAppendEntriesRequest {
    long term;
    String leaderId;
    long prevLogIndex;
    long prevLogTerm;
    long commitIndex;
    RaftLog[] entries;
}
