package cs451;

import cs451.Layer.BestEffortBroadcast;
import cs451.Layer.LogLink;
import cs451.Parser.Parser;

import java.time.ZonedDateTime;

public class Main {

    private static void handleSignal(Writer writer, LogLink link) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        writer.flush();
        if (link != null) link.printState();
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

    public static void main(String[] args) {

        System.out.println(ZonedDateTime.now().toInstant().toEpochMilli() + ": start");
        Parser parser = new Parser(args);
        parser.parse();

//        long pid = ProcessHandle.current().pid();
//        System.out.println("My PID: " + pid + "\n");
//        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
//        System.out.println("My ID: " + parser.myId() + "\n");

        int myId = parser.myId();
        Host myHost = null;
        for (Host host : parser.hosts()) if (host.getId() == myId) myHost = host;

        Writer writer = new Writer(parser::writeBroadcast, parser::writeDeliver);
        new Thread(writer).start();
        BestEffortBroadcast br = new BestEffortBroadcast(parser.nbMessageToSend(), writer, myHost, parser.hosts());
        initSignalHandlers(writer, null);
        br.run();
    }

}
