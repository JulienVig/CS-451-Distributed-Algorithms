package cs451;

//import cs451.Layer.FIFOBroadcast;
import cs451.Layer.LCBBroadcast;
import cs451.Layer.LogLayer;
import cs451.Parser.Parser;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Main {
    public static int NB_HOSTS;

    private static void handleSignal(Writer writer) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        writer.flush();
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers(Writer writer) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(writer);
            }
        });
    }

    public static void main(String[] args) {
        System.out.println("start " + ZonedDateTime.now().toInstant().toEpochMilli());
        Parser parser = new Parser(args);
        parser.parse();
        NB_HOSTS = parser.hosts().size();
        Writer writer = new Writer(parser::writeToFile);
        initSignalHandlers(writer);
        new Thread(writer).start();

        int myId = parser.myId();
        Host myHost = null;
        for (Host host : parser.hosts()) if (host.getId() == myId) myHost = host;
        new LCBBroadcast(parser.nbMessageToSend(), writer, myHost, parser.hosts(),
                parser.getHostDependencies(), null).run();
    }
}
