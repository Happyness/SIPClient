/**
 * @author Joel Denke and Mats Maatson
 * @description Enum containing all available SIP Events
 */
enum Events {
	INVITE,
	START,
	TRYING,
	RINGING,
	WAIT,
	OK,
	ACK,
	BYE,
	BUSY,
	QUIT,
	TIMEOUT
}

/**
 * @author Joel Denke and Mats Maatson
 * @description State machine
 */

/**
 * @author joel
 *
 */
public class StateMachine
{
	private SIPEvent currentState;
	
	/**
	 * @description Initiate Start State
	 * @param c - The SIP Client
	 */
	public StateMachine(SIPClient c)
	{
		currentState = new StateStart(c);
	}
	
	/**
	 * @description Check current state
	 * @param state - State to match
	 */
	public boolean isState(String state)
	{
		return currentState.getClass().toString().contains(state);
	}
	
	/**
	 * @description Process event in state machine
	 * @param event - SIP event to process
	 */
	public void processEvent(Events event)
	{
		switch (event)
		{
			case START:  currentState = currentState.start(); break;
			case TIMEOUT: currentState = currentState.timeout(); break;
			case QUIT: currentState = currentState.close(); break;
			case BUSY: currentState = currentState.busy(); break;
			case INVITE: currentState = currentState.invite(); break;
			case WAIT: currentState = currentState.sendInvite(); break;
			case TRYING: currentState = currentState.trying(); break;
			case RINGING: currentState = currentState.ringing(); break;
			case OK: currentState = currentState.ok(); break;
			case ACK: currentState = currentState.ack(); break;
			case BYE: currentState = currentState.bye(); break;
		}
	}
}