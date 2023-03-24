package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.ModAPI;
import tanks.gui.menus.CustomShape;
import tanks.network.NetworkUtils;

public class EventAddCustomShape extends EventAddFixedMenu
{
    public CustomShape.types type;
    public double sizeX;
    public double sizeY;
    public double duration;

    public EventAddCustomShape()
    {

    }

    public EventAddCustomShape(CustomShape shape)
    {
        this.type = shape.type;
        this.posX = shape.posX;
        this.posY = shape.posY;
        this.sizeX = shape.sizeX;
        this.sizeY = shape.sizeY;

        this.duration = shape.duration;
        this.styling = shape.styling;
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);

        NetworkUtils.writeString(b, this.type.toString());
        b.writeDouble(this.sizeX);
        b.writeDouble(this.sizeY);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);

        this.type = CustomShape.types.valueOf(NetworkUtils.readString(b));
        this.sizeX = b.readInt();
        this.sizeY = b.readInt();
    }

    @Override
    public void execute()
    {
        CustomShape c = new CustomShape(this.type, this.posX, this.posY, this.sizeX, this.sizeY, this.duration, 69, 42, 0);
        c.styling = styling;
        c.animations = this.animations;
        ModAPI.fixedMenus.add(c);
    }
}
