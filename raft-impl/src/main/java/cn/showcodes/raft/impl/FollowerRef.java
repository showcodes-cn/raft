package cn.showcodes.raft.impl;

import cn.showcodes.raft.CommunicationNode;

public class FollowerRef {
    CommunicationNode communicationNode;
    long lastAlive;
    long createTime;
    long nextIndex;
    long matchedIndex;
}
