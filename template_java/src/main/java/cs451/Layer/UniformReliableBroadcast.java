package cs451.Layer;

import cs451.Host;
import cs451.Operation;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;
import cs451.Writer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class UniformReliableBroadcast extends Layer{
    // At this layer pkt are identified by the string "originalSender seqNumber"
    // (so forwarding packets doesn't change its id)
    private HashSet<String> alreadyDelivered = new HashSet<>();
    private HashSet<String> pending = new HashSet<>();
    private HashMap<String, HashSet<Integer>> ack = new HashMap<>(); // Map<pkt id, host id>
    private BestEffortBroadcast beb;
    private Writer writer;
    private double quorum;
    private int myId;

    public UniformReliableBroadcast(int nbMessageToSend, Writer writer, Host myHost, List<Host> hosts){
        quorum = hosts.size() / 2.0;
        this.writer = writer;
        beb = new BestEffortBroadcast(nbMessageToSend, writer, myHost, hosts, this::deliver, this::broadcast);
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
            }
        }
    }


    // Method called when broadcasting the hosts' initial packets
    private void broadcast(PayloadPacket pkt){
        String simpleId = pkt.getSimpleId();
        pending.add(simpleId);
//        ack.put(simpleId, new HashSet<>());
//        ack.get(simpleId).add(pkt.getSenderId());
    }

    private void processPkt(Packet pkt){
        // PerfectLink delivers only PayloadPacket
        PayloadPacket payloadPkt = (PayloadPacket) pkt;
        String simpleId = payloadPkt.getSimpleId();
        if(alreadyDelivered.contains(simpleId)) return;

        if (!ack.containsKey(simpleId)) {
            ack.put(simpleId, new HashSet<>());
//            ack.get(simpleId).add(myId);
        }
        ack.get(simpleId).add(payloadPkt.getSenderId());

        if(!pending.contains(simpleId)){
            pending.add(simpleId);
            beb.broadcast(payloadPkt);
        }
        tryDelivering(payloadPkt);
    }

    private void tryDelivering(PayloadPacket pkt){
        String simpleId = pkt.getSimpleId();
        if(pending.contains(simpleId) && ack.containsKey(simpleId)
                && ack.get(simpleId).size() > quorum - 1){ // - 1 because we deduce ourselves
            alreadyDelivered.add(simpleId);
            ack.remove((simpleId));
            pending.remove(simpleId);
            writer.write(pkt, Operation.DELIVER);
            System.out.println("URB deliver " + pkt.getSimpleId());
        }
    }

}
