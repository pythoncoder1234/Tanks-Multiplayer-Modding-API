package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Animations;
import tanks.menus.FixedMenu;
import tanks.network.NetworkUtils;

import java.util.ArrayList;

public class EventAddFixedMenu extends PersonalEvent
{
    public double posX;
    public double posY;
    public double duration;

    public double colorR;
    public double colorG;
    public double colorB;

    public ArrayList<Animations.Animation> animations = new ArrayList<>();

    public EventAddFixedMenu() {}

    public EventAddFixedMenu(FixedMenu m)
    {
        this.posX = m.posX;
        this.posY = m.posY;
        this.duration = m.duration;

        this.colorR = m.colorR;
        this.colorG = m.colorG;
        this.colorB = m.colorB;

        this.animations = m.animations;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.duration);

        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);

        b.writeInt(this.animations.size());
        for (Animations.Animation a : this.animations)
            NetworkUtils.writeString(b, a.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.duration = b.readDouble();

        this.colorR = b.readDouble();
        this.colorG = b.readDouble();
        this.colorB = b.readDouble();

        int size = b.readInt();
        for (int i = 0; i < size; i++)
            this.animations.add(Animations.Animation.fromString(NetworkUtils.readString(b)));
    }

    @Override
    public void execute()
    {

    }
}
