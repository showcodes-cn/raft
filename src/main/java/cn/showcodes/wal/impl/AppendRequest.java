package cn.showcodes.wal.impl;

import cn.showcodes.wal.WriteCallback;

public class AppendRequest {
    long position;
    byte[] data;
    WriteCallback callback;
}
