package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.gui.screen.ScreenGame;

public class EventEnterLevel extends PersonalEvent
{
	public boolean prevReplay;

	public EventEnterLevel() {}

	@Override
	public void execute()
	{
		if (this.clientID == null)
		{
			Game.screen = new ScreenGame();

			if (Game.autoReady)
				Game.eventsOut.add(new EventPlayerAutoReady());

			if (!Crusade.crusadeMode)
			{
				Crusade.currentCrusade = null;
			}
			else if (Crusade.currentCrusade.replay != prevReplay)
			{
				if (prevReplay)
					Crusade.currentCrusade.currentLevel++;
				else
					Crusade.currentCrusade.currentLevel--;
			}
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		b.writeBoolean(Crusade.currentCrusade != null && Crusade.currentCrusade.replay);
	}

	@Override
	public void read(ByteBuf b)
	{
		boolean replay = b.readBoolean();
		if (Crusade.currentCrusade != null)
		{
			prevReplay = Crusade.currentCrusade.replay;
			Crusade.currentCrusade.replay = replay;
		}
	}
}
