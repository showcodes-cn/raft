package cn.showcodes.raft;


public interface RaftRequestSupplier {
    RaftRequest take() throws InterruptedException;
    void append(RaftRequest request);
}
