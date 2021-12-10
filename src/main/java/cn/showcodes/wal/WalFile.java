package cn.showcodes.wal;

public interface WalFile {
    void append(byte[] data, WriteCallback callback);
    void read(long pos, byte[] dest);
    void append(int val, WriteCallback callback);
    void append(long val, WriteCallback callback);
    int readInt(long pos);
    long readLong(long pos);
    long size();
    void close();
}
