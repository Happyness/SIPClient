import java.util.Scanner;

/**
 * @author Joel Denke and Mats Maatson
 * @description Very simple Console for inviting and quitting
 */
public class Console extends Thread
{
	private SIPClient client;
	private Scanner scanner;
	private Thread myThread;
	
	/**
	 * @description Start scanner
	 * @param c - The SIP Client
	 */
	public Console(SIPClient c)
	{
		this.client = c;
		scanner = new Scanner(System.in);
	}
	
	/**
	 * @description Start thread
	 */
	public void startThread()
	{
    	if(myThread == null) {
    		myThread = new Thread(this);
    		myThread.start();
    	}
	}
	
	/**
	 * @description Stop thread
	 */
	public void stopThread()
	{
		myThread = null;
	}
	
	/**
	 * @description Run the console and wait for input
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		if (myThread != Thread.currentThread()) return;
		
		while (myThread != null && scanner.hasNextLine()) {
				System.out.print("Type:");
				String input = scanner.nextLine();
				String[] tokens = client.tokenize(input);
				
				switch (tokens[0]) {
					case "invite" :
						if (client.getStateMachine().isState("Start")) {
							int port = client.getListenPort();
							String ip = "localhost";
							
							if (tokens.length == 3) {
								String tmp = tokens[2];
								if (tmp.contains(":")) {
									String[] parts = tmp.split(":");
									port = Integer.parseInt(parts[1]);
									ip = parts[0];
								} else {
									ip = tmp;
									port = client.getListenPort(); // Default SIP Port 5060
								}
								
								client.setToID(tokens[1]);
							} else if (tokens.length == 2) {
								// Lazy load for using localhost:5060 or localhost:5061
								if (client.getListenPort() == 5060) {
									port = 5061;
								} else {
									port = 5060;
								}
								
								client.setToID(tokens[1]);
							} else {
								System.out.println("Invalid format, need to be invite <sip> <ip>");
							}
							
							if (client.initConnection(ip, port, client)) {
								client.getStateMachine().processEvent(Events.WAIT);
							}
						} else {
							System.out.println("Cannot send invite if not in start state");
						}
						break;
					case "quit" :
						if (client.getStateMachine().isState("Session")) {
							System.out.println("Initiate quit from console");
							client.getStateMachine().processEvent(Events.QUIT);
						} else {
							System.out.println("Can only quit when in active call");
						}
						break;
					case "exit" :
						client.stopThreads();
						client.cleanup();
						client.stop();
						System.exit(0);
						return;
					default:
						System.out.println("Invalid input, try again ...");
						break;
				}
		}
		
		scanner.close();
	}
}
