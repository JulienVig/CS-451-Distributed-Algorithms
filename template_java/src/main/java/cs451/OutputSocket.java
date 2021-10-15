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

public class OutputSocket implements Runnable{
    private DatagramSocket ds;
    volatile HashSet<String> pktToBeAck;
    volatile HashMap<String, PayloadPacket> pktSent;
    volatile ArrayList<Packet> sendBuffer;
    private Writer writer;

    public OutputSocket(DatagramSocket ds, Writer writer, HashMap<String, PayloadPacket> pktSent,
                        HashSet<String> pktToBeAck, ArrayList<Packet> sendBuffer) {
        this.ds = ds;
        this.pktToBeAck = pktToBeAck;
        this.sendBuffer = sendBuffer;
        this.pktSent = pktSent;
        this.writer = writer;
    }

    @Override
    public void run() {
        while(true){
            for (int i = 0; i < sendBuffer.size(); i++) {
                send(sendBuffer.remove(i));
            }
//            for (Packet pkt : sendBuffer) send(pkt);
        }
    }

    public void sendPayloadAndLog(PayloadPacket pkt){
        writer.write(pkt, Operation.BROADCAST);
        sendPayload(pkt);
    }

    public void sendPayload(PayloadPacket pkt) {
        sendBuffer.add(pkt);
        pktToBeAck.add(pkt.getPktId());
        pktSent.put(pkt.getPktId(), pkt);
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
}
