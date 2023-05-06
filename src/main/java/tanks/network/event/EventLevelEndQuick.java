package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenInterlevel;
import tanks.network.NetworkUtils;

public class EventLevelEndQuick extends PersonalEvent
{
    public String winningTeams;

    public EventLevelEndQuick() {}

    public EventLevelEndQuick(String winners)
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
            ScreenInterlevel.title = "Victory!";
            Drawing.drawing.playSound("win.ogg", 1.0f, true);
        }
        else
        {
            Panel.win = false;
            ScreenInterlevel.title = "You were destroyed!";
            Drawing.drawing.playSound("lose.ogg", 1.0f, true);
        }

        ScreenGame.finishedQuick = true;
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
