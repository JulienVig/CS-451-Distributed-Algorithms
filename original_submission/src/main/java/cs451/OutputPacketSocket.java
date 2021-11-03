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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OutputPacketSocket extends PacketSocket {
    private volatile HashMap<String, PayloadPacket> pktSent;

    public OutputPacketSocket(DatagramSocket ds, Writer writer, LinkedHashMap<String, PayloadPacket> pktSent,
                              LinkedHashSet<String> pktToBeAck, BlockingQueue<Packet> sendBuffer) {
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
            System.out.println("Try to retransmit");
            if (pktToBeAck.isEmpty()) {
                System.out.println("No more packets to be ack");
                return;
            }

            System.out.println("Before send buffer");
            if (sendBuffer.size() > 50) {
                System.out.println("Don't retransmit because sendBuffer already congested");
                return;
            }
            System.out.println("After sendBuffer");
            System.out.println("Retransmit " + pktToBeAck);
            // for (int i = 0; i < Math.min(WINDOW_SIZE, pktToBeAck.size()); i ++){
            int counter = 0;
            int WINDOW_SIZE = 50;
            for (String pktId : pktToBeAck) {
                PayloadPacket pkt = pktSent.getOrDefault(pktId, null);
                if (pkt != null) {
                    sendPayload(pkt);
                    if(++counter > WINDOW_SIZE) {
                        System.out.println("Exceeded window size");
                        break;
                    }
                }
            }
            System.out.println("End retransmit");
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void sendPayloadAndLog(PayloadPacket pkt) {
        writer.write(pkt, Operation.BROADCAST);
        sendPayload(pkt);
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

    private void send(Packet pkt) {
        System.out.println("Send " + pkt);
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
