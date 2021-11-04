package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;

public class OutputPacketSocket extends PacketSocket {
    private volatile HashMap<String, PayloadPacket> pktSent;

    public OutputPacketSocket(DatagramSocket ds, Writer writer, LinkedHashMap<String, PayloadPacket> pktSent,
                              ConcurrentLinkedQueue<String> pktToBeAck, BlockingQueue<Packet> sendBuffer) {
        super(ds, writer, pktToBeAck, sendBuffer);
        this.pktSent = pktSent;
    }

    @Override
    public void run() {
        startRetransmissions();
        while (true) {
            try {
                send(sendBuffer.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
//                Thread.currentThread().interrupt();
            }
        }
    }

    private void startRetransmissions(){
        // Set a periodic retransmission of packets not yet ack
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            // System.out.println("Try to retransmit");
            // System.out.println("Pkt to be ack " + pktToBeAck.size());
            // System.out.println("sendbuffer size " + sendBuffer.size());
            // System.out.println("pktSent size " + pktSent.size());
            System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": start retransmit");
            if (pktToBeAck.isEmpty()) {
                System.out.println("No more packets to be ack");
                return;
            }

            // System.out.println("Before send buffer");
            if (sendBuffer.size() > 50) {
                System.out.println("Don't retransmit because sendBuffer already congested");
                return;
            }
            // System.out.println("After sendBuffer");
            
            int counter = 0;
            int WINDOW_SIZE = 50;
            Iterator<String> it = pktToBeAck.iterator();
            try{
            while(it.hasNext() && counter < WINDOW_SIZE){
                System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": start retransmit iteration " + counter);
                String pktId = it.next();
                // System.out.println("1");
                PayloadPacket pkt = pktSent.getOrDefault(pktId, null);
                // System.out.println("2");
                if (pkt != null) {
                    // System.out.println("3");
                    try {
                        sendBuffer.put(pkt);
                    } catch (InterruptedException e) {
                        System.err.println("Couldn't add packet to sendBuffer queue");
                        e.printStackTrace();
                    }
                    // System.out.println("4");
                    counter ++;
                }
            }
            } catch (Throwable e){
                e.printStackTrace();
            }

            // System.out.println("5");
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void sendPayloadAndLog(PayloadPacket pkt) {
        writer.write(pkt, Operation.BROADCAST);
        sendPayload(pkt);
    }

    public void sendPayload(PayloadPacket pkt) {
        // System.out.println("Enter sendPayload");
        try {
            sendBuffer.put(pkt);
        } catch (InterruptedException e) {
            System.err.println("Couldn't add packet to sendBuffer queue");
            e.printStackTrace();
        }
        if (!pktToBeAck.contains(pkt.getPktId())) pktToBeAck.add(pkt.getPktId());
        pktSent.put(pkt.getPktId(), pkt);
        // System.out.println("Out sendPayload");
    }

    private void send(Packet pkt) {
        // System.out.println("Send " + pkt);
        try {
            InetAddress ip = InetAddress.getByName(pkt.receiverHost.getIp());
            DatagramPacket dp = new DatagramPacket(pkt.getBytes(), pkt.length(), ip, pkt.receiverHost.getPort());
            ds.send(dp);
//            System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": sent '" + pkt + "'");
//            publish(pkt); //TODO remove for perf
        } catch (Exception e) {
            System.err.println("Exception while sending " + pkt + " through " + this);
            e.printStackTrace();
        }
    }
}
