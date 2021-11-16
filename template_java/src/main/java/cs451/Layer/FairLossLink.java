package cs451.Layer;

import cs451.Host;
import cs451.Main;
import cs451.Packet.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class FairLossLink extends Layer{
    private DatagramSocket ds;
    private BlockingDeque<Packet> sendBuffer;
    private Host[] hostIdMapping;

    public FairLossLink(int myPort, List<Host> hosts, BlockingDeque<Packet> sendBuffer,
                        Consumer<Packet> upperLayerDeliver) {
        try {
            this.ds = new DatagramSocket(myPort);
        } catch (SocketException e) {
            System.err.println("Could not initialize socket");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        hostIdMapping = new Host[hosts.size()];
        for (Host host : hosts) hostIdMapping[host.getId() - 1] = host;
        this.sendBuffer = sendBuffer;
        this.upperLayerDeliver = upperLayerDeliver;
        new Thread(this::flushSendBuffer).start();
    }

    @Override
    public void run() {
        try {
            byte[] buf;
            while (true) {
                buf = new byte[BatchPacket.BYTE_CAPACITY];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
//                if(Main.TC &&  Math.random() <=.25) return; //Drop 25% of pkts
                upperLayerDeliver.accept(BatchPacket.deserializeToObject(dp.getData()));
//                System.out.println("FL deliver");
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
                sendBatch(sendBuffer.takeFirst());
//                if (sendBuffer.size() < WINDOW_SIZE) pollRetransmissions.accept(WINDOW_SIZE);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendBatch(Packet firstPkt){
        BatchPacket batch = new BatchPacket(firstPkt);
        int receiverId = firstPkt.getReceiverId();

        Iterator<Packet> iter = sendBuffer.iterator();
        Packet pkt;
        while(iter.hasNext() && !batch.isFull()){
            pkt = iter.next();
            if (pkt.getReceiverId() == receiverId) {
                batch.addPacket(pkt);
                iter.remove();
            }
        }
        sendPacket(batch);
    }

    private void sendPacket(Packet pkt) {
        try {
            Host receiver = hostIdMapping[pkt.getReceiverId() - 1];
            DatagramPacket dp = new DatagramPacket(pkt.getBytes(), pkt.length(),
                    receiver.getInetAddress(), receiver.getPort());
            ds.send(dp);
        } catch (Exception e) {
            System.err.println("Exception while sending " + pkt + " through " + this);
            e.printStackTrace();
        }
    }
//
//    private Packet deserializePkt(byte[] bytes){
//        return bytes[0] == (byte) 1 ? PayloadPacket.deserializeToObject(bytes) : AckPacket.deserializeToObject(bytes);
//    }

}
