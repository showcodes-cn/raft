package cn.showcodes.raft.impl;


import cn.showcodes.raft.RaftRequest;
import cn.showcodes.raft.RaftRequestSupplier;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class RaftRequestMemorySupplier implements RaftRequestSupplier {

    BlockingDeque<RaftRequest> queue = new LinkedBlockingDeque<>();

    public RaftRequest take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public void append(RaftRequest request) {
        queue.addFirst(request);
    }
}
