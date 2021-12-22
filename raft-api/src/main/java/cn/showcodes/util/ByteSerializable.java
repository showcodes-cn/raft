package cn.showcodes.util;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public interface ByteSerializable {
    byte[] toBytes();
    void from(byte[] data);

    static byte[] toBytes(ByteSerializable[] items) {
        List<byte[]> data = Arrays.stream(items)
                .map(item -> item.toBytes())
                .collect(Collectors.toList());

        int total = Integer.BYTES * (1 + data.size()) + data.stream().mapToInt(i -> i.length).sum();
        ByteBuffer buffer = ByteBuffer.allocate(total);
        buffer.putInt(data.size());
        for(byte[] item : data) {
            buffer.putInt(item.length);
            buffer.put(item);
        }
        return buffer.array();
    }

    static <T extends ByteSerializable> T[] from(byte[] data, Class<T> tClass) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        int total = byteBuffer.getInt();
        List<T> result = new LinkedList();
        for(int i = 0 ; i < total; i++) {
            try {
                T t = tClass.newInstance();
                int size = byteBuffer.getInt();
                byte[] itemByte = new byte[size];
                byteBuffer.get(itemByte);
                t.from(itemByte);
                result.add(t);
            } catch (InstantiationException|IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result.toArray((T[])Array.newInstance(tClass, 0));
    }
}
