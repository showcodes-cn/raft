package cn.showcodes.raft.impl;

public class RaftNodeImpl {
    RaftRequestSupplierImpl requestSupplier;
    RaftFSM raftFSM;

    void run() {
        while(true) {
            RaftRequest request = requestSupplier.take();
            raftFSM.transmit(request);
        }
    }
}
