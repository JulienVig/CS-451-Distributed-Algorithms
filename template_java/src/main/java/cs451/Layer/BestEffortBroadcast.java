package cs451.Layer;

import cs451.Host;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BestEffortBroadcast extends Layer{
    private final PerfectLink pl;
    private final Consumer<PayloadPacket> upperLayerBroadcast;
    private final List<Host> hosts;
    private final Host myHost;

    public BestEffortBroadcast(ArrayList<PayloadPacket> pktToBroadcast, Host myHost, List<Host> hosts,
                               Consumer<Packet> upperLayerDeliver, Consumer<PayloadPacket> upperLayerBroadcast) {
        this.hosts = hosts;
        this.myHost = myHost;
        this.upperLayerDeliver = upperLayerDeliver;
        this.upperLayerBroadcast = upperLayerBroadcast;
        pl = new PerfectLink(myHost.getPort(), hosts, this::deliver, pktToBroadcast);
    }

    public void broadcast(PayloadPacket pkt){ pl.sendPayload(pkt);}

    public void reBroadcast(PayloadPacket pkt){
//        System.out.println("Broadcast " +pkt.getSimpleId());
        for(Host host: hosts) {
            if (host != myHost) {
                // Create new packet with same originalSender and seqNb
                // but with current host as sender host
                PayloadPacket newPkt = new PayloadPacket(pkt.getSeqNb(), pkt.getOriginalSenderId(),
                                                        myHost.getId(), host.getId(), pkt.getClock());
                pl.sendPayload(newPkt);
            }
        }
    }


    @Override
    public void deliver(Packet pkt) {

//        System.out.println(Thread.currentThread().getId() + " BEB deliver " + ((PayloadPacket) pkt).getSimpleId());
        upperLayerDeliver.accept(pkt);
    }

    @Override
    public void run() {
        pl.run();
    }
}
