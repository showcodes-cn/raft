package cn.showcodes.raft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunicationNode {
    String host;
    int port;

    public CommunicationNode(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
