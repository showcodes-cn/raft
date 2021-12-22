package cn.showcodes.raft.impl.frame;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaftFrameAppendEntriesResponse {
    boolean success;
    long term;
    long lastLogIndex;
    long lastCommitIndex;
}
