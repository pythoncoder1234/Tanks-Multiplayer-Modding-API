package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.EndText;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenInterlevel;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventLevelEnd extends PersonalEvent
{
	public String winningTeams;
	public boolean custom;

	/**Win title, lose title, win subtitle, lose subtitle, win top text, lose top text*/
	public String wt, lt, ws, ls, wtt, ltt;

	public EventLevelEnd()
	{
		
	}
	
	public EventLevelEnd(String winners, EndText endText)
	{
		this.winningTeams = winners;
		custom = endText != EndText.normal;
		
		if (custom)
		{
			this.wt = endText.winTitle;
			this.lt = endText.loseTitle;
			this.ws = endText.winSubtitle;
			this.ls = endText.loseSubtitle;
			this.wtt = endText.winTopText;
			this.ltt = endText.loseTopText;
		}
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
			ScreenInterlevel.title = wt != null ? wt : "Victory!";
			ScreenInterlevel.subtitle = ws;
			ScreenInterlevel.topText = wtt;
		}
		else
		{
			Panel.win = false;
			ScreenInterlevel.title = lt != null ? lt : "You were destroyed!";
			ScreenInterlevel.subtitle = ls;
			ScreenInterlevel.topText = ltt;
		}

		Game.silentCleanUp();
		Game.exitToInterlevel();

		ScreenPartyLobby.readyPlayers.clear();
		ScreenPartyLobby.includedPlayers.clear();

	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.winningTeams);

		if (!Game.vanillaMode)
		{
			b.writeBoolean(custom);
			if (custom)
			{
				NetworkUtils.writeString(b, this.wt);
				NetworkUtils.writeString(b, this.lt);
				NetworkUtils.writeString(b, this.ws);
				NetworkUtils.writeString(b, this.ls);
				NetworkUtils.writeString(b, this.wtt);
				NetworkUtils.writeString(b, this.ltt);
			}
		}
	}

	@Override
	public void read(ByteBuf b)
	{
		this.winningTeams = NetworkUtils.readString(b);

		if (!Game.vanillaMode)
		{
			if (b.readBoolean())
			{
				this.wt = NetworkUtils.readString(b);
				this.lt = NetworkUtils.readString(b);
				this.ws = NetworkUtils.readString(b);
				this.ls = NetworkUtils.readString(b);
				this.wtt = NetworkUtils.readString(b);
				this.ltt = NetworkUtils.readString(b);
			}
		}
	}
}
