package cs451.Packet;


import cs451.Host;

public class PayloadPacket extends Packet{
    private int senderId;
    private int seqNb;
    private String payload; // The payload is not used

    /**
     * Used to create a new PayloadPacket
     * @param senderId
     * @param seqNb
     */
    public PayloadPacket(int senderId, int seqNb, Host senderHost, Host receiverHost) {
        setPktId(senderId + " " + receiverHost.getId() + " " + seqNb);
        this.senderId = senderId;
        this.seqNb = seqNb;
        setSenderHost(senderHost);
        setReceiverHost(receiverHost);
        this.payload = String.valueOf(seqNb);

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

