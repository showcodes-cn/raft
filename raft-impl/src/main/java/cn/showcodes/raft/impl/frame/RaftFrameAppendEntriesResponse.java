package cn.showcodes.raft.impl.frame;

import cn.showcodes.util.ByteSerializable;
import lombok.Getter;
import lombok.Setter;
import java.nio.ByteBuffer;

@Getter
@Setter
public class RaftFrameAppendEntriesResponse implements ByteSerializable {
    boolean success;
    long term;
    long lastIndex;

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + Long.BYTES);
        buffer.put(success ? (byte)0x01 : 0x00);
        buffer.putLong(term);
        buffer.putLong(lastIndex);
        return buffer.array();
    }

    @Override
    public void from(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        success = buffer.get() == 0x00 ? Boolean.FALSE : Boolean.TRUE;
        term = buffer.getLong();
        lastIndex = buffer.getLong();
    }
}
