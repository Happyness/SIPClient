import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Joel Denke and Mats Maatson
 * @description Listen for incoming socket connections
 */
public class ConnectionListener extends Thread
{
	private SIPClient client;
	private int port;
	private ServerSocket listener = null;
	private Thread myThread = null;
	
	/**
	 * @description Initiate constructor
	 * @param c - The SIP client
	 * @param port - Port to listen on
	 */
	public ConnectionListener(SIPClient c, int port)
	{
		this.port = port;
		client = c;
	}
	
	/**
	 * @description Start the thread
	 */
	public void startThread()
	{
    	if(myThread == null) {
    		myThread = new Thread(this);
    		myThread.start();
    	}
	}
	
	/**
	 * @description Stop the thread
	 */
	public void stopThread()
	{
		myThread = null;
	}
	
	/**
	 * @description Run the thread and listen for incoming messages
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		if (myThread != Thread.currentThread()) return;
		
		try {
			listener = new ServerSocket(port);
			
			while (myThread != null) {
				Socket socket = listener.accept();
				
				if (!client.isBusy()) {
					client.setBusy(true);
					client.initConnection(socket, client);
				} else {
					PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
					output.println("BUSY");
					output.close();
					socket.close();
				}
			}
		} catch (IOException ioe) {
			System.out.println("Failed init on server socket");
			client.stop();
			client.cleanup();
			return;
		} catch (Exception e2) {
			System.out.println("Unexpected error");
			client.stop();
			client.cleanup();
			return;
		}
	}
}
