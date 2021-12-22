package cn.showcodes.wal.impl;

import cn.showcodes.wal.WalFile;
import cn.showcodes.wal.WalSegment;
import cn.showcodes.wal.WriteCallback;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WalFileImpl implements WalFile {
    WalFileConfig walFileConfig;

    ReadWriteLock cursorLock = new ReentrantReadWriteLock();
    BlockingQueue<AppendRequest> requests = new LinkedBlockingQueue<>();
    Map<Long, WalSegment> slices = new ConcurrentHashMap<>();
    long cursor;

    WalFileImpl(WalFileConfig walFileConfig) {
        this.walFileConfig = walFileConfig;
        File f = Paths.get(walFileConfig.folder).toFile();
        if (f.exists()) {
            load(f, walFileConfig.segmentSize);
        } else {
            f.mkdirs();
        }
    }

    void load(File folder, int segmentSize) {
        String suf = String.format(".%s", walFileConfig);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(suf));

        for(File f : files) {
            String[] values = f.getName().split("\\.");
            long fIndex = Long.parseLong(values[0]);
            int sliceIndex = (int)(fIndex / segmentSize);
            WalSegment slice = getSegment(walFileConfig.folder, sliceIndex, walFileConfig.segmentSize, walFileConfig.suffix);
            long last = sliceIndex * segmentSize + slice.length();
            if (last > cursor) {
                cursor = last;
            }
        }
    }

    WalSegment getSegment(String path, long sliceIndex, int segmentSize, String suffix) {
        return slices.computeIfAbsent(sliceIndex, (k) -> {
            WalSegment slice = new WalSegmentImpl(
                    Paths.get(path, String.format("%s.%s", sliceIndex * segmentSize, suffix)).toString(),
                    segmentSize
            );
            return slice;
        });
    }

    @Override
    public void append(byte[] data, WriteCallback callback) {

    }

    @Override
    public void read(long pos, byte[] dest) {

    }

    @Override
    public void append(int val, WriteCallback callback) {

    }

    @Override
    public void append(long val, WriteCallback callback) {

    }

    @Override
    public int readInt(long pos) {
        return 0;
    }

    @Override
    public long readLong(long pos) {
        return 0;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void close() {

    }
}
