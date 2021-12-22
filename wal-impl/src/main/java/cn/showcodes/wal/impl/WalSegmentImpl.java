package cn.showcodes.wal.impl;

import cn.showcodes.wal.WalSegment;
import cn.showcodes.wal.WriteCallback;

public class WalSegmentImpl implements WalSegment {

    String file;
    int size;

    WalSegmentImpl(String file, int size) {
        this.file = file;
        this.size = size;
    }

    @Override
    public void write(int position, byte[] data) {

    }

    @Override
    public void write(int position, byte[] data, WriteCallback callback) {

    }

    @Override
    public void write(int position, byte[] data, int start, int size) {

    }

    @Override
    public void write(int position, byte[] data, int start, int size, WriteCallback callback) {

    }

    @Override
    public void read(int pos, byte[] dest) {

    }

    @Override
    public void read(int pos, byte[] dest, int start, int size) {

    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public void close() {

    }
}
