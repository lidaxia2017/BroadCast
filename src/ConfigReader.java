import java.io.*;
import java.util.LinkedList;

/**
 * Created by lidaxia on 16/06/2017.
 */
public class ConfigReader {
    private int numberOfNodes;
    private TreeNode[] tn;
    private int broadcastTimes;
    private int randomMean;

    private boolean isValidLine(String line) {
        String str = line.trim();
        return !(str.length() == 0 || str.startsWith("#") || str.startsWith("-"));
    }

    /*
    * Read parameters from config file.
    * The first valid line should be broadcastTimes.
    * The second valid line should be randomMean.
    * The third valid line should be numberOfNodes.
    * The left valid lines should be node information.
    */
    void readFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("No File Found " + fileName);
        }
        InputStreamReader isr;
        try
        {
            isr = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(isr);
            String line;
            int nodeIdx = 0;
            while ((line = br.readLine()) != null) {
                if (isValidLine(line)) {
                    if (broadcastTimes == 0) {
                        broadcastTimes = Integer.parseInt(line);
                    } else if (randomMean == 0) {
                        randomMean = Integer.parseInt(line);
                    }
                    else if (numberOfNodes == 0) {
                        numberOfNodes = Integer.parseInt(line);
                        tn = new TreeNode[numberOfNodes];
                    } else {
                        String[] nodeInfo = line.split(" ");
                        tn[nodeIdx] = new TreeNode(nodeIdx,nodeInfo[0],Integer.parseInt(nodeInfo[1]),new LinkedList<>());
                        int n = 2;
                        while (n<nodeInfo.length) {
                            tn[nodeIdx].addNeighbour(Integer.parseInt(nodeInfo[n++]) - 1);
                        }
                        nodeIdx++;
                    }
                }
            }
            br.close();
            isr.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    TreeNode[] getTn() {
        return tn;
    }

    int getNumberOfNodes() {
        return  numberOfNodes;
    }

    int getBroadcastTimes() {
        return  broadcastTimes;
    }

    int getRandomMean() {
        return  randomMean;
    }
}
