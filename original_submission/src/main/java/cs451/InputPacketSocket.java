package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InputPacketSocket extends PacketSocket{
    private volatile HashSet<String> pktReceived = new HashSet<>();
    private Responder responder;

    public InputPacketSocket(DatagramSocket ds, Writer writer,
                             ConcurrentLinkedQueue<String> pktToBeAck, BlockingQueue<Packet> sendBuffer) {
        super(ds, writer, pktToBeAck, sendBuffer);
        responder = new Responder();
        new Thread(responder).start();
    }
    @Override
    public void run() {
        try {
            byte[] buf;
            while (true) {
                buf = new byte[512];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
                responder.ack(dp.getData());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inspired from https://stackoverflow.com/a/777209
     */
    private class Responder implements Runnable {
        BlockingQueue<byte[]> receiveBuffer = new LinkedBlockingQueue<>();
        public void ack(byte[] bytes){
            try {
                receiveBuffer.put(bytes);
            } catch(InterruptedException e){
                System.err.println("Couldn't add packet to receiveBuffer queue");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(true){
                try {
                    processPacket(receiveBuffer.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
//                    Thread.currentThread().interrupt();
                }
            }
        }

        private void processPacket(byte[] bytes){
            Packet pkt = deserializePkt(bytes);
//            assert pkt instanceof PayloadPacket || pkt instanceof AckPacket;
//            System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": received " + pkt);
            if (pkt instanceof PayloadPacket) {
                PayloadPacket payloadPkt = (PayloadPacket) pkt;
                sendAck(payloadPkt); // Always send ack when receiving a payload packet
                if (!pktReceived.contains(payloadPkt.getPktId())) {
                    //If pkt not already processed in the past
                    pktReceived.add(payloadPkt.getPktId());
                    System.out.println("Received " + payloadPkt);
                    writer.write(payloadPkt, Operation.DELIVER); // Format: sender_id seq_nb
//                    publish(payloadPkt); //TODO remove for perf
                }
            } else {
                AckPacket ackPacket = (AckPacket) pkt;
                // System.out.println("Ack " + ackPacket);
                pktToBeAck.remove(ackPacket.getPayloadPktId());
                // System.out.println("Remaining packets to be ack: " + pktToBeAck.size());
//                publish(ackPacket); //TODO remove for perf
            }
        }

        private void sendAck(PayloadPacket pkt){
            System.out.println("Add to sendBuffer ack " + pkt);
            AckPacket ackPkt = new AckPacket(pkt);
            try {
                sendBuffer.put(ackPkt);
            } catch(InterruptedException e){
                System.err.println("Couldn't add packet to sendBuffer queue");
                e.printStackTrace();
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
