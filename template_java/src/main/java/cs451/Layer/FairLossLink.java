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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class FairLossLink extends Layer{
    private DatagramSocket ds;
    private BlockingQueue<Packet> sendBuffer;
    private HashMap<Integer, Host> hostIdMapping = new HashMap<>();

    public FairLossLink(int myPort, List<Host> hosts, BlockingQueue<Packet> sendBuffer,
                        Consumer<Packet> upperLayerDeliver) {
        try {
            this.ds = new DatagramSocket(myPort);
        } catch (SocketException e) {
            System.err.println("Could not initialize socket");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
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
        Packet pkt = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream in = new ObjectInputStream(bis)) {
            pkt = (Packet) in.readObject();
//            if (in.readObject() == PacketType.PAYLOAD) pkt = PayloadPacket.deserializePkt(in);
//            else pkt = AckPacket.deserializePkt(in);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not deserialize packet");
            e.printStackTrace();
        }
        return pkt;
    }

}
