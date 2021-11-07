package cs451.Layer;

import cs451.Host;
import cs451.Operation;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;
import cs451.Writer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class BestEffortBroadcast extends Layer{
    private final Writer writer;
    private final PerfectLink pl;
    private final Consumer<PayloadPacket> upperLayerBroadcast;
    private final List<Host> hosts;
    private final Host myHost;

    public BestEffortBroadcast(int nbMessageToSend, Writer writer, Host myHost, List<Host> hosts,
                               Consumer<Packet> upperLayerDeliver, Consumer<PayloadPacket> upperLayerBroadcast) {
        this.writer = writer;
        this.hosts = hosts;
        this.myHost = myHost;
        this.upperLayerDeliver = upperLayerDeliver;
        this.upperLayerBroadcast = upperLayerBroadcast;
        ArrayList<PayloadPacket> pktToBroadcast = createBroadcastPkt(nbMessageToSend, myHost, hosts);
        pl = new PerfectLink(myHost.getPort(), hosts, this::deliver, pktToBroadcast);
    }

    public void broadcast(PayloadPacket pkt){
//        System.out.println("Broadcast " +pkt.getSimpleId());
        for(Host host: hosts) {
            if (host != myHost) {
                // Create new packet with same originalSender and seqNb
                // but with current host as sender host
                PayloadPacket newPkt = new PayloadPacket(pkt.getSeqNb(), pkt.getOriginalSenderId(),
                                                        myHost.getId(), host.getId());
                pl.sendPayload(newPkt);
            }
        }
    }

    private ArrayList<PayloadPacket> createBroadcastPkt(int nbMessageToSend, Host myHost, List<Host> hosts){
        ArrayList<PayloadPacket> broadcastPkt = new ArrayList<>();
        boolean pktAlreadyLogged = false;
        for(Host host: hosts) {
            if (host != myHost) {
                for (int i = 1; i <= nbMessageToSend ; i++) {
                    PayloadPacket pkt = new PayloadPacket(i, myHost.getId(), host.getId());
                    broadcastPkt.add(pkt);
                    //Only log broadcast once per packet
                    if (!pktAlreadyLogged){
//                        System.out.println("Broadcast " + pkt.getSimpleId());
                        upperLayerBroadcast.accept(pkt);
                        writer.write(pkt, Operation.BROADCAST);
                    }
                }
                pktAlreadyLogged = true;
            }
        }
        return broadcastPkt;
    }

    @Override
    public void deliver(Packet pkt) {
        if (upperLayerDeliver != null) upperLayerDeliver.accept(pkt);
    }

    @Override
    public void run() {
        pl.run();
    }
}
