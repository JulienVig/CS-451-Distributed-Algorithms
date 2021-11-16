package cs451.Packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class BatchPacket extends Packet {
    public static final int BYTE_CAPACITY = 1016;//508;
    private ByteBuffer bb;
    private int length = 4;
//    private int nbPkt;
    private ArrayList<Packet> packets;

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
//        nbPkt++;
    }

    public boolean isFull(){
        return length + PayloadPacket.BYTE_SIZE > BYTE_CAPACITY;
    }

    public BatchPacket(ArrayList<Packet> batch) {
        packets = batch;
    }

    @Override
    public byte[] serializeToBytes() {
        bb.rewind();
        bb.putInt(length);
//        System.out.println("Serialize "+ nbPkt +" to " + length + " bytes");
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

//        int count=0;
        int length = bb.getInt();
        int offset = 4;
        while(offset < length){
            bb.get(); //flush the first byte
            if(bytes[offset] == (byte) 1){
                PayloadPacket pkt = new PayloadPacket(bb,
                        Arrays.copyOfRange(bytes, offset, offset + PayloadPacket.BYTE_SIZE));
                batch.add(pkt);
                offset += PayloadPacket.BYTE_SIZE;
            } else{
                batch.add(new AckPacket(bb));
                offset += AckPacket.BYTE_SIZE;
            }
//            count++;
        }
//        System.out.println("Deserialize " + length + " bytes to "+count+" packets");
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
