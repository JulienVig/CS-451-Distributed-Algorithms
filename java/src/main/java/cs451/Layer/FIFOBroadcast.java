//package cs451.Layer;
//
//import cs451.Host;
//import cs451.Operation;
//import cs451.Packet.Packet;
//import cs451.Packet.PayloadPacket;
//import cs451.Writer;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Consumer;
//
//public class FIFOBroadcast extends Layer {
//
//    private final int[] next; // one entry per host
//    //One priority queue per host, where PriorityQueues' comparator uses pktSimpleId
//    private final PriorityQueue<PayloadPacket>[] pending;
//    private final Writer writer;
//
//    public FIFOBroadcast(int nbMessageToSend, Writer writer, Host myHost, List<Host> hosts) {
////        this.upperLayerDeliver = upperLayerDeliver;
//        this.writer = writer;
//        next = new int[hosts.size()];
//        pending = new PriorityQueue[hosts.size()];
//        // Comparator on simpleId because we don't want equality tested on pktId
//        for (int i = 0; i < pending.length; i++) pending[i] = new PriorityQueue<>(Comparator.comparing(PayloadPacket::getSimpleId));
//        for (Host host : hosts) next[host.getId() - 1] = 1;
//        UniformReliableBroadcast urb = new UniformReliableBroadcast(nbMessageToSend, writer, myHost, hosts, this::deliver);
//        new Thread(urb).start();
//
//    }
//
//    @Override
//    public void deliver(Packet pkt) {
//        delivered.offer(pkt);
////        upperLayerDeliver.accept(pkt);
//    }
//
//    @Override
//    public void run() {
//        while(true){
//            try {
//                processPkt(delivered.take());
//            } catch (InterruptedException e){
//                e.printStackTrace();
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//    private void processPkt(Packet pkt){
//        PayloadPacket payloadPacket = (PayloadPacket)  pkt;
//        int hostIdx = payloadPacket.getOriginalSenderId() - 1;
//        pending[hostIdx].add(payloadPacket);
//        if(pending[hostIdx].peek().getSeqNb() == next[hostIdx]) tryDelivering(hostIdx);
//    }
//
//    private void tryDelivering(int hostIdx){
//        //The first packet is always the smallest seq number
//        do {
////            PayloadPacket pkt = pending[hostIdx].poll();
////                System.out.println(Thread.currentThread().getId() + " FIFO deliver " + pkt.getSimpleId());
//            writer.write(pending[hostIdx].poll(), Operation.DELIVER);
//            next[hostIdx]++;
//        } while(pending[hostIdx].size() > 0 && pending[hostIdx].peek().getSeqNb() == next[hostIdx]);
//    }
//}
