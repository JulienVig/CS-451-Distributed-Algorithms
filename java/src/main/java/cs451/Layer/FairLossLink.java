package cs451.Layer;

import cs451.Host;
import cs451.Packet.BatchPacket;
import cs451.Packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
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
                upperLayerDeliver.accept(BatchPacket.deserializeToObject(dp.getData()));
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
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendBatch(Packet firstPkt){
        BatchPacket batch = new BatchPacket(firstPkt);
        int receiverId = firstPkt.getReceiverId();
//        System.out.println("Buffer size "+ sendBuffer.size());
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
}
