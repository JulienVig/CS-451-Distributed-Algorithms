package cs451;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
public class ConfigParser {

    private String path;
    private int nbMessageToSend;
    private int receiverID;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        return readConfig();
    }

    public boolean readConfig(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String configText = br.readLine();
            String[] configArgs = configText.split(" ");
            nbMessageToSend = Integer.parseInt(configArgs[0]);
            receiverID = Integer.parseInt(configArgs[1]);
            br.close();
            return true;
        } catch (Exception e) {
            System.err.println("An exception occurred while reading the config file: ");
            e.printStackTrace();
            return false;
        }
    }

    public String getPath() {
        return path;
    }

    public int getNbMessage(){
        return nbMessageToSend;
    }

    public int getReceiverID(){
        return receiverID;
    }


}
