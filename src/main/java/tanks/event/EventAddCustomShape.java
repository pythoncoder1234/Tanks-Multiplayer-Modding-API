package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;
import tanks.menus.CustomShape;
import tanks.network.NetworkUtils;

public class EventAddCustomShape extends EventAddFixedMenu
{
    public CustomShape.types type;
    public int sizeX;
    public int sizeY;
    public int duration;
    public int colorA;

    public EventAddCustomShape()
    {

    }

    public EventAddCustomShape(CustomShape.types type, int x, int y, int sizeX, int sizeY, int duration, int r, int g, int b, int a)
    {
        this.type = type;
        this.posX = x;
        this.posY = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        this.duration = duration;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);

        NetworkUtils.writeString(b, this.type.toString());
        b.writeDouble(this.sizeX);
        b.writeDouble(this.sizeY);
        b.writeDouble(this.colorA);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);

        this.type = CustomShape.types.valueOf(NetworkUtils.readString(b));
        this.sizeX = b.readInt();
        this.sizeY = b.readInt();
        this.colorA = b.readInt();
    }

    @Override
    public void execute()
    {
        CustomShape c = new CustomShape(this.type, this.posX, this.posY, this.sizeX, this.sizeY, this.duration, this.colorR, this.colorG, this.colorB, this.colorA);
        c.animations = this.animations;
        ModAPI.menuGroup.add(c);
    }
}
