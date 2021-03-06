package cs451.Packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * An AckPacket only contains the PayloadPacket's id
 * to not congest the network while sending acks.
 * This implies an local overhead while checking for retransmissions
 * since we have to find the original PayloadPacket from its id.
 */
public class AckPacket extends Packet{
    private long payloadPktId;

    public static final int BYTE_SIZE = 9;

    @Override
    public int getByteSize() {
        return BYTE_SIZE;
    }

    public AckPacket(PayloadPacket payloadPkt){
        this.payloadPktId = payloadPkt.getPktId();
        setPktId(payloadPktId + 1);
        // Swap hosts for response
        setReceiverId(payloadPkt.getSenderId());
        setSenderId(payloadPkt.getReceiverId());
    }

    public AckPacket(long payloadPktId){
        this.payloadPktId = payloadPktId;
    }

    public long getPayloadPktId() {
        return payloadPktId;
    }

    @Override
    public String toString(){
        return "ack for " + payloadPktId;
    }

    @Override
    public byte[] serializeToBytes() {
        ByteBuffer bb = ByteBuffer.allocate(BYTE_SIZE);
        serializeToBytes(bb);
        return bb.array();
    }

    @Override
    public void serializeToBytes(ByteBuffer bb){
        bb.put((byte) 0); // 0 for PayloadPacket
        bb.putLong(payloadPktId);
    }

    public AckPacket(ByteBuffer bb){
        this(bb.getLong());
    }
}
