import java.io.*;
import java.net.*;

/**
 * Created by lidaxia on 16/06/2017.
 */
public class TCPSampleClient {
	private String hostName;
	private int port;

	TCPSampleClient(String hostname, int port) {
		this.hostName = hostname;
		this.port = port;
	}

	void send(String message) {
		try
		{
			Socket clientSocket = new Socket(hostName, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			out.write(message);
			out.close();
			clientSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}