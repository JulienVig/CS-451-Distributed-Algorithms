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
import java.util.concurrent.*;

public class PerfectLink implements Runnable {
    private int myPort;
    private String toIp;
    private int toPort;
    volatile HashSet<String> pktToBeAck = new HashSet<>();
    volatile HashMap<String, PayloadPacket> pktSent = new HashMap<>();
    private BlockingQueue<Packet> sendBuffer = new LinkedBlockingQueue<>();
    private DatagramSocket ds;
    private InputSocket inputSocket;
    private OutputSocket outputSocket;

    public PerfectLink(int myPort, String toIp, int toPort, Writer writer) {
        this.myPort = myPort;
        this.toIp = toIp;
        this.toPort = toPort;
        try {
            ds = new DatagramSocket(myPort);
        } catch (SocketException e) {
            System.err.println("Could not initialize socket");
            e.printStackTrace();
        }
        inputSocket = new InputSocket(ds, writer, pktToBeAck, sendBuffer);
        outputSocket = new OutputSocket(ds, writer, pktSent, pktToBeAck, sendBuffer);
        new Thread(inputSocket).start();
        new Thread(outputSocket).start();
    }

    @Override
    public void run() {
        // Set a periodic retransmission of packets not yet ack
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (pktToBeAck.isEmpty()) return;
                System.out.println("Retransmit " + pktToBeAck);
                for (String pktId : pktToBeAck) {
                    PayloadPacket pkt = pktSent.getOrDefault(pktId, null);

                    if (pkt != null) {
                        outputSocket.sendPayload(pkt);
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
        inputSocket.receive();
    }

    public void send(PayloadPacket pkt) {
        outputSocket.sendPayloadAndLog(pkt);
    }

    @Override
    public String toString() {
        return "link from port " + myPort + " to IP " + toIp + " at port " + toPort;
    }

}
