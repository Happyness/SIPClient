import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

/**
 * @author Joel Denke and Mats Maatson
 * @description All SIP states
 */

public abstract class Client
{
	private boolean running = true;
	private boolean busy = false;
	
	private PrintWriter output = null;
	private Socket socket = null;
	private StateMachine stateMachine;
	private AudioStream stream;
	private MessageListener incoming;
	private ConnectionListener listener;
	private Console console;
	
	private String id;
	private String to_id;
	private int audioPort;
	
	/**
	 * @description Check if client is busy for new connections
	 */
	public boolean isBusy()
	{
		return busy;
	}
	
	/**
	 * @description Set client busy or not busy for new connections
	 * @param mode - If busy or not
	 */
	public void setBusy(boolean mode)
	{
		busy = mode;
	}
	
	/**
	 * @description Start Console thread
	 * @param client - The SIP Client
	 */
	public void initConsole(SIPClient client)
	{
		 console = new Console(client);
		 console.startThread();
	}	
	
	/**
	 * @description Start connection listener
	 * @param client - The SIP Client
	 * @param port - Port to listen on
	 */
	public void initListener(int port, SIPClient client)
	{
		 System.out.println("Init listener on port " + port);
		 listener = new ConnectionListener(client, port);
		 listener.startThread();
	}
	
	/**
	 * @description Initiate state machine
	 * @param client - The SIP client
	 */
	public void initStateMachine(SIPClient client)
	{
		stateMachine = new StateMachine(client);
	}
	
	/**
	 * @description get the state machine
	 */
	public synchronized StateMachine getStateMachine()
	{
		return stateMachine;
	}
	
	/**
	 * @param port - Set send port for audio stream
	 */
	public void setAudioPort(int port)
	{
		audioPort = port;
	}

	/**
	 * @description Check if the client still is running
	 */
	public boolean isRunning()
	{
		return running;
	}
	
	/**
	 * @description Close audio stream if any available
	 */
	public void closeAudioStream()
	{
		if (stream != null) {
			stream.stopStreaming();
			stream.close();
		}
	}
	
	/**
	 * @description Initiate the audio stream
	 */
	public void initAudioStream()
	{
		try {
			stream = new AudioStream();
			System.out.println("Initatied audio stream");
		} catch (IOException e) {
			System.out.println("Failed initiate audio stream");
			cleanup();
			stateMachine.processEvent(Events.START);
		}
	}
	
	/**
	 * @description Connect audio to remote an start streaming
	 */
	public void startAudioStream()
	{
		try {
			if (stream == null) {
				initAudioStream();
			}
			stream.connectTo(InetAddress.getByName(getToIP()), audioPort);
			stream.startStreaming();
			System.out.println("Started audio stream on port: " + audioPort);
		} catch (Exception e) {
			System.out.println("Failed connect to audio");
			cleanup();
			stateMachine.processEvent(Events.START);
		}
	}
	
	/**
	 * @description Start connection 
	 * @param ip - Ip or hostname to connect to
	 * @param port - port connect to
	 * @param c - The Sip Client
	 */
	public boolean initConnection(String ip, int port, SIPClient c)
	{
		try {
			Socket socket = new Socket(InetAddress.getByName(ip), port);
			System.out.println("Started socket on: " + ip + ":" + port);
			return initConnection(socket, c);
		} catch (ConnectException se) {
			System.out.println("Failed connect to socket");
			cleanup();
		} catch (IOException e) {
			System.out.println("Failed init socket connection");
			cleanup();
		}
		
		return false;
	}
	
	/**
	 * @description Start listening for messages and define output source
	 * @param s - Socket to use for read/write
	 * @param c - The Sip Client
	 */
	public boolean initConnection(Socket s, SIPClient c)
	{
		try {
			this.socket = s;
			s.setSoTimeout(7000); // Read timeout 7 seconds
			
			incoming = new MessageListener(socket, c);
			incoming.startThread();
			output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			System.out.println("Init connection on port: " + socket.getPort() + " and ip: " + getToIP());
			return true;
		} catch (SocketException e) {
			System.out.println("TCP Error when trying to set timeout");
			cleanup();
		} catch (IOException e) {
			System.out.println("Failed init connection");
			cleanup();
		} 
		return false;
	}
	
	/**
	 * @description Stop all threads except main thread
	 */
	public synchronized void stopThreads()
	{
		if (incoming != null) incoming.stopThread();
		if (listener != null) listener.stopThread();
		if (console != null) console.stopThread();
	}
	
	/**
	 * @description Cleanup the mess: close audio, incoming thread and socket,
	 * and make myself available again.
	 */
	public synchronized void cleanup()
	{
		closeAudioStream();
		if (incoming != null) incoming.stopThread();
		
		try {
			if (output != null) output.close();
			if (socket != null) socket.close();
		} catch (Exception e) {}
		
		setBusy(false);
	}
	
	/**
	 * @description Stop client from running further
	 */
	public synchronized void stop()
	{
		running = false;
	}
	
	/**
	 * @description Get current SIP ID
	 */
	public String getID()
	{
		return id;
	}
	
	
	/**
	 * @description Get current SIP ID to send to
	 */
	public String getToID()
	{
		return to_id;
	}
	
	/**
	 * @description Set my current SIP ID
	 * @param id - SIP ID
	 */
	public void setID(String id)
	{
		this.id = id;
	}
	
	
	/**
	 * @description Set current SIP ID to send to
	 * @param id - SIP id
	 */
	public void setToID(String id)
	{
		to_id = id;
	}
	
	/**
	 * @description Get current socket
	 */
	public synchronized Socket getSocket()
	{
		return socket;
	}
	
	/**
	 * @description - Split string into tokens using default separator white space
	 * @param message - Message to tokenize
	 */
	public synchronized String[] tokenize(String message)
	{
		StringTokenizer tokenizer = new StringTokenizer(message);
		String[] tokens = new String[tokenizer.countTokens()];
		int n = 0;
		
		while (tokenizer.hasMoreTokens()) {
			tokens[n++] = tokenizer.nextToken();
		}
		
		return tokens;
	}
	
	/**
	 * @description Get IP Adress from socket to send to
	 */
	public String getToIP()
	{
		return socket.getInetAddress().getHostAddress();
	}
	
	/**
	 * @description Get my own IP Adress from socket
	 */
	public String getFromIP()
	{
		return socket.getLocalAddress().getHostAddress();
	}

	/**
	 * @description - Send message over network, using socket and streams
	 * @param message - Message to send over network
	 */
	public synchronized void sendMessage(String message)
	{
		switch (message) {
			case "INVITE" :
				message += " " + getID() + " " + getToID() + " " +  getFromIP() + " " + getToIP() + " " + stream.getLocalPort(); 
				break;
			case "OK" :
				message += " " + stream.getLocalPort();
				break;
		}
		
		output.println(message);
		System.out.println("Sent message: " + message);
	}
}
