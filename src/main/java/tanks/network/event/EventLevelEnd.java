package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenPartyInterlevel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.network.NetworkUtils;

public class EventLevelEnd extends PersonalEvent
{	
	public String winningTeams;

	public EventLevelEnd()
	{
		
	}
	
	public EventLevelEnd(String winners)
	{
		this.winningTeams = winners;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		String[] teams = winningTeams.split(",");

		if (Game.listContains(Game.clientID.toString(), teams) || (Game.playerTank != null && Game.playerTank.team != null && Game.listContains(Game.playerTank.team.name, teams)))
		{
			Panel.win = true;
			Panel.winlose = "Victory!";
		}
		else
		{
			Panel.win = false;
			Panel.winlose = "You were destroyed!";
		}

		for (Item i : Game.player.hotbar.itemBar.slots)
		{
			if (i instanceof ItemBullet)
				((ItemBullet) i).liveBullets = 0;
		}

		Game.silentCleanUp();
		Game.screen = new ScreenPartyInterlevel();

		ScreenPartyLobby.readyPlayers.clear();
		ScreenPartyLobby.includedPlayers.clear();

		System.gc();
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.winningTeams);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.winningTeams = NetworkUtils.readString(b);
	}
}
