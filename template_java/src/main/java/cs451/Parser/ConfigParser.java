package cs451.Parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigParser {

    private String path;
    private int nbMessageToSend;
    private int receiverID;
    private ArrayList[] hostDep;

    public boolean populate(String value, int nbOfHosts) {
        File file = new File(value);
        path = file.getPath();
        hostDep = new ArrayList[nbOfHosts];
        return readConfig();
    }

    public boolean readConfig(){
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            parseLCBConfig(br);
            return true;
        } catch (Exception e) {
            System.err.println("An exception occurred while reading the config file: ");
            e.printStackTrace();
            return false;
        }
    }

    private void parseLCBConfig(BufferedReader br) throws IOException {
        nbMessageToSend = Integer.valueOf(br.readLine());
        String line;
        String[] dep;
        ArrayList<Integer> curr;
        int counter = 0;
        while ((line = br.readLine()) != null) {
            dep = line.split(" ");
            curr = new ArrayList<>();
            for (int i = 1; i < dep.length; i++) {
                curr.add(Integer.valueOf(dep[i]));
            }
            hostDep[Integer.valueOf(dep[0]) - 1] = (ArrayList) curr.clone();
            counter++;
        }
        if(counter != hostDep.length) System.err.println("LCB config missing dependencies for some hosts.");
    }

    private void parseFIFOConfig(String configText){
        nbMessageToSend = Integer.valueOf(configText);
    }

    private void parsePerfectLinkConfig(String configText){
        String[] configArgs = configText.split(" ");
        nbMessageToSend = Integer.parseInt(configArgs[0]);
        receiverID = Integer.parseInt(configArgs[1]);
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

    public ArrayList[] getHostDep(){
        return hostDep;
    }


}
