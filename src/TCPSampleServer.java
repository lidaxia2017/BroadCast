import java.io.*;
import java.net.*;

/**
 * Created by lidaxia on 16/06/2017.
 */
public class TCPSampleServer {
	private String hostName;
	private int port;
    private ServerSocket serverSock;

	TCPSampleServer(String hostname, int port) {
		this.hostName = hostname;
		this.port = port;
	}

	ServerSocket getServerSocket() {
		return serverSock;
	}

	void go() {
		try
		{
            serverSock = new ServerSocket(this.port);
			System.out.println("Server is running on " + this.hostName + " with port number: " + this.port);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}