package cn.showcodes.raft.impl;

import cn.showcodes.raft.CommunicationService;
import cn.showcodes.state.FiniteStateMachine;

public class RaftFSM implements FiniteStateMachine<RaftRequest> {

    CommunicationService communicationService;

    @Override
    public FiniteStateMachine transmit(RaftRequest transition) {
        return null;
    }
}
