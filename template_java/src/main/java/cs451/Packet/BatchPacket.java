package cs451.Packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class BatchPacket extends Packet {
    public static final int BYTE_CAPACITY = 65507;// 64000; //doesn't change perf when above 508
    private ByteBuffer bb;
    private int length = 4;
    private ArrayList<Packet> packets;
//    private int nbPkts;

    public BatchPacket(Packet firstPkt){
        setPktId(firstPkt.getPktId());
        setReceiverId(firstPkt.getReceiverId());
        bb = ByteBuffer.allocate(BYTE_CAPACITY);
        bb.position(4); //Skip 4 byte, the first 4 is for the nb of packets
        addPacket(firstPkt);
    }

    public void addPacket(Packet pkt){
        pkt.serializeToBytes(bb);
        length += pkt.getByteSize();
//        nbPkts++;
    }

    public boolean isFull(){
        return length + PayloadPacket.BYTE_SIZE > BYTE_CAPACITY;
    }

    public BatchPacket(ArrayList<Packet> batch) {
        packets = batch;
    }

    @Override
    public byte[] serializeToBytes() {
//        System.out.println(nbPkts);
        bb.rewind();
        bb.putInt(length);
        return (length >= 490) ? bb.array() : Arrays.copyOfRange(bb.array(),0, length);
    }

    @Override
    public int length(){
        return length;
    }

    @Override
    public void serializeToBytes(ByteBuffer bb) {
        return;
    }

    public static Packet deserializeToObject(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        ArrayList<Packet> batch = new ArrayList<>();

        int length = bb.getInt();
        int offset = 4;
        while(offset < length){
            bb.get(); //flush the first byte
            if(bytes[offset] == (byte) 1){
                batch.add(new PayloadPacket(bb,
                        Arrays.copyOfRange(bytes, offset, offset + PayloadPacket.BYTE_SIZE)));
                offset += PayloadPacket.BYTE_SIZE;
            } else{
                batch.add(new AckPacket(bb));
                offset += AckPacket.BYTE_SIZE;
            }
        }
        return new BatchPacket(batch);
    }

    public ArrayList<Packet> getPackets() {
        return packets;
    }

    @Override
    public int getByteSize() {
        return length;
    }
}
