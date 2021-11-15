package cs451.Packet;

import java.nio.ByteBuffer;

public class PayloadPacket extends Packet{ //} implements Comparable<PayloadPacket>{
    private int seqNb;
    private int payload; // The payload is not used
    private int originalSenderId;
    private long simpleId;

    private static final long idBase1 = (long) Math.pow(10, 12);
    private static final long idBase2 = 100000000;
    private static final long idBase3 = 10000;

    public PayloadPacket(int seqNb, int originalSenderId, int senderId, int receiverId) {
        setPktId(createPktId(seqNb, originalSenderId, senderId, receiverId));
        this.seqNb = seqNb;
        setSenderId(senderId);
        setReceiverId(receiverId);
        this.originalSenderId = originalSenderId;
        this.payload = seqNb;
    }
    public PayloadPacket(int seqNb, int originalSenderId, int senderId, int receiverId, byte[] bytes) {
        this(seqNb, originalSenderId, senderId, receiverId);
        setByteArray(bytes);
    }

    private long createPktId(int seqNb, int originalSenderId, int senderId, int receiverId){
        return seqNb * idBase1 + originalSenderId * idBase2 + senderId * idBase3 + receiverId;
    }
    public long getSimpleId(){
        if (simpleId == 0) simpleId = seqNb * idBase3 + originalSenderId;
        return simpleId;
    }

    public PayloadPacket(int seqNb, int senderId, int receiverId){
        this(seqNb, senderId, senderId, receiverId);
    }

    public int getSeqNb() {
        return seqNb;
    }

    public int getOriginalSenderId() {
        return originalSenderId;
    }

    @Override
    public String toString() {
//        return String.valueOf(getPktId());
        return originalSenderId + " " + getSenderId() + " " + getReceiverId() + " " + seqNb;
    }

    @Override
    public byte[] serializeToBytes(){
        ByteBuffer bb = ByteBuffer.allocate(21);
        bb.put((byte) 1); // 1 for PayloadPacket
        bb.putInt(seqNb);
        bb.putInt(originalSenderId);
        bb.putInt(getSenderId());
        bb.putInt(getReceiverId());
        return bb.array();
    }

    public static Packet deserializeToObject(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes, 1, 20);//Offset of 1 since the first byte is the Packet type
        return new PayloadPacket(bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt(), bytes);
    }
}

