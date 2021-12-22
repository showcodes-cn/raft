package cn.showcodes.raft.impl;

import cn.showcodes.raft.*;

import java.util.concurrent.CompletableFuture;

public class RaftHandleContext {
    RaftRequest request;
    CommunicationService communicationService;
    RaftRequestSupplier requestSupplier;

    // 触发一个request请求事件
    public void fireRequest(RaftFrame frame) {
        fireRequest(null, frame);
    }

    public void fireRequest(CommunicationNode node, RaftFrame frame) {
        RaftRequest request = new RaftRequest();
        request.setCommunicationNode(node);
        request.setFrame(frame);
        requestSupplier.append(request);
    }

    // 发送response
    public CompletableFuture<RaftProtocol> sendResponse(RaftFrame frame) {
        return sendRequest(request.getCommunicationNode(), frame);
    }

    // 发送request到给定的node
    CompletableFuture<RaftProtocol> sendRequest(CommunicationNode node, RaftFrame frame) {
        RaftRequest request = new RaftRequest();
        request.setCommunicationNode(node);
        request.setFrame(frame);
        return sendRequest(request);
    }

    CompletableFuture<RaftProtocol> sendRequest(RaftRequest request) {
        return communicationService.request(request.getCommunicationNode(), request.getFrame());
    }
}
