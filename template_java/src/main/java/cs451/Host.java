package cs451;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class Host {

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private InetAddress inet;
    private int port = -1;

    public boolean populate(String idString, String ipString, String portString) {
        try {
            id = Integer.parseInt(idString);
            inet = InetAddress.getByName(ipString);
            String ipTest = inet.toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = ipTest.substring(1);
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]).getHostAddress();
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                System.err.println("Port in the hosts file must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            if (port == -1) {
                System.err.println("Id in the hosts file must be a number!");
            } else {
                System.err.println("Port in the hosts file must be a number!");
            }
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public InetAddress getInetAddress() {
        return inet;
    }

    public int getPort() {
        return port;
    }

    public String toString(){
        return "host " + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return id == host.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
