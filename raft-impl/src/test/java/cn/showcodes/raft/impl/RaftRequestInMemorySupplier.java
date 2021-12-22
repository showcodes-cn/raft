package cn.showcodes.raft.impl;

import cn.showcodes.raft.RaftRequest;
import cn.showcodes.raft.RaftRequestSupplier;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RaftRequestInMemorySupplier implements RaftRequestSupplier {
    BlockingQueue<RaftRequest> requests = new LinkedBlockingQueue<>();

    @Override
    public RaftRequest take() throws InterruptedException {
        return requests.take();
    }

    @Override
    public void append(RaftRequest request) {
        requests.add(request);
    }
}
