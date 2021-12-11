package cs451.Layer;

import cs451.Host;
import cs451.Operation;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;
import cs451.Writer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LCBBroadcast extends Layer {

    private final ArrayList<Integer>[] hostDep;
    private final int[] next; // one entry per host
    //One priority queue per host, where PriorityQueues' comparator uses pktSimpleId
    private final PriorityQueue<PayloadPacket>[] pending;
    private final Writer writer;
    private Consumer<Packet> upperLayerDeliver;
    private int nbMessageSent = 1;
    private int nbMessageToSend;
    private UniformReliableBroadcast urb;

    private Host myHost;
    private List<Host> hosts;

    private static final int NB_BATCH_BROADCAST = 10000;

    public LCBBroadcast(int nbMessageToSend, Writer writer, Host myHost, List<Host> hosts,
                        ArrayList[] hostDep, Consumer<Packet> upperLayerDeliver) {
        this.nbMessageToSend = nbMessageToSend;
        this.upperLayerDeliver = upperLayerDeliver;
        this.myHost = myHost;
        this.hosts = hosts;
        this.writer = writer;
        next = new int[hosts.size()];
        this.hostDep = hostDep;
        pending = new PriorityQueue[hosts.size()];
        // Comparator on simpleId because we don't want equality tested on pktId
        for (int i = 0; i < pending.length; i++) pending[i] = new PriorityQueue<>(Comparator.comparing(PayloadPacket::getSimpleId));
        for (Host host : hosts) next[host.getId() - 1] = 1;

        ArrayList<PayloadPacket> pktToBroadcast = createBroadcastBatch();
        urb = new UniformReliableBroadcast(pktToBroadcast, myHost, hosts, this::deliver);
        new Thread(urb).start();
    }

    private ArrayList<PayloadPacket> createBroadcastBatch(){
        ArrayList<PayloadPacket> broadcastPkt = new ArrayList<>();
        boolean pktAlreadyLogged = false;
        int prevNbMessageSent = nbMessageSent;
        for(Host host: hosts) {
            if (host != myHost) {
                int i  = prevNbMessageSent;
                //Stop when sent all the messages or reached limit of broadcast messages at once
                while(i <= nbMessageToSend && i - prevNbMessageSent < NB_BATCH_BROADCAST){
                    PayloadPacket pkt = new PayloadPacket(i++, myHost.getId(), host.getId(), next);
                    broadcastPkt.add(pkt);
                    //Only log broadcast once per packet
                    if (!pktAlreadyLogged){
//                        System.out.println("Broadcast " + pkt.getSimpleId());
                        writer.write(pkt, Operation.BROADCAST);
                        nbMessageSent = i; //Update it once here
                    }
                }
                pktAlreadyLogged = true;
            }
        }
        return broadcastPkt;
    }


    @Override
    public void deliver(Packet pkt) {
        delivered.offer(pkt);
        if (upperLayerDeliver != null) upperLayerDeliver.accept(pkt);
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

    private boolean canBeDelivered(PayloadPacket pkt, int hostIdx){
        if(pkt.getSeqNb() != next[hostIdx]) return false;
        for (int dep : hostDep[hostIdx]) if (pkt.getClock()[dep - 1] > next[dep - 1]) return false;
        return true;
    }

    private void processPkt(Packet pkt){
        PayloadPacket payloadPacket = (PayloadPacket)  pkt;
        int hostIdx = payloadPacket.getOriginalSenderId() - 1;
        pending[hostIdx].add(payloadPacket);
        if(canBeDelivered(pending[hostIdx].peek(), hostIdx)) tryDelivering(hostIdx);
    }

    private void tryDelivering(int hostIdx){
        //The first packet is always the smallest seq number
        do {
//            PayloadPacket pkt = pending[hostIdx].poll();
//                System.out.println(Thread.currentThread().getId() + " FIFO deliver " + pkt.getSimpleId());
            writer.write(pending[hostIdx].poll(), Operation.DELIVER);
            next[hostIdx]++;
        } while(pending[hostIdx].size() > 0 && canBeDelivered(pending[hostIdx].peek(), hostIdx));
        if(nbMessageSent < nbMessageToSend) {
            for (PayloadPacket pkt : (createBroadcastBatch())) urb.broadcast(pkt);
        }
    }

}
