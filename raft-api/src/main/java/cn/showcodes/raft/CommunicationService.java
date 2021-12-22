package cn.showcodes.raft;

import java.util.concurrent.CompletableFuture;

public interface CommunicationService {
    CompletableFuture<RaftProtocol> request(CommunicationNode node, RaftProtocol data);
}
