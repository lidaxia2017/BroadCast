import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by lidaxia on 16/06/2017.
 */
public class SpanningTreeConstructor {
	private int myNodeIdx;
	private int initiator;
	private TreeNode[] tn;

	SpanningTreeConstructor(int idx,int initiator,TreeNode[] tn) {
		this.myNodeIdx = idx;
		this.initiator = initiator;
		this.tn = tn;
	}

	/*
	* QUERY, ACCEPT, REJECT messages are used to construct spanning tree.
	* FINISH message is used to tell parent node current node finished building tree.
	* When the initiator receives all FINISH messages from its children, it will send TERMINATION to children.
	* After receive TERMINATION, current node can continue to do broadcast. Initiator will do broadcast after sending TERMINATION.
	*/
	void generate(ServerSocket serverSock) {
		boolean connected = false;
		boolean terminated = false;
		String QUERY = "QUERY";
		String ACCEPT = "ACCEPT";
		String REJECT = "REJECT";
		String FINISH = "FINISH";
		String TERMINATION = "TERMINATION";
		int treeNeighbours = 0;
		boolean sendFinish = false;
		String message;
		int messageCount = 0;
		if (initiator == myNodeIdx) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			connected = true;
			for (int n : tn[myNodeIdx].getNeighbours()) {
				sendMessageToNeighbour(tn[n].getHostName(), tn[n].getPort(), myNodeIdx + " " + QUERY);
				messageCount++;
			}
		}
		try
		{
			while (!terminated) {
				Socket sock = serverSock.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				message = reader.readLine();
				if (message != null) {
					String[] args = message.split(" ");
					int src = Integer.parseInt(args[0]);
					message = args[1];
					if (message.equals(QUERY)) {
						if (!connected) {
							connected = true;
							tn[myNodeIdx].addTreeNeighbour(src);
							sendMessageToNeighbour(tn[src].getHostName(), tn[src].getPort(), myNodeIdx + " " + ACCEPT);
							for (int n : tn[myNodeIdx].getNeighbours()) {
								sendMessageToNeighbour(tn[n].getHostName(), tn[n].getPort(), myNodeIdx + " " + QUERY);
								messageCount++;
							}
						} else {
							sendMessageToNeighbour(tn[src].getHostName(), tn[src].getPort(), myNodeIdx + " " + REJECT);
						}
					} else if (message.equals(ACCEPT)) {
						tn[myNodeIdx].addTreeNeighbour(src);
						messageCount--;
					} else if (message.equals(REJECT)) {
						messageCount--;
					} else if (message.equals(FINISH)) {
						treeNeighbours++;
					} else if (message.equals(TERMINATION)) {
						for (int n : tn[myNodeIdx].getTreeNeighbour()) {
							if (n != src) {
								sendMessageToNeighbour(tn[n].getHostName(), tn[n].getPort(), myNodeIdx + " " + TERMINATION);
								messageCount++;
							}
						}
						terminated = true;
						System.out.println(myNodeIdx + "'s treeNeighbours :" + tn[myNodeIdx].getTreeNeighbour());
					}
					if (messageCount == 0 && (!sendFinish)) {
						if (initiator != myNodeIdx && (treeNeighbours + 1) == tn[myNodeIdx].getTreeNeighbour().size()){
							int n = tn[myNodeIdx].getTreeNeighbour().get(0);
							sendMessageToNeighbour(tn[n].getHostName(), tn[n].getPort(), myNodeIdx + " " + FINISH);
							sendFinish = true;
						} else if(treeNeighbours == tn[myNodeIdx].getTreeNeighbour().size()){
							for (int n : tn[myNodeIdx].getTreeNeighbour()) {
								sendMessageToNeighbour(tn[n].getHostName(), tn[n].getPort(), myNodeIdx + " " + TERMINATION);
							}
							terminated = true;
							System.out.println(myNodeIdx + "'s treeNeighbours :" + tn[myNodeIdx].getTreeNeighbour());
						}
					}
				}
				reader.close();
				sock.close();
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private void sendMessageToNeighbour(String hostname, int port, String message) {
		TCPSampleClient SampleClientObj = new TCPSampleClient(hostname, port);
		SampleClientObj.send(message);
	}
}
