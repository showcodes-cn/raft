package cn.showcodes.protocol;

public interface CommonProtocol {
    int magicCode();
    byte version();
    byte[] getPayload();
}
