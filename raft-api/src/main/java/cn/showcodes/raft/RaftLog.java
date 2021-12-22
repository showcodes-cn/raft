package cn.showcodes.raft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaftLog {
    long index;
    long term;
    RaftLogType type;
    byte[] data;
}
