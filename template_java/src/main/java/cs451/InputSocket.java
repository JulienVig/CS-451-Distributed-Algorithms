package cs451;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class InputSocket implements Runnable{
    private DatagramSocket ds;
    private Writer writer;
    volatile HashSet<String> pktToBeAck;
    volatile HashSet<String> pktReceived = new HashSet<>();
    volatile ArrayList<Packet> sendBuffer;
    private Responder responder;

    public InputSocket(DatagramSocket ds, Writer writer,
                       HashSet<String> pktToBeAck, ArrayList<Packet> sendBuffer) {
        this.ds = ds;
        this.writer = writer;
        this.pktToBeAck = pktToBeAck;
        this.sendBuffer = sendBuffer;
        responder = new Responder();
        new Thread(responder).start();
    }
    @Override
    public void run() {
        receive();
    }

    public void receive(){
        try {
            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket dp = new DatagramPacket(buf, 1024);
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
        private ArrayList<byte[]> receiveBuffer = new ArrayList<>();

        public void ack(byte[] bytes){
            receiveBuffer.add(bytes);
        }

        @Override
        public void run() {
            while(true){
                for (int i = 0; i < receiveBuffer.size(); i++) {
                    processPacket(receiveBuffer.remove(i));
                }
//                for (byte[] bytes : receiveBuffer) processPacket(bytes);
            }
        }

        private void processPacket(byte[] bytes){
            Packet pkt = deserializePkt(bytes);
            assert pkt instanceof PayloadPacket || pkt instanceof AckPacket;
            System.out.println("Received: " + pkt);
            if (pkt instanceof PayloadPacket) {
                PayloadPacket payloadPkt = (PayloadPacket) pkt;
                sendAck(payloadPkt); // Always send ack when receiving a payload packet
                if (!pktReceived.contains(payloadPkt.getPktId())) {
                    //If pkt not already processed in the past
                    pktReceived.add(payloadPkt.getPktId());
                    writer.write(payloadPkt, Operation.DELIVER); // Format: sender_id seq_nb
                }
            } else {
                pktToBeAck.remove(((AckPacket) pkt).getPayloadPktId());
            }
        }

        private void sendAck(PayloadPacket pkt){
            AckPacket ackPkt = new AckPacket(pkt);
            sendBuffer.add(ackPkt);
            //TODO: implement sender thread on OutputSocket
//            while(!send(ackPkt)){
//                System.out.println("Waiting 1s");
//                try{
//                    TimeUnit.SECONDS.sleep(1);
//                } catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
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
