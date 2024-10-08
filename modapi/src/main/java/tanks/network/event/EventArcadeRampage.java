package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.minigames.Arcade;

public class EventArcadeRampage extends PersonalEvent
{
    public int power;

    public EventArcadeRampage(int power)
    {
        this.power = power;
    }

    public EventArcadeRampage()
    {

    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(power);
    }

    @Override
    public void read(ByteBuf b)
    {
        power = b.readInt();
    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.currentGame instanceof Arcade)
        {
            ((Arcade) Game.currentGame).setRampage(power);
        }
    }
}
