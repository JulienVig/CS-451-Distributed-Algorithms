package cs451.Layer;

import cs451.Host;
import cs451.Operation;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;
import cs451.Parser.Parser;
import cs451.Writer;

import java.util.ArrayList;
import java.util.List;

public class BestEffortBroadcast extends Layer{
    private Writer writer;
    private PerfectLink pl;

    public BestEffortBroadcast(int nbMessageToSend, Writer writer, Host myHost, List<Host> hosts) {
        this.writer = writer;
        ArrayList<PayloadPacket> pktToBroadcast = createBroadcastPkt(nbMessageToSend, myHost, hosts);
        pl = new PerfectLink(myHost.getPort(), writer, pktToBroadcast);
        pl.addUpperLayerDeliver(this::deliver);
    }

    private ArrayList<PayloadPacket> createBroadcastPkt(int nbMessageToSend, Host myHost, List<Host> hosts){
        ArrayList<PayloadPacket> broadcastPkt = new ArrayList<>();
        int myId = myHost.getId();
        boolean pktAlreadyLogged = false;
        for(Host host: hosts) {
            if (host != myHost) {
                for (int i = 0; i < nbMessageToSend; i++) {
                    PayloadPacket pkt = new PayloadPacket(myId, i + 1, myHost, host);
                    broadcastPkt.add(pkt);

                    //Only log broadcast once per packet
                    if (!pktAlreadyLogged){
                        System.out.println("Send " + pkt);
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
        // PerfectLink only delivers PayloadPacket
//        if (pkt instanceof PayloadPacket) {
//            PayloadPacket payloadPkt = (PayloadPacket) pkt;
        System.out.println("Beb deliver" + pkt);
//        }
    }

    @Override
    public void run() {
        pl.run();
    }
}
