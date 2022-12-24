package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.ModAPI;
import tanks.menus.FixedText;
import tanks.network.NetworkUtils;

public class EventDisplayText extends EventAddFixedMenu
{
    public boolean customPos = false;
    public FixedText.types location = null;
    public String text;
    public boolean afterGameStarted;
    public int duration;
    public boolean hasItems = Game.currentLevel != null && (Game.currentLevel.shop.size() > 0 || Game.currentLevel.startingItems.size() > 0 || Crusade.crusadeMode);

    public double fontSize;

    public EventDisplayText()
    {

    }

    public EventDisplayText(FixedText t)
    {
        super(t);

        this.customPos = true;
        this.posX = t.posX;
        this.posY = t.posY;
        this.text = t.text;
        this.duration = (int) t.duration;
        this.afterGameStarted = t.afterGameStarted;

        this.colorR = t.colorR;
        this.colorG = t.colorG;
        this.colorB = t.colorB;
        this.fontSize = t.fontSize;
    }

    public EventDisplayText(FixedText t, boolean location)
    {
        this(t);

        if (!location)
            return;

        this.location = t.location;
        this.customPos = false;
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);

        b.writeBoolean(this.location == null);
        if (this.location == null)
        {
            b.writeDouble(this.posX);
            b.writeDouble(this.posY);
        }
        else
            NetworkUtils.writeString(b, this.location.toString());

        NetworkUtils.writeString(b, this.text);
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
        super.read(b);

        this.customPos = b.readBoolean();
        if (this.customPos)
        {
            this.posX = b.readDouble();
            this.posY = b.readDouble();
        }
        else
            this.location = FixedText.types.valueOf(NetworkUtils.readString(b));

        this.text = NetworkUtils.readString(b);
        this.afterGameStarted = b.readBoolean();
        this.hasItems = b.readBoolean();

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
        t.animations = this.animations;
        ModAPI.menuGroup.add(t);
    }
}
