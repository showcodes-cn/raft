package cn.showcodes.wal;

public interface WalSegment {
    void write(int position, byte[] data);
    void write(int position, byte[] data, WriteCallback callback);
    void write(int position, byte[] data, int start, int size);
    void write(int position, byte[] data, int start, int size, WriteCallback callback);
    void read(int pos, byte[] dest);
    void read(int pos, byte[] dest, int start, int size);
    int length();
    void close();
}
