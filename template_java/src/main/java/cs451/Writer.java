package cs451;

import cs451.Packet.PayloadPacket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static cs451.Operation.BROADCAST;
import static cs451.Operation.DELIVER;


public class Writer implements Runnable{
    private BlockingQueue<OperationLog> buffer = new LinkedBlockingQueue<>();
    private Consumer<Integer> writeBroadcast;
    private Consumer<String> writeDeliver;

    public Writer(Consumer<Integer> writeBroadcast, Consumer<String> writeDeliver) {
        this.writeBroadcast = writeBroadcast;
        this.writeDeliver = writeDeliver;
    }

    public void write(PayloadPacket pkt, Operation op){
        try {
            OperationLog log = null;
            if (op == BROADCAST) log = new OperationLog(BROADCAST, pkt.getSeqNb());
            else if (op == DELIVER)  log = new OperationLog(DELIVER, pkt.getOriginalSenderId() + " " + pkt.getSeqNb());
            else System.err.println("Unrecognized write Operation type: " + op);
            
            if (log != null) buffer.add(log);
        } catch(Exception e){
            System.err.println("Couldn't add packet log to write buffer");
            e.printStackTrace();
        }
    }

    public void flush(){
            try {
                OperationLog log = buffer.take();
                if (log.getType() == BROADCAST) writeBroadcast.accept(log.getIntContent());
                else if (log.getType() == DELIVER) writeDeliver.accept(log.getContent());
                else System.err.println("Unrecognized OperationLog type: " + log.getType());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void run() {
        while (true) flush();
    }
}
