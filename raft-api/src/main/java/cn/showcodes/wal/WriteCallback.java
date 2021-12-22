package cn.showcodes.wal;

public interface WriteCallback {
    void onWrite(long position);
    void onFlush(long position);
}
