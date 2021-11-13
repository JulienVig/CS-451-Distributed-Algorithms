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
        setPktId(createPktId(seqNb, originalSenderId, senderId, receiverId));
        this.seqNb = seqNb;
        setSenderId(senderId);
        setReceiverId(receiverId);
        this.originalSenderId = originalSenderId;
        this.payload = String.valueOf(seqNb);
    }

    private long createPktId(int seqNb, int originalSenderId, int senderId, int receiverId){
        String format = "%04d"; // 0 pad for a length of 4
        String pktIdStr = new StringBuilder().append(seqNb)
                                            .append(String.format(format, originalSenderId))
                                            .append(String.format(format, senderId))
                                            .append(String.format(format, receiverId)).toString();
        return Long.valueOf(pktIdStr);
    }

//    public String getSimpleId(){
//        return getOriginalSenderId() + " " + seqNb;
//    }
    public long getSimpleId(){
        String format = "%04d";
        String simplePktIdStr = new StringBuilder().append(seqNb)
                .append(String.format(format, originalSenderId)).toString();
        return Long.valueOf(simplePktIdStr);
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
    public void readObject(ObjectInputStream in) throws IOException{
        seqNb = in.readInt();
        originalSenderId = in.readInt();
        setSenderId(in.readInt());
        setReceiverId(in.readInt());
        payload = String.valueOf(seqNb);
//        setPktId(createPktId(seqNb, originalSenderId, getSenderId(), getReceiverId()));
    }

    @Override
    public void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(getSeqNb());
        out.writeInt(getOriginalSenderId());
        out.writeInt(getSenderId());
        out.writeInt(getReceiverId());
    }
}

