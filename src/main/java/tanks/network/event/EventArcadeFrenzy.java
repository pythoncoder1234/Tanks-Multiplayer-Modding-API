package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.minigames.Arcade;

public class EventArcadeFrenzy extends PersonalEvent
{
    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

    @Override
    public void execute()
    {
        if (Game.currentGame instanceof Arcade && clientID == null)
        {
            ((Arcade) Game.currentGame).frenzy = true;
            ((Arcade) Game.currentGame).frenzyTime = ((Arcade) Game.currentGame).age;
            Drawing.drawing.playSound("rampage.ogg");
        }
    }
}
