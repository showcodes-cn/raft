package cn.showcodes.raft;

import cn.showcodes.state.Transition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaftRequest implements Transition {
    CommunicationNode communicationNode;
    RaftFrame frame;
}
