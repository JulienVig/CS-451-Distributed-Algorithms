package cs451;

import java.util.ArrayList;

public class Main {

    private static void handleSignal(ArrayList<String> writeBuffer, Parser parser) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        for (String payload : writeBuffer) {
            parser.writeDeliver(payload);
        }
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers(ArrayList<String> writeBuffer, Parser parser) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(writeBuffer, parser);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
        System.out.println("My ID: " + parser.myId() + "\n");
        int myId = parser.myId();
        int receiverID = parser.receiverID();
        Host myHost = null;
        Host receiverHost  = null;

        for (Host host : parser.hosts()) {
            if (host.getId() == receiverID) receiverHost = host;
            if (host.getId() == myId) myHost = host;
        }
        assert myHost != null && receiverHost != null;

        ArrayList<String> writeBuffer = new ArrayList<>();
        initSignalHandlers(writeBuffer, parser);

        PerfectLink link = new PerfectLink(myHost.getPort(), receiverHost.getIp(),
                                            receiverHost.getPort(), writeBuffer);

        if (myId == receiverID) link.run();
        else broadcast(parser, myHost, receiverHost, link);

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }

    public static void broadcast(Parser parser, Host myHost, Host receiverHost, PerfectLink link){
        for (int i = 0; i < parser.nbMessageToSend(); i++) {
            int seqNb = i + 1;
            PayloadPacket pkt = new PayloadPacket(myHost.getId(), seqNb, myHost, receiverHost);
            if (link.sendPayload(pkt)) {
                parser.writeBroadcast(seqNb);
            }
        }
        link.run();
    }



}
