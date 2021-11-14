package cs451.Layer;

import cs451.Host;
import cs451.Packet.AckPacket;
import cs451.Packet.Packet;
import cs451.Packet.PacketType;
import cs451.Packet.PayloadPacket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class FairLossLink extends Layer{
    private DatagramSocket ds;
    private BlockingQueue<Packet> sendBuffer;
    private HashMap<Integer, Host> hostIdMapping = new HashMap<>();
    private Consumer<Integer> pollRetransmissions;
    private final int WINDOW_SIZE = 50;

    public FairLossLink(int myPort, List<Host> hosts, BlockingQueue<Packet> sendBuffer,
                        Consumer<Packet> upperLayerDeliver, Consumer<Integer> pollRetransmissions) {
        try {
            this.ds = new DatagramSocket(myPort);
        } catch (SocketException e) {
            System.err.println("Could not initialize socket");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        this.pollRetransmissions = pollRetransmissions;
        for (Host host : hosts) hostIdMapping.put(host.getId(), host);
        this.sendBuffer = sendBuffer;
        this.upperLayerDeliver = upperLayerDeliver;
        new Thread(this::flushSendBuffer).start();
    }

    @Override
    public void run() {
        try {
            byte[] buf;
            while (true) {
                buf = new byte[512];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
                upperLayerDeliver.accept(deserializePkt(dp.getData()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void deliver(Packet pkt) {
        return;
    }

    private void flushSendBuffer() {
        while (true) {
            try {
                sendPacket(sendBuffer.take());
                if (sendBuffer.size() < WINDOW_SIZE) pollRetransmissions.accept(WINDOW_SIZE);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendPacket(Packet pkt) {
        try {
            InetAddress ip = InetAddress.getByName(hostIdMapping.get(pkt.getReceiverId()).getIp());
            DatagramPacket dp = new DatagramPacket(pkt.getBytes(), pkt.length(), ip,
                                                    hostIdMapping.get(pkt.getReceiverId()).getPort());
            ds.send(dp);

        } catch (Exception e) {
            System.err.println("Exception while sending " + pkt + " through " + this);
            e.printStackTrace();
        }
    }

    private Packet deserializePkt(byte[] bytes){
        return bytes[0] == (byte) 1 ? PayloadPacket.deserializeToObject(bytes) : AckPacket.deserializeToObject(bytes);
    }

}
