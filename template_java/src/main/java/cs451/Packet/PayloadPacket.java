package cs451.Packet;


import cs451.Host;

import java.io.*;
import java.util.Comparator;

public class PayloadPacket extends Packet{ //} implements Comparable<PayloadPacket>{
    private int seqNb;
    private String payload; // The payload is not used
    private int originalSenderId;

    public PayloadPacket(int seqNb, int originalSenderId, int senderId, int receiverId) {
//        setPktId(createPktId(seqNb, originalSenderHost, senderHost, receiverHost));
//        System.out.print(getPktId() + " - ");

        setPktId(originalSenderId + " " + senderId + " " + receiverId + " " + seqNb);
        this.seqNb = seqNb;
        setSenderId(senderId);
        setReceiverId(receiverId);
        this.originalSenderId = originalSenderId;
        this.payload = String.valueOf(seqNb);

//        // /!\ Serialize at the last line of the constructor
//        setByteArray(serializeToBytes());
    }

    public String getSimpleId(){
        return getOriginalSenderId() + " " + seqNb;
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
        return getPktId();
//        return originalSenderHost.getId() + " " + getSenderHost().getId() + " " + getReceiverHost().getId() + " " + seqNb;

    }

    @Override
    public void readObject(ObjectInputStream in) throws IOException{
        seqNb = in.readInt();
        originalSenderId = in.readInt();
        setSenderId(in.readInt());
        setReceiverId(in.readInt());
        payload = String.valueOf(seqNb);
        setPktId(originalSenderId + " " + getSenderId() + " " + getReceiverId() + " " + seqNb);

        // /!\ Serialize at the last line of the constructor
        setByteArray(serializeToBytes());
    }

    @Override
    public void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(getSeqNb());
        out.writeInt(getOriginalSenderId());
        out.writeInt(getSenderId());
        out.writeInt(getReceiverId());
    }

//    public static Packet deserializePkt(ObjectInputStream in) throws IOException {
//        return new PayloadPacket(in.readInt(), in.readInt(), in.readInt(), in.readInt());
//    }

//    @Override
//    public int compareTo(PayloadPacket o) {
//        int seqNbOrder = Integer.compare(this.seqNb, o.getSeqNb());
//        if (seqNbOrder != 0) return seqNbOrder;
//        return this.getPktId().compareTo(o.getPktId());
//    }

    public static class PayloadPacketComparator implements Comparator<String> {
        /*
            First compare seqNb (in ascending order),
            then (in alphabetical order) receiverID, originalSenderID, senderID
         */
        @Override
        public int compare(String o1, String o2) {
            int seqNbOrder = Integer.compare(Integer.parseInt(o1.split(" ")[3]), Integer.parseInt(o2.split(" ")[3]));
            if (seqNbOrder != 0) return seqNbOrder;
            return o1.compareTo(o2);
        }
    }
}

