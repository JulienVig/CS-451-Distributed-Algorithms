package cs451.Packet;

import cs451.Host;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Serialization inspired from https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
 */
public abstract class Packet {
    private byte[] byteArray = null;
    private long pktId;
    private int senderId;
    private int receiverId;

    abstract public byte[] serializeToBytes();

    abstract public void serializeToBytes(ByteBuffer bb);


    abstract public int getByteSize();

    public int length() {
        return byteArray.length;
    }

    public long getPktId() {
        return pktId;
    }

    public void setPktId(long pktId) {
        this.pktId = pktId;
    }

    public byte[] getBytes() {
        if (byteArray == null || byteArray.length == 0) setByteArray(serializeToBytes());
        return byteArray;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    /**
     * Source: https://www.sitepoint.com/implement-javas-equals-method-correctly/
     * @param o object to be compared with
     * @return boolean
     */
    @Override
    public boolean equals(Object o){
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        PayloadPacket that = (PayloadPacket) o;
        // field comparison
        return Objects.equals(this.getPktId(), that.getPktId());
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getPktId());
    }
}