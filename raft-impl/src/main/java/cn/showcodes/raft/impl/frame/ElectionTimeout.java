package cn.showcodes.raft.impl.frame;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElectionTimeout {
    long startTime;
    long outTime;
    long term;
}
