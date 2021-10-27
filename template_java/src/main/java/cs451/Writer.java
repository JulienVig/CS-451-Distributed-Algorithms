package cs451;

import cs451.Packet.PayloadPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;


public class Writer implements Runnable{
    private volatile Queue<Integer> broadcastBuffer = new ConcurrentLinkedQueue<>();
    private volatile Queue<String> deliverBuffer  = new ConcurrentLinkedQueue<>();
    private Consumer<Integer> writeBroadcast;
    private Consumer<String> writeDeliver;

    public Writer(Consumer<Integer> writeBroadcast, Consumer<String> writeDeliver) {
        this.writeBroadcast = writeBroadcast;
        this.writeDeliver = writeDeliver;
    }

    public void write(PayloadPacket pkt, Operation op){
        try {
            if (op == Operation.BROADCAST) broadcastBuffer.add(pkt.getSeqNb());
            if (op == Operation.DELIVER) deliverBuffer.add(pkt.getOriginalSenderId() + " " + pkt.getSeqNb());
        } catch(Exception e){
            System.err.println("Couldn't add packet log to write buffer");
            e.printStackTrace();
        }
    }

    public void flush(){
        emptyBroadcastBuffer();
        emptyDeliverBuffer();
    }

    private void emptyBroadcastBuffer(){
        Integer elem;
        while((elem = broadcastBuffer.poll()) != null) writeBroadcast.accept(elem);
    }
    private void emptyDeliverBuffer(){
        String elem;
        while((elem = deliverBuffer.poll()) != null) writeDeliver.accept(elem);
    }

    @Override
    public void run() {
        while (true) {
            if(!broadcastBuffer.isEmpty()) emptyBroadcastBuffer();
            if(!deliverBuffer.isEmpty()) emptyDeliverBuffer();
        }
    }
}
