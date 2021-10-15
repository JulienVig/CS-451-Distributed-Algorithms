package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

public class OutputSocket implements Runnable{
    private DatagramSocket ds;
    volatile HashSet<String> pktToBeAck;
    volatile HashMap<String, PayloadPacket> pktSent;
    private BlockingQueue<Packet> sendBuffer;
    private Writer writer;

    public OutputSocket(DatagramSocket ds, Writer writer, HashMap<String, PayloadPacket> pktSent,
                        HashSet<String> pktToBeAck, BlockingQueue<Packet> sendBuffer) {
        this.ds = ds;
        this.pktToBeAck = pktToBeAck;
        this.sendBuffer = sendBuffer;
        this.pktSent = pktSent;
        this.writer = writer;
    }

    @Override
    public void run() {
        while(true){
            try {
                send(sendBuffer.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendPayloadAndLog(PayloadPacket pkt){
        writer.write(pkt, Operation.BROADCAST);
        sendPayload(pkt);
    }

    public void sendPayload(PayloadPacket pkt){
        try {
            sendBuffer.put(pkt);
        } catch(InterruptedException e){
            System.err.println("Couldn't add packet to sendBuffer queue");
            e.printStackTrace();
        }
        pktToBeAck.add(pkt.getPktId());
        pktSent.put(pkt.getPktId(), pkt);
    }

    private boolean send(Packet pkt){
        try {
            InetAddress ip = InetAddress.getByName(pkt.receiverHost.getIp());
            DatagramPacket dp = new DatagramPacket(pkt.getBytes(), pkt.length(), ip, pkt.receiverHost.getPort());
            ds.send(dp);
            System.out.println(Thread.currentThread().getId() + " sent '" + pkt + "'");
            return true;
        } catch (Exception e) {
            System.err.println("Exception while sending " + pkt + " through " + this);
            e.printStackTrace();
            return false;
        }
    }
}
