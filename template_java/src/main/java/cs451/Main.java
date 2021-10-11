package cs451;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    private static void handleSignal(ArrayList<String> writeBuffer, Parser parser,
                                     HashMap<String, String> portToIdMapping) {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        for (String payload : writeBuffer) {
            parser.writeDeliver(parsePayload(payload, portToIdMapping));
        }
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers(ArrayList<String> writeBuffer, Parser parser,
                                           HashMap<String, String> portToIdMapping) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal(writeBuffer, parser, portToIdMapping);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();
        ArrayList<String> writeBuffer = new ArrayList<>();
        HashMap<String, String> portToIdMapping = new HashMap<>();
        initSignalHandlers(writeBuffer, parser, portToIdMapping);

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        int myId = parser.myId();
        int myPort = 0;
        int receiverID = parser.receiverID();
        String receiverIp = "";
        int receiverPort = 0;

        for (Host host: parser.hosts()) {
            int hostId = host.getId();
            int hostPort = host.getPort();

            portToIdMapping.put(String.valueOf(hostPort), String.valueOf(hostId));
            if(hostId == receiverID) {
                receiverIp = host.getIp();
                receiverPort = hostPort;
            }
            if(hostId == myId) myPort = hostPort;
        }

//        System.out.println("Path to output:");
//        System.out.println("===============");
//        System.out.println(parser.output() + "\n");
//
//        System.out.println("Path to config:");
//        System.out.println("===============");
//        System.out.println(parser.config() + "\n");
//
//        System.out.println("Doing some initialization\n");
//
//        System.out.println("Broadcasting and delivering messages...\n");
//
//        System.out.println("Receiver ID: " + parser.receiverID());


        if (myId != receiverID){
            // broadcast
            for (int i = 0; i < parser.nbMessageToSend(); i++) {
                int seqNb = i + 1;
                String payload = String.valueOf(seqNb);
                if(send(payload, receiverIp, receiverPort, myPort)) {
                    parser.writeBroadcast(seqNb);
                }

            }
        } else {
            // receive and write to output

            receive(myPort, writeBuffer);
//                writeBuffer.add(parsePayload(payload, portToIdMapping));
        }
        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }

    public static boolean send(String payload, String toIp, int toPort, int myPort) throws InterruptedException {
        try (DatagramSocket ds = new DatagramSocket(myPort)){
            InetAddress ip = InetAddress.getByName(toIp);
            DatagramPacket dp = new DatagramPacket(payload.getBytes(), payload.length(), ip, toPort);
            ds.send(dp);
            System.out.println("Sent '" + payload + "'");
            return true;
        } catch (Exception e){
            e.printStackTrace();
            throw new InterruptedException();
        }
    }

    public static void receive(int myPort, ArrayList<String> writeBuffer) throws InterruptedException {
        try (DatagramSocket ds = new DatagramSocket(myPort)){
            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket dp = new DatagramPacket(buf, 1024);
                ds.receive(dp);
                String msg = new String(dp.getData(), 0, dp.getLength());
                writeBuffer.add(dp.getPort() + " " + msg); // Format: port seq_nb
                System.out.println(msg + " from port " + dp.getPort());
            }
        } catch(Exception e){
            e.printStackTrace();
            throw new InterruptedException();
        }
    }

    public static String parsePayload(String payload, HashMap<String, String> portToIdMapping){
        String[] args = payload.split("\\s+");
        String senderId = portToIdMapping.get(args[0]);
        String seqNb = args[1];
        return senderId + " " + seqNb;
    }



}
