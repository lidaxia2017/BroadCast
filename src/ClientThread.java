import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lidaxia on 07/07/2017.
 * This class works as server and accepts clients which tries to connect.
 */
public class ClientThread {
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    ClientThread[] threads;
    BufferedReader in = null;

    ClientThread(ServerSocket serverSocket, int treeNeighboursNumber) {
        this.serverSocket = serverSocket;
        threads = new ClientThread[treeNeighboursNumber];
    }

    ClientThread(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void acceptClients() {
        try {
            int i = 0;
            while (i < threads.length) {
                clientSocket = serverSocket.accept();
                threads[i++] =new ClientThread(clientSocket);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
