package tanks.network.event;

import java.util.UUID;

public abstract class PersonalEvent implements INetworkEvent
{
	public UUID clientID;
	public UUID targetClient;

	public PersonalEvent setTargetClient(UUID clientID)
	{
		this.targetClient = clientID;
		return this;
	}
}
