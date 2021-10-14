package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PerfectLink implements Runnable {
    private int myPort;
    private String toIp;
    private int toPort;
    private ArrayList<String> writeBuffer;
    volatile HashSet<String> pktToBeAck = new HashSet<>();
    volatile HashSet<String> pktReceived = new HashSet<>();
    volatile HashMap<String, PayloadPacket> pktSent = new HashMap<>();
    private DatagramSocket ds;

    public PerfectLink(int myPort, String toIp, int toPort, ArrayList<String> writeBuffer){
        this.myPort = myPort;
        this.toIp = toIp;
        this.toPort = toPort;
        this.writeBuffer = writeBuffer;
        try {
            ds = new DatagramSocket(myPort);
        } catch(SocketException e){
            System.err.println("Could not initialize socket");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Set a periodic retransmission of packets not yet ack
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Retransmit " + pktToBeAck);
                if (pktToBeAck.isEmpty()) return;
                for (String pktId : pktToBeAck){
                    PayloadPacket pkt = pktSent.getOrDefault(pktId, null);
                    System.out.println("Found " + pkt);

                    if (pkt != null){
                        sendPayload(pkt);
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
        receive();
    }

    public boolean sendPayload(PayloadPacket pkt) {
        pktToBeAck.add(pkt.getPktId());
        pktSent.put(pkt.getPktId(), pkt);
        return send(pkt);
    }

    private boolean send(Packet pkt){
        try {
            InetAddress ip = InetAddress.getByName(pkt.receiverHost.getIp());
            DatagramPacket dp = new DatagramPacket(pkt.getBytes(), pkt.length(), ip, pkt.receiverHost.getPort());
            ds.send(dp);
            System.out.println("Sent '" + pkt + "'");
            return true;
        } catch (Exception e) {
            System.err.println("Exception while sending " + pkt + " through " + this);
            e.printStackTrace();
            return false;
        }
    }

    private void receive(){
        try {
            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket dp = new DatagramPacket(buf, 1024);
                ds.receive(dp);

                new Responder(dp.getData()).run();
//                new Thread(new Responder(dp.getData())).start();
//                "thread_" + Thread.currentThread().getId()).start();
//                System.out.println(pktReceived);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return "link from port " + myPort + " to IP " + toIp + " at port " + toPort;
    }

    /**
     * Inspired from https://stackoverflow.com/a/777209
     */
    private class Responder implements Runnable {
        private byte[] bytes;

        public Responder(byte[] bytes) {
            this.bytes = bytes;
        }

        public void run() {
            Packet pkt = deserializePkt(bytes);
            assert pkt instanceof PayloadPacket || pkt instanceof AckPacket;
            System.out.println("Received: " + pkt);
            if (pkt instanceof PayloadPacket) {
                PayloadPacket payloadPkt = (PayloadPacket) pkt;
                if (!pktReceived.contains(payloadPkt.getPktId())) {
                    //If pkt not already processed in the past
                    pktReceived.add(payloadPkt.getPktId());
                    writeBuffer.add(payloadPkt.toExpectedFormat()); // Format: sender_id seq_nb
                    sendAck(payloadPkt);
                }
            } else {
                pktToBeAck.remove(((AckPacket) pkt).getPayloadPktId());
            }
        }

        private void sendAck(PayloadPacket pkt){
            AckPacket ackPkt = new AckPacket(pkt);
            while(!send(ackPkt)){
                System.out.println("Waiting 1s");
                try{
                    TimeUnit.SECONDS.sleep(1);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        private Packet deserializePkt(byte[] bytes){
            Packet pkt = null;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream in = new ObjectInputStream(bis)) {
                pkt = (Packet) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Could not deserialize packet");
                e.printStackTrace();
            }
            return pkt;
        }
    }
}
