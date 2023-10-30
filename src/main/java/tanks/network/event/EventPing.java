package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;

public class EventPing implements INetworkEvent
{
	public int iteration = 0;

	public EventPing() {}

	public EventPing(int iteration)
	{
		this.iteration = iteration+1;
	}

	@Override
	public void write(ByteBuf b)
	{
		if (!Game.vanillaMode)
			b.writeInt(iteration);
	}

	@Override
	public void read(ByteBuf b)
	{
		if (!Game.vanillaMode)
			this.iteration = b.readInt();
	}

	@Override
	public void execute() 
	{
		
	}
}
