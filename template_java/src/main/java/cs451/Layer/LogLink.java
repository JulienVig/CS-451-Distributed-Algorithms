package cs451.Layer;

import cs451.Packet.AckPacket;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;

import java.time.ZonedDateTime;
import java.util.HashSet;

public class LogLink extends Layer {
    private PerfectLink link;
    private boolean isReceiver;
    private volatile HashSet<String> pktIdToDeliver = new HashSet<>();
    private volatile HashSet<String> pktIdToBroadcast = new HashSet<>();
    private boolean sentAllPkt = true;
    private int nbMsg;
    private int nbHost;
//    private BlockingQueue<Packet> delivered = new LinkedBlockingQueue<>();

    public LogLink(PerfectLink link, boolean isReceiver, int myId,int nbHost, int nbMsg) {
        if (myId == 2) System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": starting broadcast");
        this.link = link;
        link.addUpperLayerDeliver(this::deliver);
        this.isReceiver = isReceiver;
        this.nbMsg = nbMsg;
        this.nbHost = nbHost;
        if (isReceiver){
            for (int id = 1; id <= nbHost; id++) {
                if(id != myId){
                    for (int seqNb = 1; seqNb <= nbMsg; seqNb++) {
                        pktIdToDeliver.add(id + " " + seqNb);
                    }
                }
            }
        } else for (int seqNb = 1; seqNb <= nbMsg; seqNb++) pktIdToBroadcast.add(myId + " " + seqNb);

    }

    @Override
    public void run() {
        new Thread(link).start();
        while (true) {
            try {
                receiverHandler(delivered.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiverHandler(Packet pkt) {
        if (isReceiver && pkt instanceof PayloadPacket) {
            if (!pktIdToDeliver.remove(pkt.getPktId())) {
                System.err.println("Delivered an unexpected packet: " + pkt.getPktId());
            } else if (pktIdToDeliver.isEmpty()) {
                System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": delivered all expected packets!");
            }
        }
        if(!isReceiver && pkt instanceof AckPacket){
            AckPacket ackPkt = (AckPacket) pkt;
            if (!pktIdToBroadcast.remove(ackPkt.getPayloadPktId())){
                System.err.println("Received ack for an already delivered packet: " + pkt.getPktId());
            }else if (sentAllPkt && pktIdToBroadcast.isEmpty()) {
                System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": received ack for all packets!");
            }
        }
    }

    private void senderHandler(Packet pkt){
        if(!isReceiver && pkt instanceof PayloadPacket){
            pktIdToBroadcast.add(pkt.getPktId());
            if (!sentAllPkt && pktIdToBroadcast.size() == nbMsg) {
                sentAllPkt = true;
                System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": sent all packets!");
            }

        }
    }

    public void printState(){
        if(isReceiver) {
            int nbPktNotDelivered = pktIdToDeliver.size();
            System.out.println("Number of packets not delivered: " + nbPktNotDelivered);
            System.out.println("Number of packets delivered: " + (nbMsg * (nbHost - 1) - nbPktNotDelivered));
        } else{
            int bdSize = pktIdToBroadcast.size();
            if(sentAllPkt){
                System.out.println("Nb message not acknowledged:" + bdSize);
                System.out.println("Nb message acknowledged:" + (nbMsg - bdSize));
            } else {
                System.out.println("Broadcasted only " + bdSize + " packets over " + nbMsg);
            }

        }

    }

    @Override
    public void deliver(Packet pkt) {
        delivered.offer(pkt);
    }
}
