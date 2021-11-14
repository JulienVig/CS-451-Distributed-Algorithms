package cs451;

import cs451.Packet.PayloadPacket;

import java.util.concurrent.*;
import java.util.function.Consumer;

import static cs451.Operation.BROADCAST;
import static cs451.Operation.DELIVER;


public class Writer implements Runnable{
    private BlockingQueue<OperationLog> buffer = new LinkedBlockingQueue<>();
    private final Consumer<String> writeToOutput;
    private final StringBuilder builder = new StringBuilder(10000); //around 1500 lines
    private final int BUILDER_LIMIT = builder.capacity() - 100;

    public Writer(Consumer<String> writeToOutput) {
        this.writeToOutput = writeToOutput;
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
        if (builder.length() == 0) return;
        if (String.valueOf(builder.charAt(builder.length() - 1))
                .equals(System.getProperty("line.separator"))){
            builder.setLength(builder.length() - 1); //remove last newline char
        }
        writeToOutput.accept(builder.toString());
    }

    @Override
    public void run() {
        while (true) {
            try {
                writeToBuilder(buffer.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void writeToBuilder(OperationLog log){
        try {
            builder.append((log.getType() == BROADCAST) ? "b " : "d ");
            builder.append(log.getContent());
            if (builder.length() >= BUILDER_LIMIT) emptyBuilder();
            else builder.append(System.getProperty("line.separator"));
        } catch(Exception e){
            System.err.println("Couldn't add packet log to string builder");
            e.printStackTrace();
        }
    }

    private void emptyBuilder(){
        String logs = builder.toString();
        builder.setLength(0);
        writeToOutput.accept(logs);

    }
}
