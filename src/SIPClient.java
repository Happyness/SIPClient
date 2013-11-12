import java.util.Scanner;

/**
 * @author Joel Denke and Mats Maatson
 * @description Client main program, works as server/client
 */
public class SIPClient extends Client
{
	// Endast om man ska köra programmet via loopback localhost
	private static int LISTEN_PORT = 5060; // Default SIP Port
	
	/**
	 * @description - Just starts the client non static.
	 * @param args - Input args to java program
	 */
	public static void main(String[] args)
	{
		SIPClient client = new SIPClient();
		client.start();
	}
	
	/**
	 * @description Get current listening port
	 */
	public int getListenPort()
	{
		return LISTEN_PORT;
	}
	
	/**
	 * @description - Start the client
	 */
	public void start()
	{	
		Scanner s = new Scanner(System.in);
		System.out.print("Type port to listen on (5060-5061 is default for SIP):");
		LISTEN_PORT = Integer.parseInt(s.nextLine());
		System.out.print("What is your SIP ID:");
		String sipID = s.nextLine();
		setID(sipID);
		
		initListener(LISTEN_PORT, this);
		initStateMachine(this);
		initConsole(this);
		
		System.out.println("Welcome:\ninvite <sip_id> <ip:port> - Invite other client\nquit - Quit current conversation");
		System.out.println("exit - Exit program");

		while (isRunning()) {
		}
		
		stopThreads();
		s.close();
	}
}
