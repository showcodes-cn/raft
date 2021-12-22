package cn.showcodes.raft.impl.frame;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaftFrameVoteResponse {
    String followerId;
    boolean success;
    long term;
    String voteGranted;
}
