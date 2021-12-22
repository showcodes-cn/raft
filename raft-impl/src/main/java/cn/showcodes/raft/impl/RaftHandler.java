package cn.showcodes.raft.impl;

import cn.showcodes.raft.RaftRequest;

public interface RaftHandler {
    void handle(RaftRequest request, RaftHandleContext context);
}
