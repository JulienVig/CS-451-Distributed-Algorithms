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
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.put((byte) 0); // 0 for PayloadPacket
        bb.putLong(payloadPktId);
        return bb.array();
    }

    public static Packet deserializeToObject(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes, 1, 8);//Offset of 1 since the first byte is the Packet type
        return new AckPacket(bb.getLong());
    }
}
