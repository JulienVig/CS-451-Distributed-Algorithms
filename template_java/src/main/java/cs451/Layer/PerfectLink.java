package cs451.Layer;

import cs451.Host;
import cs451.Packet.AckPacket;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PerfectLink extends Layer {
    private final ConcurrentSkipListSet<Long> pktToBeAck = new ConcurrentSkipListSet<>(); //ConcurrentHashSet
//    private final Set<PayloadPacket> pktToBeAck = Collections.newSetFromMap(new ConcurrentHashMap<>()); //ConcurrentHashSet
    private final ConcurrentHashMap<Long, PayloadPacket> pktSent = new ConcurrentHashMap<>(); //ConcurrentHashSet

    //Needs to be a map and not a set to be able to remove packets given the pktId in an AckPacket
//    private final ConcurrentHashMap<Long, PayloadPacket> pktToBeAck = new ConcurrentHashMap<>();
    private final HashSet<Long> pktReceived = new HashSet<>();
    private final BlockingQueue<Packet> sendBuffer = new LinkedBlockingQueue<>();

    public PerfectLink(int myPort, List<Host> hosts, Consumer<Packet> upperLayerDeliver) {
        this.upperLayerDeliver = upperLayerDeliver;
        FairLossLink link = new FairLossLink(myPort, hosts, sendBuffer, this::deliver, this::pollRetransmissions);
        new Thread(link).start();
    }

    public PerfectLink(int myPort, List<Host> hosts, Consumer<Packet> upperLayerDeliver,
                       ArrayList<PayloadPacket> broadcastPkt) {
        this(myPort, hosts, upperLayerDeliver);
        for (PayloadPacket pkt : broadcastPkt) sendPayload(pkt);
    }

    @Override
    public void deliver(Packet pkt) {
        try {
            delivered.put(pkt);
        } catch(InterruptedException e){
            System.err.println("Couldn't add packet to receiveBuffer queue");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Packet receivedPkt = delivered.take();
                processPacket(receivedPkt);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendPayload(PayloadPacket pkt) {
        try {
            sendBuffer.put(pkt);
        } catch (InterruptedException e) {
            System.err.println("Couldn't add packet to sendBuffer queue");
            e.printStackTrace();
        }
        long pktId = pkt.getPktId();
        pktToBeAck.add(pktId);
        pktSent.put(pktId, pkt);
    }

    @Override
    public String toString() {
        return "perfect link";
    }

    private void processPacket(Packet pkt){
        if (pkt instanceof PayloadPacket) {
            PayloadPacket payloadPkt = (PayloadPacket) pkt;
            sendAck(payloadPkt); // Always send ack when receiving a payload packet
            if (!pktReceived.contains(payloadPkt.getPktId())) {
                //If pkt not already processed in the past
                pktReceived.add(payloadPkt.getPktId());
                upperLayerDeliver.accept(payloadPkt);
            }
        } else {
            AckPacket ackPacket = (AckPacket) pkt;
            pktToBeAck.remove(ackPacket.getPayloadPktId());
            pktSent.remove(ackPacket.getPayloadPktId());
        }
    }

    private void sendAck(PayloadPacket pkt){
        AckPacket ackPkt = new AckPacket(pkt);
        try {
            sendBuffer.put(ackPkt);
        } catch(InterruptedException e){
            System.err.println("Couldn't add packet to sendBuffer queue");
            e.printStackTrace();
        }
    }

    private void pollRetransmissions(int windowSize){
        try {
            if (pktToBeAck.isEmpty()) return;

            int counter = 0; //Limit the number of retransmissions to WINDOW_SIZE
            Iterator<Long> iter = pktToBeAck.iterator();
            PayloadPacket pkt;
            while (iter.hasNext() && counter < windowSize) {
                if((pkt = pktSent.getOrDefault(iter.next(), null)) != null){
                    sendPayload(pkt);
                    counter++;
                }
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
    }
}
