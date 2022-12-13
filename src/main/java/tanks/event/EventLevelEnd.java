package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenPartyInterlevel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventLevelEnd extends PersonalEvent
{	
	public String winningTeam;
	public String winMessage;
	public String loseMessage;
	public String winSubtitle;
	public String loseSubtitle;
	
	public EventLevelEnd()
	{
		
	}
	
	public EventLevelEnd(String winner, String winMessage, String loseMessage, String winSubtitle, String loseSubtitle)
	{
		if (winMessage.length() > 100)
			winMessage = winMessage.substring(0, 100);

		if (loseMessage.length() > 100)
			loseMessage = loseMessage.substring(0, 100);

		if (winSubtitle.length() > 100)
			winSubtitle = winSubtitle.substring(0, 100);

		if (loseSubtitle.length() > 100)
			loseSubtitle = loseSubtitle.substring(0, 100);

		this.winningTeam = winner;
		this.winMessage = winMessage;
		this.loseMessage = loseMessage;
		this.winSubtitle = winSubtitle;
		this.loseSubtitle = loseSubtitle;
	}

	@Override
	public void execute()
	{
		if (this.clientID != null)
			return;

		if (Game.clientID.toString().equals(winningTeam) || (Game.playerTank != null && Game.playerTank.team != null && Game.playerTank.team.name.equals(this.winningTeam)))
		{
			Panel.win = true;
			Panel.winlose = this.winMessage;
			Panel.subtitle = this.winSubtitle;
		}
		else
		{
			Panel.win = false;
			Panel.winlose = this.loseMessage;
			Panel.subtitle = this.loseSubtitle;
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
		NetworkUtils.writeString(b, this.winningTeam);
		NetworkUtils.writeString(b, this.winMessage);
		NetworkUtils.writeString(b, this.loseMessage);
		NetworkUtils.writeString(b, this.winSubtitle);
		NetworkUtils.writeString(b, this.loseSubtitle);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.winningTeam = NetworkUtils.readString(b);
		this.winMessage = NetworkUtils.readString(b);
		this.loseMessage = NetworkUtils.readString(b);
		this.winSubtitle = NetworkUtils.readString(b);
		this.loseSubtitle = NetworkUtils.readString(b);
	}
}
