import java.util.LinkedList;

/**
 * Created by lidaxia on 16/06/2017.
 */
public class TreeNode {
    private int nodeId;
    private String hostName;
    private int port;
    private LinkedList<Integer> neighbours;
    private LinkedList<Integer> treeNeighbour;

    TreeNode(int nodeId, String hostName, int port, LinkedList<Integer> neighbours) {
        this.nodeId = nodeId;
        this.hostName = hostName;
        this.port = port;
        this.neighbours = neighbours;
        this.treeNeighbour = new LinkedList<>();
    }

    int getNodeId() {
        return nodeId;
    }

    String getHostName() {
        return hostName;
    }

    int getPort() {
        return port;
    }

    LinkedList<Integer> getNeighbours() {
        return neighbours;
    }

    LinkedList<Integer> getTreeNeighbour() {
        return treeNeighbour;
    }

    void addNeighbour(int n) {
        neighbours.add(n);
    }

    void addTreeNeighbour(int n) {
        treeNeighbour.add(n);
    }
}
