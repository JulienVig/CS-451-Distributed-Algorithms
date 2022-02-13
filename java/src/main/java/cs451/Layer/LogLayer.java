package cs451.Layer;

import cs451.Host;
import cs451.Packet.AckPacket;
import cs451.Packet.Packet;
import cs451.Packet.PayloadPacket;
import cs451.Writer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class LogLayer extends Layer{
    private LCBBroadcast lcb;
    private int nbMessageToDeliver;
    private int deliveredCounter;

    public LogLayer(int nbMessageToSend, Writer writer, Host myHost, List<Host> hosts, ArrayList[] hostDep) {
        this.nbMessageToDeliver = nbMessageToSend * hosts.size();
        lcb = new LCBBroadcast(nbMessageToSend, writer, myHost, hosts, hostDep, this::deliver);
    }

    public void deliver(Packet pkt) {
        if (++deliveredCounter == nbMessageToDeliver) {
            System.out.println("end " + ZonedDateTime.now().toInstant().toEpochMilli());
        }
//        System.out.println(deliveredCounter);
    }

    @Override
    public void run() {
        lcb.run();
    }
}
