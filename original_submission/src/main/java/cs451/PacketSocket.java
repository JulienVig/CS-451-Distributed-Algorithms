package cs451;

import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

abstract public class PacketSocket implements Runnable {
    DatagramSocket ds;
    Writer writer;
    volatile ConcurrentLinkedQueue<String> pktToBeAck;
    BlockingQueue<Packet> sendBuffer;
    private HashSet<BlockingQueue<Packet>> listeners = new HashSet<>();

    public PacketSocket(DatagramSocket ds, Writer writer, ConcurrentLinkedQueue<String> pktToBeAck,
                           BlockingQueue<Packet> sendBuffer){
        this.ds = ds;
        this.writer = writer;
        this.pktToBeAck = pktToBeAck;
        this.sendBuffer = sendBuffer;
    }


    //TODO remove for perf
//    public void subscribe(BlockingQueue<Packet> listener){
//        listeners.add(listener);
//    }

//    public void publish(Packet pkt){
//        for (BlockingQueue<Packet> listener : listeners) listener.offer(pkt);
//    }
}
