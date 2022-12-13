package tanks.event;

import io.netty.buffer.ByteBuf;

public class EventPing implements INetworkEvent
{
	public short iteration = 0;

	public EventPing()
	{
		
	}

	public EventPing(short iteration)
	{
		this.iteration = (short) (iteration + 1);
	}

	@Override
	public void write(ByteBuf b)
	{
		b.writeShort(iteration);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.iteration = b.readShort();
	}

	@Override
	public void execute() 
	{
		
	}
}
