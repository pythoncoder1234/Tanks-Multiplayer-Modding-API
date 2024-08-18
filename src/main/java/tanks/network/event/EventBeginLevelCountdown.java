package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventBeginLevelCountdown extends PersonalEvent
{	
	public EventBeginLevelCountdown()
	{
		
	}

	@Override
	public void execute() 
	{
		ScreenGame g = ScreenGame.getInstance();
		if (g != null && this.clientID == null)
		{
			g.shopScreen = false;
			g.cancelCountdown = false;
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		
	}

	@Override
	public void read(ByteBuf b)
	{
		
	}
}
