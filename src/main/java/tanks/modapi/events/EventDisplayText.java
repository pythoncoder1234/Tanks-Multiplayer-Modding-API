package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.modapi.ModAPI;
import tanks.modapi.menus.FixedText;
import tanks.network.NetworkUtils;

public class EventDisplayText extends PersonalEvent
{
    public boolean customPos = false;
    public double posX;
    public double posY;
    public FixedText.types location = null;
    public String text;
    public boolean afterGameStarted;
    public int duration;
    public boolean hasItems = Game.currentLevel != null && (Game.currentLevel.shop.size() > 0 || Game.currentLevel.startingItems.size() > 0 || Crusade.crusadeMode);

    public double colorR;
    public double colorG;
    public double colorB;
    public double fontSize;

    public EventDisplayText()
    {

    }

    public EventDisplayText(double x, double y, String text, boolean afterGameStarted, double fontSize, int duration, double r, double g, double b)
    {
        this.customPos = true;
        this.posX = x;
        this.posY = y;
        this.text = text;
        this.duration = duration;
        this.afterGameStarted = afterGameStarted;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.fontSize = fontSize;
    }

    public EventDisplayText(FixedText.types location, String text, boolean afterGameStarted, double fontSize, int duration, double r, double g, double b)
    {
        this.location = location;
        this.text = text;
        this.duration = duration;
        this.afterGameStarted = afterGameStarted;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.fontSize = fontSize;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeBoolean(this.location == null);
        if (this.location == null)
        {
            b.writeDouble(this.posX);
            b.writeDouble(this.posY);
        }
        else
            NetworkUtils.writeString(b, this.location.toString());

        NetworkUtils.writeString(b, this.text);
        b.writeInt(this.duration);
        b.writeBoolean(this.afterGameStarted);
        b.writeBoolean(this.hasItems);

        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
        b.writeDouble(this.fontSize);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.customPos = b.readBoolean();
        if (this.customPos)
        {
            this.posX = b.readDouble();
            this.posY = b.readDouble();
        }
        else
            this.location = FixedText.types.valueOf(NetworkUtils.readString(b));

        this.text = NetworkUtils.readString(b);
        this.duration = b.readInt();
        this.afterGameStarted = b.readBoolean();
        this.hasItems = b.readBoolean();

        this.colorR = b.readDouble();
        this.colorG = b.readDouble();
        this.colorB = b.readDouble();
        this.fontSize = b.readDouble();
    }

    @Override
    public void execute()
    {
        FixedText t;

        if (!customPos)
            t = new FixedText(this.location, this.text, this.colorR, this.colorG, this.colorB, this.fontSize);
        else
            t = new FixedText(this.posX, this.posY, this.text, this.colorR, this.colorG, this.colorB, this.fontSize);

        t.afterGameStarted = afterGameStarted;
        t.duration = duration;
        t.hasItems = this.hasItems;
        ModAPI.menuGroup.add(t);
    }
}
