import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lidaxia on 07/07/2017.
 * This class works as client and tries to connect tree neighbours.
 */
public class ServerThread {
    Socket clientSocket = null;
    ServerThread[] threads;
    private TreeNode[] tn;
    int LocalId;
    int serverId;
    PrintStream out = null;

    ServerThread(TreeNode[] tn, int LocalId, int treeNeighboursNumber) {
        this.tn = tn;
        this.LocalId = LocalId;
        threads = new ServerThread[treeNeighboursNumber];
    }

    ServerThread(Socket clientSocket, int serverId) throws IOException{
        this.clientSocket = clientSocket;
        this.serverId =serverId;
        out = new PrintStream(clientSocket.getOutputStream());
    }

    void connectServers() {
        try {
            for (int i = 0; i < threads.length; i++) {
                String hostname = tn[tn[LocalId].getTreeNeighbour().get(i)].getHostName() + ".utdallas.edu";
                int port = tn[tn[LocalId].getTreeNeighbour().get(i)].getPort();
                clientSocket = new Socket(hostname, port);
                threads[i] = new ServerThread(clientSocket, tn[LocalId].getTreeNeighbour().get(i));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
