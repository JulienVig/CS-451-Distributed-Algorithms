package cs451.Packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static cs451.Main.NB_HOSTS;

public class PayloadPacket extends Packet{ //} implements Comparable<PayloadPacket>{
    private int seqNb;
    private int payload; // The payload is not used
    private int originalSenderId;
    private long simpleId;

    private static final long idBase1 = (long) Math.pow(10, 12);
    private static final long idBase2 = 100000000;
    private static final long idBase3 = 10000;

    public static final int BYTE_SIZE = 17 + 4 * NB_HOSTS;
    private int[] clock = new int[NB_HOSTS];

    @Override
    public int getByteSize() {
        return BYTE_SIZE;
    }


    public PayloadPacket(int seqNb, int originalSenderId, int senderId, int receiverId) {
        setPktId(createPktId(seqNb, originalSenderId, senderId, receiverId));
        this.seqNb = seqNb;
        setSenderId(senderId);
        setReceiverId(receiverId);
        this.originalSenderId = originalSenderId;
        this.payload = seqNb;
    }

    //Only used to deserialize packets, the clock is set in the deserialization
    public PayloadPacket(int seqNb, int originalSenderId, int senderId, int receiverId, byte[] bytes) {
        this(seqNb, originalSenderId, senderId, receiverId);
        setByteArray(bytes);
    }

    public PayloadPacket(int seqNb, int originalSenderId, int senderId, int receiverId, int[] clock){
        this(seqNb, originalSenderId, senderId, receiverId);
        this.clock = clock;
    }
    public PayloadPacket(int seqNb, int senderId, int receiverId, int[] clock){
        this(seqNb, senderId, senderId, receiverId, clock);
    }

    private long createPktId(int seqNb, int originalSenderId, int senderId, int receiverId){
        //shift by one bit to make it even (ack packets are odd)
        return (seqNb * idBase1 + originalSenderId * idBase2 + senderId * idBase3 + receiverId) << 1;
    }
    public long getSimpleId(){
        if (simpleId == 0) simpleId = seqNb * idBase3 + originalSenderId;
        return simpleId;
    }

    public int[] getClock(){
        return clock;
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
        ByteBuffer bb = ByteBuffer.allocate(BYTE_SIZE);
        serializeToBytes(bb);
        return bb.array();
    }

    @Override
    public void serializeToBytes(ByteBuffer bb) {
        bb.put((byte) 1); // 1 for PayloadPacket
        bb.putInt(seqNb);
        bb.putInt(originalSenderId);
        bb.putInt(getSenderId());
        bb.putInt(getReceiverId());
        for (int lsn : clock) bb.putInt(lsn);
    }

    public PayloadPacket(ByteBuffer bb, byte[] bytes){
        this(bb.getInt(), bb.getInt(), bb.getInt(), bb.getInt(), bytes);
        clock = new int[NB_HOSTS];
        for (int i = 0; i < NB_HOSTS; i++) clock[i] = bb.getInt();
    }
}

