package cn.showcodes.raft.impl;

import cn.showcodes.raft.RaftNode;
import cn.showcodes.raft.RaftRequest;
import cn.showcodes.raft.RaftRequestSupplier;

import java.util.concurrent.atomic.AtomicBoolean;

public class RaftNodeImpl implements RaftNode {
    RaftRequestSupplier requestSupplier;
    RaftFSM raftFSM;
    AtomicBoolean running = new AtomicBoolean();
    RaftConfig raftConfig;
    Thread thread;

    public void start() {
        if (thread == null) {
            raftFSM.start(raftConfig);
        }
        thread = new Thread(this::run);
        running.set(true);
        thread.start();
    }

    public void stop() {
        running.set(false);
    }

    void run() {
        while(running.get()) {
            try {
                RaftRequest request = requestSupplier.take();
                raftFSM.transmit(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
