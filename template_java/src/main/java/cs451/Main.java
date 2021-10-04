package cs451;

import java.net.*;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

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
            if(host.getId() == receiverID) {
                receiverIp = host.getIp();
                receiverPort = host.getPort();
            }
            if(host.getId() == myId) myPort = host.getPort();
        }

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");

        System.out.println("Number of messages to send: " + parser.nbMessageToSend());
        System.out.println("Receiver ID: " + parser.receiverID());


        if (myId != receiverID){
            // broadcast
            send(1, receiverIp, receiverPort);
        } else {
            // receive and write to output
            receive(myPort);
        }
        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }

    public static void send(int nb, String toIp, int toPort) throws InterruptedException {
        try (DatagramSocket ds = new DatagramSocket()){
            String msg = "b " + nb;
            InetAddress ip = InetAddress.getByName(toIp);
            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), ip, toPort);
            ds.send(dp);
            System.out.println("Sent '"+msg+"'");
        } catch (Exception e){
            throw new InterruptedException();
        }
    }

    public static void receive(int myPort) throws InterruptedException {
        try (DatagramSocket ds = new DatagramSocket(myPort)){
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, 1024);
            while(true) {
                ds.receive(dp);
                String msg = new String(dp.getData(), 0, dp.getLength());
                System.out.println(msg + " from port " + dp.getPort());
            }
        } catch(Exception e){
            throw new InterruptedException();
        }
    }
}
