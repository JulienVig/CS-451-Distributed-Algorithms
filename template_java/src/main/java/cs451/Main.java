package cs451;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Main {

    private static void handleSignal(Writer writer, LogLink link) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        writer.flush();
        link.printState();
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers(Writer writer, LogLink link) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(writer, link);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
//        System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": start");
        Parser parser = new Parser(args);
        parser.parse();

//        long pid = ProcessHandle.current().pid();
//        System.out.println("My PID: " + pid + "\n");
//        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
//        System.out.println("My ID: " + parser.myId() + "\n");
        int myId = parser.myId();
        int receiverID = parser.receiverID();
        Host myHost = null;
        Host receiverHost  = null;

        for (Host host : parser.hosts()) {
            if (host.getId() == receiverID) receiverHost = host;
            if (host.getId() == myId) myHost = host;
        }
//        assert myHost != null && receiverHost != null;

        Writer writer = new Writer(parser::writeBroadcast, parser::writeDeliver);
        new Thread(writer).start();

        PerfectLink link;
        if (myId != receiverID) {
            link = new PerfectLink(myHost.getPort(), receiverHost.getIp(), receiverHost.getPort(),
                    writer, createBroadcastPkt(parser, myHost, receiverHost));

        } else link = new PerfectLink(myHost.getPort(), receiverHost.getIp(),
                                            receiverHost.getPort(), writer);
//        LogLink link = new LogLink(pl, myId == receiverID, myId, parser.hosts().size(),
//                                    parser.nbMessageToSend());
        initSignalHandlers(writer, null);
        link.run();
    }

    public static ArrayList<PayloadPacket> createBroadcastPkt(Parser parser, Host myHost, Host receiverHost){
        ArrayList<PayloadPacket> broadcastPkt = new ArrayList<>();
        for (int i = 0; i < parser.nbMessageToSend(); i++) {
            PayloadPacket pkt = new PayloadPacket(myHost.getId(), i + 1, myHost, receiverHost);
            broadcastPkt.add(pkt);
        }
        return broadcastPkt;
    }

}
