package cs451.Packet;


import cs451.Host;

import java.util.Comparator;

public class PayloadPacket extends Packet implements Comparable<PayloadPacket>{
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

    @Override
    public int compareTo(PayloadPacket o) {
        return Integer.compare(this.seqNb, o.getSeqNb());
    }

    public static class PayloadPacketComparator implements Comparator<String> {
        /*
            First compare seqNb (in ascending order),
            then (in alphabetical order) receiverID, originalSenderID, senderID
         */
        @Override
        public int compare(String o1, String o2) {
            if (o1.equals(o2)) return 0;
            String[] o1Fields = o1.split(" ");
            String[] o2Fields = o2.split(" ");
            int seqNbOrder = Integer.compare(Integer.parseInt(o1Fields[3]), Integer.parseInt(o2Fields[3]));
            if (seqNbOrder != 0) return seqNbOrder;
            int receiverOrder = o1Fields[2].compareTo(o2Fields[2]);
            if (receiverOrder != 0) return receiverOrder;
            int originalSenderOrder = o1Fields[0].compareTo(o2Fields[0]);
            if (originalSenderOrder != 0) return originalSenderOrder;
            int senderOrder = o1Fields[1].compareTo(o2Fields[1]);
            return senderOrder;
        }
    }
}

