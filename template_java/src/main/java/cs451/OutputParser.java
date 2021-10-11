package cs451;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputParser {

    private static final String OUTPUT_KEY = "--output";

    private String path;

    public boolean populate(String key, String value) {
        if (!key.equals(OUTPUT_KEY)) {
            return false;
        }
        File file = new File(value);
        path = file.getPath();
        try (PrintWriter pw = new PrintWriter(path)){
            // Initializing the writer empties the file if there was
            // already some content written inside.
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public String getPath() {
        return path;
    }

    public boolean writeBroadcast(int seqNb){
        return writeToFile("b " + seqNb);
    }

    public boolean writeDeliver(String msg){
        return writeToFile("d " + msg);
    }

    private boolean writeToFile(String msg){
        try(FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(msg);
            return true;
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
            System.err.println("An exception occurred while writing to an output file: ");
            e.printStackTrace();
            return false;
        }
    }

}
