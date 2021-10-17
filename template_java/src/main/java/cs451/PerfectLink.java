package cs451;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;

public class PerfectLink implements Runnable {
    private int myPort;
    private String toIp;
    private int toPort;
    private volatile HashSet<String> pktToBeAck = new HashSet<>();
    private volatile HashMap<String, PayloadPacket> pktSent = new HashMap<>();
    private BlockingQueue<Packet> sendBuffer = new LinkedBlockingQueue<>();
    private DatagramSocket ds;
    private InputPacketSocket inputSocket;
    private OutputPacketSocket outputSocket;

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
        inputSocket = new InputPacketSocket(ds, writer, pktToBeAck, sendBuffer);
        outputSocket = new OutputPacketSocket(ds, writer, pktSent, pktToBeAck, sendBuffer);
        new Thread(inputSocket).start();
        new Thread(outputSocket).start();
    }

    public PerfectLink(int myPort, String toIp, int toPort, Writer writer, ArrayList<PayloadPacket> broadcastPkt) {
        this(myPort, toIp, toPort, writer);
        for (PayloadPacket pkt : broadcastPkt) send(pkt);
    }

    @Override
    public void run() {
        inputSocket.run();
    }

    private void send(PayloadPacket pkt) {
        outputSocket.sendPayloadAndLog(pkt);
    }

    @Override
    public String toString() {
        return "link from port " + myPort + " to IP " + toIp + " at port " + toPort;
    }

    public InputPacketSocket getInputSocket() {
        return inputSocket;
    }

    public OutputPacketSocket getOutputSocket() {
        return outputSocket;
    }
}
