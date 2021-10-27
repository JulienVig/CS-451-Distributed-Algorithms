package cs451.Packet;


import cs451.Host;

public class PayloadPacket extends Packet{
    private int seqNb;
    private String payload; // The payload is not used
    private Host originalSenderHost;

    public PayloadPacket(int seqNb, Host originalSenderHost, Host senderHost, Host receiverHost) {
        setPktId(originalSenderHost.getId() + " " + senderHost.getId() + " " + receiverHost.getId() + " " + seqNb);
        this.seqNb = seqNb;
        setSenderHost(senderHost);
        setReceiverHost(receiverHost);
        this.originalSenderHost = originalSenderHost;
        this.payload = String.valueOf(seqNb);

        // /!\ Serialize at the last line of the constructor
        setByteArray(serializeToBytes());
    }

    public String getSimpleId(){
        return getOriginalSenderId() + " " + seqNb;
    }

    public PayloadPacket(int seqNb, Host senderHost, Host receiverHost){
        this(seqNb, senderHost, senderHost, receiverHost);
    }

    public int getSenderId() {
        return getSenderHost().getId();
    }

    public int getSeqNb() {
        return seqNb;
    }

    public int getOriginalSenderId() {
        return originalSenderHost.getId();
    }

    public Host getOriginalSenderHost() {
        return originalSenderHost;
    }

    @Override
    public String toString() {
        return getPktId();

    }
}

