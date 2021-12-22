package cn.showcodes.raft.impl.frame;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class RaftFrameVoteRequest {

    long term;
    String candidateId;
    long lastLogTerm;
    long lastLogIndex;

    public byte[] toBytes() {
        byte[] str = candidateId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES * 3 + Integer.BYTES + str.length);
        byteBuffer.putLong(term);
        byteBuffer.putLong(lastLogTerm);
        byteBuffer.putLong(lastLogIndex);
        byteBuffer.putInt(str.length);
        byteBuffer.put(str);
        return byteBuffer.array();
    }

    public static RaftFrameVoteRequest from(byte[] data) {
        RaftFrameVoteRequest voteRequest = new RaftFrameVoteRequest();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        voteRequest.term = buffer.getLong();
        voteRequest.lastLogTerm = buffer.getLong();
        voteRequest.lastLogIndex = buffer.getLong();
        int nameSize = buffer.getInt();
        byte[] nameBytes = new byte[nameSize];
        buffer.get(nameBytes);
        voteRequest.candidateId = new String(nameBytes, StandardCharsets.UTF_8);
        return voteRequest;
    }
}
