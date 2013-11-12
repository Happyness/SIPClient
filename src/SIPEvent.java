/**
 * @author Joel Denke and Mats Maatson
 * @description SIP Event class used by State machine
 * Each method represents one event
 */
public class SIPEvent
{
	protected SIPClient client;
	
	public SIPEvent(SIPClient c)
	{
		client = c;
	}
	
	public SIPEvent start()
	{
		return new StateStart(client);
	}

	public SIPEvent busy()
	{
		return this;
	}
	
	public SIPEvent invite()
	{
		return this;
	}
	public SIPEvent ok()
	{
		return this;
	}
	public SIPEvent trying()
	{
		return this;
	}
	public SIPEvent ringing()
	{
		return this;
	}
	public SIPEvent ack()
	{
		return this;
	}
	public SIPEvent bye()
	{
		return this;
	}
	public SIPEvent sendInvite()
	{
		return this;
	}
	public SIPEvent close()
	{
		return this;
	}

	public SIPEvent timeout()
	{
		return this;
	}
}
