/**
 * @author Joel Denke and Mats Maatson
 * @description All SIP states
 */

class StateStart extends SIPEvent
{
	public StateStart(SIPClient c)
	{
		super(c);
		System.out.println("Initiate start state");
	}

	@Override
	public SIPEvent sendInvite()
	{
		client.initAudioStream();
		client.setBusy(true);
		client.sendMessage("INVITE");
		return new StateWaitTrying(client);
	}
	
	@Override
	public SIPEvent invite()
	{
		client.initAudioStream();
		client.sendMessage("TRYING");
		client.sendMessage("RINGING");
		client.sendMessage("OK");
		return new StateWaitAck(client);
	}
}

class StateWaitTrying extends SIPEvent
{	
	public StateWaitTrying(SIPClient c)
	{
		super(c);
		System.out.println("Initiate waiting for trying state");
	}
	
	public SIPEvent busy()
	{
		return new StateStart(client);
	}
   
	@Override
	public SIPEvent trying()
	{
		return new StateWaitRinging(client);
	}
	
	public SIPEvent timeout()
	{
		System.out.println("Did not receive TRYING within timeout");
		client.cleanup();
		return new StateStart(client);
	}
}

class StateWaitRinging extends SIPEvent
{

	public StateWaitRinging(SIPClient c) {
		super(c);
		System.out.println("Initiate state wait for ringing");
	}
	
	public SIPEvent ringing()
	{
		return new StateWaitOK(client);
	}

	public SIPEvent timeout()
	{
		System.out.println("Did not receive RINGING within timeout");
		client.cleanup();
		return new StateStart(client);
	}
}

class StateWaitOK extends SIPEvent
{

	public StateWaitOK(SIPClient c)
	{
		super(c);
		System.out.println("Initiate state wait for OK");
	}
	
	public SIPEvent ok()
	{
		client.sendMessage("ACK");
		return new StateInSession(client);
	}
	
	public SIPEvent timeout()
	{
		System.out.println("Did not receive OK within timeout");
		client.cleanup();
		return new StateStart(client);
	}
}

class StateInSession extends SIPEvent
{
	public StateInSession(SIPClient c)
	{
		super(c);
		c.startAudioStream();
		System.out.println("Initiate SESSION State");
	}
	
	public SIPEvent bye()
	{
		client.sendMessage("OK");
		client.cleanup();
		return new StateStart(client);
	}
	
	public SIPEvent close()
	{
		client.sendMessage("BYE");
		return new StateCloseCall(client);
	}
}

class StateCloseCall extends SIPEvent
{
	public StateCloseCall(SIPClient c)
	{
		super(c);
		System.out.println("Initiate close call State");
	}

	public SIPEvent ok()
	{
		client.cleanup();
		return new StateStart(client);
	}
	
	public SIPEvent timeout()
	{
		System.out.println("Did not receive OK within timeout");
		client.cleanup();
		return new StateStart(client);
	}
}

class StateWaitAck extends SIPEvent
{
	public StateWaitAck(SIPClient c)
	{
		super(c);
		System.out.println("Initiate State wait for ack");
	}
	
	@Override
	public SIPEvent ack()
	{
		return new StateInSession(client);
	}
	
	public SIPEvent timeout()
	{
		System.out.println("Did not receive ACK within timeout");
		client.cleanup();
		return new StateStart(client);
	}
}