package cs451.Layer;

import cs451.Host;
import cs451.Packet.AckPacket;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PerfectLink extends Layer {
    private final ConcurrentSkipListSet<Long> pktToBeAck = new ConcurrentSkipListSet<>();
//    private final Set<PayloadPacket> pktToBeAck = Collections.newSetFromMap(new ConcurrentHashMap<>()); //ConcurrentHashSet
    private final ConcurrentHashMap<Long, PayloadPacket> pktSent = new ConcurrentHashMap<>();

    //Needs to be a map and not a set to be able to remove packets given the pktId in an AckPacket
//    private final ConcurrentHashMap<Long, PayloadPacket> pktToBeAck = new ConcurrentHashMap<>();
    private final HashSet<Long> pktReceived = new HashSet<>();
    private final ConcurrentLinkedDeque<Packet> sendBuffer = new ConcurrentLinkedDeque<>();

    public PerfectLink(int myPort, List<Host> hosts, Consumer<Packet> upperLayerDeliver) {
        this.upperLayerDeliver = upperLayerDeliver;
        FairLossLink link = new FairLossLink(myPort, hosts, sendBuffer, this::deliver);
        new Thread(link).start();
        startRetransmissions();
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
            long pktId = ackPacket.getPayloadPktId();
            pktToBeAck.remove(pktId);
            pktSent.remove(pktId);
        }
    }

    private void sendAck(PayloadPacket pkt){
        AckPacket ackPkt = new AckPacket(pkt);
        sendBuffer.addFirst(ackPkt);
    }

    private void startRetransmissions() {
        final int WINDOW_SIZE = 300;
        // Set a periodic retransmission of packets not yet ack
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            try {
                if (pktToBeAck.isEmpty() | sendBuffer.size() > WINDOW_SIZE) return;

                int counter = 0; //Limit the number of retransmissions to WINDOW_SIZE
                Iterator<Long> iter = pktToBeAck.iterator();
                PayloadPacket pkt;
                while (iter.hasNext() && counter < WINDOW_SIZE) {
                    if ((pkt = pktSent.getOrDefault(iter.next(), null)) != null) {
                        sendBuffer.addLast(pkt);
                        counter++;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }
}
