package cn.showcodes.raft;

import cn.showcodes.util.ByteSerializable;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.Objects;

@Getter
@Setter
public class RaftLog implements ByteSerializable {
    long term;
    long index;
    RaftLogType type;
    byte[] data;

    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES * 2 + 1 + Integer.BYTES + (data == null ? 0 : data.length));
        byteBuffer.putLong(term);
        byteBuffer.putLong(index);
        byteBuffer.put((byte)type.ordinal());
        if (data == null) {
            byteBuffer.putInt(0);
        } else {
            byteBuffer.putInt(data.length);
            byteBuffer.put(data);
        }
        return byteBuffer.array();
    }

    @Override
    public void from(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        term = byteBuffer.getLong();
        index = byteBuffer.getLong();
        type = RaftLogType.values()[byteBuffer.get()];
        int size =  byteBuffer.getInt();
        byte[] bs = new byte[size];
        byteBuffer.get(bs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaftLog raftLog = (RaftLog) o;
        return term == raftLog.term && index == raftLog.index && type == raftLog.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, index, type);
    }
}
