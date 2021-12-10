package cn.showcodes.wal;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public interface MappedMemoryService {
    MappedByteBuffer create(FileChannel fileChannel);
    void clean(MappedByteBuffer mappedByteBuffer);
}
