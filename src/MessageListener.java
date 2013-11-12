import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * @author Joel Denke and Mats Maatson
 * @description Socket incoming message thread reader
 */

public class MessageListener extends Thread
{
	private BufferedReader reader = null;
	private SIPClient client;
	private Thread myThread = null;
	
	/**
	 * @description - Create buffer reader
	 * @param s - Socket to read from
	 * @param c - Client
	 * @throws IOException
	 */
	public MessageListener(Socket s, SIPClient c) throws IOException
	{
		client = c;
		reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
	}
	
	
	/**
	 * @description - Start the reading thread
	 */
	public void startThread()
	{
    	if(myThread == null) {
    		myThread = new Thread(this);
    		myThread.start();
    	}
	}
	
	/**
	 * @description - Close current buffer reader if any and stop thread
	 */
	public void stopThread()
	{
		try {
			if (reader != null) reader.close();
		} catch (IOException e) {}
		
		myThread = null;
	}
	
	/**
	 * @description - Handles the incoming messages from the other SIP Client
	 * @param message - Message to handle
	 */
	public void handleMessage(String message)
	{
		System.out.println("Received message: " + message);
		String[] tokens = client.tokenize(message); // Split message into tokens, separator is white space
		StateMachine stateHandler = client.getStateMachine();
		
		if (tokens.length >= 1) {
				switch (tokens[0]) {
					case "INVITE" :
						if (tokens.length == 6 && tokens[2].equals(client.getID())) {
							client.initAudioStream();
							client.setAudioPort(Integer.parseInt(tokens[5])); // Setup port to send audio to when call is starting
							System.out.println("Process event invite");
							stateHandler.processEvent(Events.INVITE);
						}
						break;
					case "RINGING" :
						System.out.println("Process event ringing");
						stateHandler.processEvent(Events.RINGING);
						break;
					case "TRYING" :
						System.out.println("Process event trying");
						stateHandler.processEvent(Events.TRYING);
						break;
					case "OK" :
						if (tokens.length == 2) {
							client.setAudioPort(Integer.parseInt(tokens[1])); // Setup port to send audio to when call is starting
						}
						System.out.println("Process event ok");
						stateHandler.processEvent(Events.OK);
						break;
					case "ACK" :
						System.out.println("Process event ack");
						stateHandler.processEvent(Events.ACK);
						break;
					case "BYE" :
						System.out.println("Process event bye");
						stateHandler.processEvent(Events.BYE);
						break;
					case "BUSY" :
						System.out.println("Process event busy");
						stateHandler.processEvent(Events.BUSY);
						break;
					default :
						System.out.println("Invalid message received: " + tokens[0]);
						break;
				}
		}
	}
	
	/**
	 * @see java.lang.Thread#run()
	 * @description - Runs the thread listen for incoming messages on socket stream 
	 */
	public void run()
	{
		if (myThread != Thread.currentThread()) return;
		String message;
		
		while (myThread != null) {
			try {
				message = reader.readLine();
				if (message != null) {
					handleMessage(message); // If message is null it means other end suddenly closed
				} else {
					System.out.println("Other client suddenly disappeared");
					client.getStateMachine().processEvent(Events.START);
					stopThread();
					client.cleanup();
				}
			} catch (SocketTimeoutException ste) {
				// Got timeout, process it in state if we want to
				client.getStateMachine().processEvent(Events.TIMEOUT);
			} catch (SocketException se) {
				System.out.println("The other participant suddenly exit");
				stopThread();
				client.cleanup();
			} catch (IOException e) {
				System.out.println("Error while listening on new messages");
				stopThread();
				client.cleanup();
			} catch (Exception e2) {
				System.out.println("Unexpected error");
				stopThread();
				client.cleanup();
			}
		}
	}
}
