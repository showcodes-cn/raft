package cn.showcodes.raft.impl;

import cn.showcodes.raft.CommunicationNode;
import cn.showcodes.raft.CommunicationService;
import cn.showcodes.raft.RaftProtocol;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class FakeCommunicationService implements CommunicationService {
    public BlockingQueue<RaftProtocol> outgoing = new LinkedBlockingQueue<>();

    @Override
    public CompletableFuture<RaftProtocol> request(CommunicationNode node, RaftProtocol data) {
        outgoing.add(data);
        return CompletableFuture.completedFuture(data);
    }
}
