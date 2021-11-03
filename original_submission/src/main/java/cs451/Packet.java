package cs451;

import java.io.*;
import java.util.Objects;

/**
 * Serialization inspired from https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
 */
public abstract class Packet implements Serializable {
    private String pktId;
    private transient byte[] byteArray;
    // TODO: set to private and create deep copy for setters
    Host senderHost;
    Host receiverHost;

    byte[] serializeToBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            System.err.println("Could not serialize packet" + this);
            e.printStackTrace();
            return null;
        }
    }


    public int length() {
        return byteArray.length;
    }

    public String getPktId() {
        return pktId;
    }

    public void setPktId(String pktId) {
        this.pktId = pktId;
    }

    public byte[] getBytes() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    /**
     * Source: https://www.sitepoint.com/implement-javas-equals-method-correctly/
     * @param o object to be compared with
     * @return boolean
     */
    @Override
    public boolean equals(Object o){
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        PayloadPacket that = (PayloadPacket) o;
        // field comparison
        return Objects.equals(this.getPktId(), that.getPktId());
    }

    @Override
    public int hashCode() {
        return getPktId().hashCode();
    }
}