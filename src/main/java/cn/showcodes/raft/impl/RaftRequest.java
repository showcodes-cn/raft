package cn.showcodes.raft.impl;

import cn.showcodes.raft.CommunicationNode;
import cn.showcodes.state.Transition;

public class RaftRequest implements Transition {
    CommunicationNode communicationNode;
    RaftFrame frame;
}
