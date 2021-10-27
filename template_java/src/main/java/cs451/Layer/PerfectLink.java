package cs451.Layer;

import cs451.Operation;
import cs451.Packet.AckPacket;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;
import cs451.Writer;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PerfectLink extends Layer {
    private volatile LinkedHashSet<String> pktToBeAck = new LinkedHashSet<>();
    private volatile LinkedHashMap<String, PayloadPacket> pktSent = new LinkedHashMap<>();
    private volatile HashSet<String> pktReceived = new HashSet<>();
    private BlockingQueue<Packet> sendBuffer = new LinkedBlockingQueue<>();
    private Consumer<Packet> upperLayerDeliver;
    private Writer writer;
    private FairLossLink link;

    public PerfectLink(int myPort, Writer writer, Consumer<Packet> upperLayerDeliver) {
        this.writer = writer;
        this.upperLayerDeliver = upperLayerDeliver;
        this.link = new FairLossLink(myPort, sendBuffer, this::deliver);
        new Thread(link).start();
        new Thread(this::startRetransmissions).start();
    }

    public PerfectLink(int myPort, Writer writer, Consumer<Packet> upperLayerDeliver,
                       ArrayList<PayloadPacket> broadcastPkt) {
        this(myPort, writer, upperLayerDeliver);
        for (PayloadPacket pkt : broadcastPkt) sendPayload(pkt);
    }

    @Override
    public void deliver(Packet pkt) {
        try {
            delivered.put(pkt);
//            System.out.println(Thread.currentThread().getId()  +" PL delivered:" + pkt);
        } catch(InterruptedException e){
            System.err.println("Couldn't add packet to receiveBuffer queue");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
//                System.out.println(Thread.currentThread().getId()  +" delivered " + delivered);
                Packet receivedPkt = delivered.take();
//                System.out.println(Thread.currentThread().getId()  +" consumed: " + receivedPkt);
                processPacket(receivedPkt);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        pktToBeAck.add(pkt.getPktId());
        pktSent.put(pkt.getPktId(), pkt);
    }

    @Override
    public String toString() {
        return "perfect link";
    }

    private void processPacket(Packet pkt){
//            assert pkt instanceof PayloadPacket || pkt instanceof AckPacket;
//        System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": received " + pkt);
        if (pkt instanceof PayloadPacket) {
            PayloadPacket payloadPkt = (PayloadPacket) pkt;
            sendAck(payloadPkt); // Always send ack when receiving a payload packet
            if (!pktReceived.contains(payloadPkt.getPktId())) {
                //If pkt not already processed in the past
                pktReceived.add(payloadPkt.getPktId());
//                System.out.println(Thread.currentThread().getId()  +"Received " + payloadPkt);
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
        // Set a periodic retransmission of packets not yet ack
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            if (pktToBeAck.isEmpty()) return;
//            System.out.println("Retransmit " + pktToBeAck);
            for (String pktId : pktToBeAck) {
                PayloadPacket pkt = pktSent.getOrDefault(pktId, null);
                if (pkt != null) {
                    sendPayload(pkt);
                }
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }
}
