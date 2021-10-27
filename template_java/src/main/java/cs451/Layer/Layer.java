package cs451.Layer;


import cs451.Packet.Packet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

abstract class Layer implements Runnable {
    BlockingQueue<Packet> delivered = new LinkedBlockingQueue<>();

    abstract public void deliver(Packet pkt);
    @Override
    abstract public void run();
}
