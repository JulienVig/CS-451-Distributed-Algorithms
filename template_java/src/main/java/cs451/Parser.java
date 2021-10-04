package cs451;

import java.util.List;

public class Parser {

    private String[] args;
    private long pid;
    private IdParser idParser;
    private HostsParser hostsParser;
    private OutputParser outputParser;
    private ConfigParser configParser;

    public Parser(String[] args) {
        this.args = args;
    }

    public void parse() {
        pid = ProcessHandle.current().pid();

        idParser = new IdParser();
        hostsParser = new HostsParser();
        outputParser = new OutputParser();
        configParser = new ConfigParser();

        int argsNum = args.length;
        if (argsNum != Constants.ARG_LIMIT_CONFIG) {
            help("Wrong number of arguments");
        }

        if (!idParser.populate(args[Constants.ID_KEY], args[Constants.ID_VALUE])) {
            help("ID parsing failed");
        }

        if (!hostsParser.populate(args[Constants.HOSTS_KEY], args[Constants.HOSTS_VALUE])) {
            help("Hosts parsing failed");
        }

        if (!hostsParser.inRange(idParser.getId())) {
            help("Specified ID not in hosts ID's");
        }

        if (!outputParser.populate(args[Constants.OUTPUT_KEY], args[Constants.OUTPUT_VALUE])) {
            help("Output parsing failed");
        }

        if (!configParser.populate(args[Constants.CONFIG_VALUE])) {
            help("Config parsing failed");
        }
    }

    private void help(String errorMsg) {
        System.err.println(errorMsg);
        System.err.println("Usage: ./run.sh --id ID --hosts HOSTS --output OUTPUT CONFIG");
        System.exit(1);
    }

    public int myId() {
        return idParser.getId();
    }

    public int myPort() {return idParser.getId(); }

    public List<Host> hosts() {
        return hostsParser.getHosts();
    }

    public String output() {
        return outputParser.getPath();
    }

    public String config() {
        return configParser.getPath();
    }
    public int nbMessageToSend() {
        return configParser.getNbMessage();
    }
    public int receiverID() {return configParser.getReceiverID();}

}
