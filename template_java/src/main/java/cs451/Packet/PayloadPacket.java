package cs451.Packet;


import cs451.Host;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

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
//        String format = "%04d"; // 0 pad for a length of 4
//        String pktIdStr = new StringBuilder().append(seqNb)
//                                            .append(String.format(format, originalSenderId))
//                                            .append(String.format(format, senderId))
//                                            .append(String.format(format, receiverId)).toString();
//        return Long.valueOf(pktIdStr);

        return seqNb * idBase1 + originalSenderId * idBase2 + senderId * idBase3 + receiverId;
    }

//    public String getSimpleId(){
//        return getOriginalSenderId() + " " + seqNb;
//    }
    public long getSimpleId(){
        if (simpleId == 0) {
            String format = "%04d";
            String simplePktIdStr = new StringBuilder().append(seqNb)
                    .append(String.format(format, originalSenderId)).toString();
            simpleId = Long.valueOf(simplePktIdStr);
        }
        return simpleId;
//        return Long.valueOf(simplePktIdStr);
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
        return String.valueOf(getPktId());
//        return originalSenderId + " " + getSenderId() + " " + getReceiverId() + " " + seqNb;
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
        int seqNb = bb.getInt();
        int originalSenderId = bb.getInt();
        int senderId= bb.getInt();
        int receiverId = bb.getInt();
        return new PayloadPacket(seqNb, originalSenderId, senderId, receiverId, bytes);
    }
}

