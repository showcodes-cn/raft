package cn.showcodes.raft.impl.frame;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BecomeFollower {
    String leaderId;
    long electionTimeout;
    long currentTerm;
}
