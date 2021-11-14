package cs451;

import cs451.Layer.FIFOBroadcast;
import cs451.Layer.LogLayer;
import cs451.Parser.Parser;

import java.time.ZonedDateTime;

public class Main {

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
        Writer writer = new Writer(parser::writeToFile);
        initSignalHandlers(writer);
//        new Thread(writer).start();

//        long pid = ProcessHandle.current().pid();
//        System.out.println("My PID: " + pid + "\n");
//        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
//        System.out.println("My ID: " + parser.myId() + "\n");

        int myId = parser.myId();
        Host myHost = null;
        for (Host host : parser.hosts()) if (host.getId() == myId) myHost = host;

        LogLayer log = new LogLayer(parser.nbMessageToSend(), writer,
                myHost, parser.hosts());
        log.run();
    }

}
