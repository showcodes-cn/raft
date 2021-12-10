package cn.showcodes.raft;

import cn.showcodes.raft.impl.RaftRequest;

public interface RaftRequestSupplier {
    RaftRequest take();
}
