package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
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
	public String wt;
	public String lt;
	public String ws;
	public String ls;

	public boolean crusade = false;
	public boolean levelPassed = false;
	public int currentLevel;
	public boolean win;
	public boolean lose;

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
		}
	}

	/** For crusades */
	public EventLevelEnd(Crusade c)
	{
		this.crusade = true;
		this.levelPassed = Panel.win;
		this.currentLevel = c.currentLevel + (Panel.win && !c.replay ? 1 : 0);
		this.win = c.win;
		this.lose = c.lose;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		if (!crusade)
		{
			String[] teams = winningTeams.split(",");

			if (Game.listContains(Game.clientID.toString(), teams) || (Game.playerTank != null && Game.playerTank.team != null && Game.listContains(Game.playerTank.team.name, teams)))
			{
				Panel.win = true;
				ScreenInterlevel.title = wt != null ? wt : "Victory!";
				ScreenInterlevel.subtitle = ws;
			}
			else
			{
				Panel.win = false;
				ScreenInterlevel.title = lt != null ? lt : "You were destroyed!";
				ScreenInterlevel.subtitle = ls;
			}
		}
		else
		{
			Panel.win = levelPassed;
			ScreenInterlevel.title = levelPassed ? "Battle cleared!" : "Battle failed!";

			Crusade.currentCrusade.win = win;
			Crusade.currentCrusade.lose = lose;
			Crusade.currentCrusade.currentLevel = currentLevel;
			Crusade.currentCrusade.lifeGained = levelPassed && !Crusade.currentCrusade.replay && !Crusade.currentCrusade.win
					&& currentLevel > 0 && currentLevel % Crusade.currentCrusade.bonusLifeFrequency == 0;
		}

		Game.silentCleanUp();
		Game.exitToInterlevel();

		ScreenPartyLobby.readyPlayers.clear();
		ScreenPartyLobby.includedPlayers.clear();

		System.gc();
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeBoolean(crusade);
		if (!crusade)
		{
			NetworkUtils.writeString(b, this.winningTeams);
			
			b.writeBoolean(custom);
			if (custom)
			{
				NetworkUtils.writeString(b, this.wt);
				NetworkUtils.writeString(b, this.lt);
				NetworkUtils.writeString(b, this.ws);
				NetworkUtils.writeString(b, this.ls);
			}
		}
		else
		{
			b.writeBoolean(this.levelPassed);
			b.writeInt(this.currentLevel);
			b.writeBoolean(this.win);
			b.writeBoolean(this.lose);
		}
	}

	@Override
	public void read(ByteBuf b)
	{
		this.crusade = b.readBoolean();
		if (!this.crusade)
		{
			this.winningTeams = NetworkUtils.readString(b);
			
			if (b.readBoolean())
			{
				this.wt = NetworkUtils.readString(b);
				this.lt = NetworkUtils.readString(b);
				this.ws = NetworkUtils.readString(b);
				this.ls = NetworkUtils.readString(b);
			}
		}
		else
		{
			this.levelPassed = b.readBoolean();
			this.currentLevel = b.readInt();
			this.win = b.readBoolean();
			this.lose = b.readBoolean();
		}
	}
}
