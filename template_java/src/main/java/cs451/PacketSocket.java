package cs451;

import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

abstract public class PacketSocket implements Runnable {
    DatagramSocket ds;
    Writer writer;
    volatile HashSet<String> pktToBeAck;
    BlockingQueue<Packet> sendBuffer;
    private HashSet<BlockingQueue<Packet>> listeners = new HashSet<>();

    public PacketSocket(DatagramSocket ds, Writer writer, HashSet<String> pktToBeAck,
                           BlockingQueue<Packet> sendBuffer){
        this.ds = ds;
        this.writer = writer;
        this.pktToBeAck = pktToBeAck;
        this.sendBuffer = sendBuffer;
    }


    //TODO remove for perf
    public void subscribe(BlockingQueue<Packet> listener){
        listeners.add(listener);
    }

    public void publish(Packet pkt){
        for (BlockingQueue<Packet> listener : listeners) listener.offer(pkt);
    }
}
