package cs451.Layer;

import cs451.Packet.AckPacket;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PerfectLink extends Layer {
    private ConcurrentSkipListSet<String> pktToBeAck =
            new ConcurrentSkipListSet<>(new PayloadPacket.PayloadPacketComparator());
    private ConcurrentHashMap<String, PayloadPacket> pktSent = new ConcurrentHashMap<>();
    private HashSet<String> pktReceived = new HashSet<>();
    private BlockingQueue<Packet> sendBuffer = new LinkedBlockingQueue<>();
    private FairLossLink link;

    public PerfectLink(int myPort, Consumer<Packet> upperLayerDeliver) {
        this.upperLayerDeliver = upperLayerDeliver;
        this.link = new FairLossLink(myPort, sendBuffer, this::deliver);
        new Thread(link).start();
        new Thread(this::startRetransmissions).start();
    }

    public PerfectLink(int myPort, Consumer<Packet> upperLayerDeliver,
                       ArrayList<PayloadPacket> broadcastPkt) {
        this(myPort, upperLayerDeliver);
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
        String pktId = pkt.getPktId();
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

    private void startRetransmissions(){
        final int WINDOW_SIZE = 50;
        // Set a periodic retransmission of packets not yet ack
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            if (pktToBeAck.isEmpty()) return;
            if (sendBuffer.size() > WINDOW_SIZE) return;

            int counter = 0; //Limit the number of retransmissions to WINDOW_SIZE

            Iterator<String> iter = pktToBeAck.iterator();
            PayloadPacket pkt;
            while (iter.hasNext() && counter < WINDOW_SIZE) {
                if ((pkt = pktSent.getOrDefault(iter.next(), null)) != null) {
                    sendPayload(pkt);
                    counter++;
                }
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }
}
