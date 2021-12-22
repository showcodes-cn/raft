package cn.showcodes.raft.impl;

import cn.showcodes.raft.CommunicationNode;

import java.util.List;

public class RaftConfig {
    String nodeId;
    List<CommunicationNode> peers;
    long electionMinTime = 150;
    long electionMaxTime = 300;
    long voteTimeout = 1000;
    long heartbeatTimeout = 100;
    int batchLogCount = 10;
}