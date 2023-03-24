package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;
import tanks.gui.menus.FixedMenu;
import tanks.gui.menus.RemoteScoreboard;
import tanks.network.NetworkUtils;

public class EventScoreboardUpdateScore extends PersonalEvent
{
    public double id;
    public int start;
    public int end;
    public String name;
    public double value;

    public EventScoreboardUpdateScore()
    {
    }

    public EventScoreboardUpdateScore(double id, int start, int end, String name, double value)
    {
        this.id = id;
        this.start = start;
        this.end = end;
        this.name = name;
        this.value = value;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(id);
        b.writeInt(start);
        b.writeInt(end);

        NetworkUtils.writeString(b, name);
        b.writeDouble(this.value);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readDouble();
        this.start = b.readInt();
        this.end = b.readInt();

        this.name = NetworkUtils.readString(b);
        this.value = b.readDouble();
    }

    @Override
    public void execute()
    {
        for (FixedMenu m : ModAPI.fixedMenus)
        {
            if (m instanceof RemoteScoreboard && m.id == this.id)
            {
                ((RemoteScoreboard) m).updateScore(start, end, this.name, this.value);
                break;
            }
        }
    }
}
