package cn.showcodes.raft.impl.frame;

import cn.showcodes.raft.RaftLog;
import cn.showcodes.util.ByteSerializable;
import lombok.Getter;
import lombok.Setter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class RaftFrameAppendEntriesRequest implements ByteSerializable {
    long term;
    long prevLogTerm;
    long prevLogIndex;
    long commitIndex;
    String leaderId;
    RaftLog[] entries;

    public byte[] toBytes() {
        byte[] entriesBytes = ByteSerializable.toBytes(entries);
        byte[] leaderIdBytes = leaderId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES * 4 + Integer.BYTES * 2 + entriesBytes.length + leaderIdBytes.length);
        byteBuffer.putLong(term);
        byteBuffer.putLong(prevLogTerm);
        byteBuffer.putLong(prevLogIndex);
        byteBuffer.putLong(commitIndex);
        byteBuffer.putInt(leaderIdBytes.length);
        byteBuffer.put(leaderIdBytes);
        byteBuffer.putInt(entriesBytes.length);
        byteBuffer.put(entriesBytes);
        return byteBuffer.array();
    }

    @Override
    public void from(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        term = buffer.getLong();
        prevLogTerm = buffer.getLong();
        prevLogIndex = buffer.getLong();
        commitIndex = buffer.getLong();
        int size = buffer.getInt();
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        leaderId = new String(bytes, StandardCharsets.UTF_8);
        size = buffer.getInt();
        bytes = new byte[size];
        buffer.get(bytes);
        entries = ByteSerializable.from(bytes, RaftLog.class);
    }
}
