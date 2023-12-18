package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Panel;
import tanks.gui.screen.ScreenInterlevel;
import tanks.network.NetworkUtils;

/** Not used in Mod API, gotta keep it for backwards compatability. */
public class EventReturnToCrusade extends PersonalEvent
{
    public String msg1;
    public String msg2;
    public boolean win;
    public boolean lose;

    public EventReturnToCrusade()
    {

    }

    public EventReturnToCrusade(Crusade c)
    {
        if (c.win)
        {
            msg1 = "You finished the crusade!";
            win = true;
        }
        else if (c.lose)
        {
            msg1 = "Game over!";
            lose = true;
        }
        else
        {
            if (Panel.win)
                msg1 = "Battle cleared!";
            else
                msg1 = "Battle failed!";
        }

        if (c.lifeGained)
            msg2 = "You gained a life for clearing Battle " + (c.currentLevel + 1) + "!";
        else
            msg2 = "";
    }

    @Override
    public void execute()
    {
        ScreenInterlevel.title = msg1;
        ScreenInterlevel.topText = msg2;
        Crusade.currentCrusade.win = win;
        Crusade.currentCrusade.lose = lose;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, msg1);
        NetworkUtils.writeString(b, msg2);
        b.writeBoolean(win);
        b.writeBoolean(lose);
    }

    @Override
    public void read(ByteBuf b)
    {
        msg1 = NetworkUtils.readString(b);
        msg2 = NetworkUtils.readString(b);
        win = b.readBoolean();
        lose = b.readBoolean();
    }
}