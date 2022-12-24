package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.ModAPI;
import tanks.menus.FixedMenu;
import tanks.menus.FixedText;
import tanks.network.NetworkUtils;

public class EventChangeText extends PersonalEvent
{
    public String text;
    public double id;

    public EventChangeText(double id, String text)
    {
        this.text = text;
        this.id = id;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.id);
        NetworkUtils.writeString(b, this.text);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readDouble();
        this.text = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        FixedMenu m = ModAPI.ids.get(this.id);

        if (m instanceof FixedText)
            ((FixedText) m).text = this.text;
    }
}
