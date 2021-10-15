package cs451;


import java.util.Objects;

public class PayloadPacket extends Packet{
    private int senderId;
    private int seqNb;
//    private String pktId;

    /**
     * Used to create a new PayloadPacket
     * @param senderId
     * @param seqNb
     */
    public PayloadPacket(int senderId, int seqNb, Host senderHost, Host receiverHost) {
        setPktId(senderId + " " + seqNb);
        this.senderId = senderId;
        this.seqNb = seqNb;
        this.senderHost = senderHost;
        this.receiverHost = receiverHost;

        // /!\ Serialize at the last line of the constructor
        setByteArray(serializeToBytes());
    }

    public int getSenderId() {
        return senderId;
    }

    public int getSeqNb() {
        return seqNb;
    }

    @Override
    public String toString() {
        return getPktId(); //toExpectedFormat() + " from " + senderHost + " to " + receiverHost + " id " +
    }
}

