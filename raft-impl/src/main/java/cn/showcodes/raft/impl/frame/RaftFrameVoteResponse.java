package cn.showcodes.raft.impl.frame;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class RaftFrameVoteResponse {
    String followerId;
    boolean success;
    long term;
    String voteGranted;

    public byte[] toBytes() {
        byte[] fidBytes = followerId.getBytes(StandardCharsets.UTF_8);
        byte[] granted = voteGranted.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + Long.BYTES + Integer.BYTES * 2 + fidBytes.length + granted.length);
        byteBuffer.put(success ? (byte)0x01 : 0x0);
        byteBuffer.putLong(term);
        byteBuffer.putInt(fidBytes.length);
        byteBuffer.put(fidBytes);
        byteBuffer.putInt(granted.length);
        byteBuffer.put(granted);
        return byteBuffer.array();
    }

    public static RaftFrameVoteResponse from(byte[] data) {
        RaftFrameVoteResponse response = new RaftFrameVoteResponse();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        response.success = byteBuffer.get() == 0x00 ? Boolean.FALSE : Boolean.TRUE;
        response.term = byteBuffer.getLong();
        int size = byteBuffer.getInt();
        byte[] str = new byte[size];
        byteBuffer.get(str);
        response.followerId = new String(str, StandardCharsets.UTF_8);
        size = byteBuffer.getInt();
        str = new byte[size];
        response.voteGranted = new String(str, StandardCharsets.UTF_8);
        return response;
    }
}
