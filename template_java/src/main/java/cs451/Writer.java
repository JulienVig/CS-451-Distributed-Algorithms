package cs451;

import cs451.Packet.PayloadPacket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static cs451.Operation.BROADCAST;
import static cs451.Operation.DELIVER;


public class Writer{
//    private BlockingQueue<OperationLog> buffer = new LinkedBlockingQueue<>();
    private Consumer<String> writeToOutput;
    private StringBuilder builder = new StringBuilder(10000);

    public Writer(Consumer<String> writeToOutput) {
        this.writeToOutput = writeToOutput;
    }

    public void write(PayloadPacket pkt, Operation op){
        try {
            if (builder.length() != 0) builder.append(System.getProperty("line.separator"));
            if (op == BROADCAST) builder.append("b "+ pkt.getSeqNb());
            else if (op == DELIVER)  builder.append("d "+ pkt.getOriginalSenderId() + " " + pkt.getSeqNb());
            else System.err.println("Unrecognized write Operation type: " + op);

        } catch(Exception e){
            System.err.println("Couldn't add packet log to write buffer");
            e.printStackTrace();
        }
    }

//    @Override
//    public void run() {
//        while (true) emptyBuffer();
//    }

    public void flush(){
        writeToOutput.accept(builder.toString());
    }

//    private void emptyBuffer(){
//            try {
//                writeLog(buffer.take());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                Thread.currentThread().interrupt();
//            }
//    }
//
//    private void writeLog(OperationLog log){
//        if (log.getType() == BROADCAST) writeBroadcast.accept(log.getIntContent());
//        else if (log.getType() == DELIVER) writeDeliver.accept(log.getContent());
//        else System.err.println("Unrecognized OperationLog type: " + log.getType());
//    }
}
