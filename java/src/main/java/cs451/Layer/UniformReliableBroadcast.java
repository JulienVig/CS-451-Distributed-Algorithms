package cs451.Layer;

import cs451.Host;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class UniformReliableBroadcast extends Layer{
    // At this layer pkt are identified by the long composed of originalSender and seqNumber
    // (so forwarding packets doesn't change its id)
    private HashSet<Long> alreadyDelivered = new HashSet<>();
    private HashSet<Long> pending = new HashSet<>();
    private HashMap<Long, HashSet<Integer>> ack = new HashMap<>(); // Map<pkt id, host id>
    private BestEffortBroadcast beb;
    private double quorum;

    public UniformReliableBroadcast(ArrayList<PayloadPacket> pktToBroadcast, Host myHost, List<Host> hosts,
                                    Consumer<Packet> upperLayerDeliver){
        quorum = hosts.size() / 2.0;
        this.upperLayerDeliver = upperLayerDeliver;
        for (PayloadPacket pkt : pktToBroadcast) setPending(pkt);
        beb = new BestEffortBroadcast(pktToBroadcast, myHost, hosts, this::deliver, this::setPending);
        new Thread(beb).start();
    }


    @Override
    public void deliver(Packet pkt) {
        delivered.offer(pkt);
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

    public void broadcast(PayloadPacket pkt){
        setPending(pkt);
        beb.broadcast(pkt);
    }



    // Method called when broadcasting the hosts' initial packets
    private void setPending(PayloadPacket pkt){
        long simpleId = pkt.getSimpleId();
        pending.add(simpleId);
//        ack.put(simpleId, new HashSet<>());
//        ack.get(simpleId).add(pkt.getSenderId());
    }

    private void processPkt(Packet pkt){
        // PerfectLink delivers only PayloadPacket
        PayloadPacket payloadPkt = (PayloadPacket) pkt;
        long simpleId = payloadPkt.getSimpleId();
        if(alreadyDelivered.contains(simpleId)) return;

        if (!ack.containsKey(simpleId)) {
            ack.put(simpleId, new HashSet<>());
//            ack.get(simpleId).add(myId);
        }
        ack.get(simpleId).add(payloadPkt.getSenderId());

        if(!pending.contains(simpleId)){
            pending.add(simpleId);
            beb.reBroadcast(payloadPkt);
        }
        tryDelivering(payloadPkt);
    }

    private void tryDelivering(PayloadPacket pkt){
        long simpleId = pkt.getSimpleId();
        if(pending.contains(simpleId) && ack.containsKey(simpleId)
                && ack.get(simpleId).size() > quorum - 1){ // - 1 because we deduce ourselves
//            System.out.println(Thread.currentThread().getId() + " URB deliver " + pkt.getSimpleId());
            alreadyDelivered.add(simpleId);
            ack.remove((simpleId));
            pending.remove(simpleId);
            upperLayerDeliver.accept(pkt);
        }
    }

}
