package cs451.Layer;

import cs451.Host;
import cs451.Operation;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;
import cs451.Writer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class FIFOBroadcast extends Layer {

    private final ConcurrentHashMap<Integer, Integer> next = new ConcurrentHashMap<>(); //Map<Host id, seq nb>
    // Use Map<simple Id, packet> because we don't want equality tested on
    // pktId but only on simpleId
    private final ConcurrentHashMap<String, PayloadPacket> pending = new ConcurrentHashMap<>();
    private final Writer writer;

    public FIFOBroadcast(int nbMessageToSend, Writer writer, Host myHost, List<Host> hosts,
                         Consumer<Packet> upperLayerDeliver) {
        this.upperLayerDeliver = upperLayerDeliver;
        this.writer = writer;
        for (Host host : hosts) next.put(host.getId(), 1);
        UniformReliableBroadcast urb = new UniformReliableBroadcast(nbMessageToSend, writer, myHost, hosts, this::deliver);
        new Thread(urb).start();

    }

    @Override
    public void deliver(Packet pkt) {
        delivered.offer(pkt);
        upperLayerDeliver.accept(pkt);
    }

    @Override
    public void run() {
        while(true){
            try {
                processPkt(delivered.take());
            } catch (InterruptedException e){
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processPkt(Packet pkt){
        PayloadPacket payloadPacket = (PayloadPacket)  pkt;
        pending.put(payloadPacket.getSimpleId(), payloadPacket);
        tryDelivering();
    }

    private void tryDelivering(){
        int hostId;
        boolean deliveredPacket;
        do {
            deliveredPacket = false;
            for (Map.Entry<String, PayloadPacket> entry : pending.entrySet()) {
                PayloadPacket pkt = entry.getValue();
                hostId = pkt.getOriginalSenderId();
                if (pkt.getSeqNb() == next.get(hostId)) {
//                    System.out.println("FIFO deliver " + pkt.getSimpleId());
                    writer.write(pkt, Operation.DELIVER);
                    next.put(hostId, next.get(hostId) + 1);
                    pending.remove(entry.getKey());
                    deliveredPacket = true;
                }
            }
        } while(deliveredPacket);
    }
}
