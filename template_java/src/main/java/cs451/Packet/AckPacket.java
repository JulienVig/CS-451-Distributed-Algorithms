package cs451.Packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An AckPacket only contains the PayloadPacket's id
 * to not congest the network while sending acks.
 * This implies an local overhead while checking for retransmissions
 * since we have to find the original PayloadPacket from its id.
 */
public class AckPacket extends Packet{
    private String payloadPktId;

    public AckPacket(PayloadPacket payloadPkt){
        this.payloadPktId = payloadPkt.getPktId();
        setPktId("ack " + payloadPktId);
        // Swap hosts for response
        setReceiverId(payloadPkt.getSenderId());
        setSenderId(payloadPkt.getReceiverId());

//        // /!\ Serialize at the last line of the constructor
//        setByteArray(serializeToBytes());
    }

    public String getPayloadPktId() {
        return payloadPktId;
    }

    @Override
    public String toString(){
        return "ack for " + payloadPktId;
    }

    @Override
    public void readObject(ObjectInputStream in) throws IOException{
        payloadPktId = in.readUTF();
    }

    @Override
    public void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(payloadPktId);
    }
}
