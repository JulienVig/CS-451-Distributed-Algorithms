package cs451;

import java.util.ArrayList;
import java.util.function.Consumer;


public class Writer implements Runnable{
    private volatile ArrayList<Integer> broadcastBuffer = new ArrayList<>();
    private volatile ArrayList<String> deliverBuffer  = new ArrayList<>();
    private Consumer<Integer> writeBroadcast;
    private Consumer<String> writeDeliver;


    public Writer(Consumer<Integer> writeBroadcast, Consumer<String> writeDeliver) {
        this.writeBroadcast = writeBroadcast;
        this.writeDeliver = writeDeliver;
    }

    public void write(PayloadPacket pkt, Operation op){
        if(op == Operation.BROADCAST) broadcastBuffer.add(pkt.getSeqNb());
        if(op == Operation.DELIVER) deliverBuffer.add(pkt.getSenderId() + " " + pkt.getSeqNb());
    }

    public void flush(){
        for (int i = 0; i < broadcastBuffer.size(); i++) {
            writeBroadcast.accept(broadcastBuffer.remove(i));
        }
        for (int i = 0; i < deliverBuffer.size(); i++) {
            writeDeliver.accept(deliverBuffer.remove(i));
        }
//        for (Integer seqNb : broadcastBuffer)
//        for (String senderIdSeqNb : deliverBuffer) writeDeliver.accept(senderIdSeqNb);
    }

    @Override
    public void run(){
        while(true) flush();
    }
}
