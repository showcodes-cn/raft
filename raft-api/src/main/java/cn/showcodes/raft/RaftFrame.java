package cn.showcodes.raft;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RaftFrame implements RaftProtocol {
    RaftFrameType type;
    Map<String, Object> attributes;
    byte[] data;
}
