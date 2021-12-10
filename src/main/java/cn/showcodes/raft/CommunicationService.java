package cn.showcodes.raft;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CommunicationService {
    CompletableFuture<RaftProtocol> request(CommunicationNode node, RaftProtocol data);
    CompletableFuture<List<RaftProtocol>> broadcast(List<CommunicationNode> nodes, RaftProtocol data);
}
