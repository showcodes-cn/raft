package cn.showcodes.raft.impl;

import cn.showcodes.raft.CommunicationNode;
import cn.showcodes.raft.CommunicationService;
import cn.showcodes.raft.RaftProtocol;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommunicationServiceImpl implements CommunicationService {
    @Override
    public CompletableFuture<RaftProtocol> request(CommunicationNode node, RaftProtocol data) {
        return null;
    }

    @Override
    public CompletableFuture<List<RaftProtocol>> broadcast(List<CommunicationNode> nodes, RaftProtocol data) {
        return null;
    }
}
