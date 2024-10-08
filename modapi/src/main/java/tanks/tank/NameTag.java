package tanks.tank;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.IDrawable;
import tanks.Movable;
import tanks.gui.TextWithStyling;
import tanks.network.ISyncable;
import tanks.network.SyncedFieldMap;

public class NameTag implements IDrawable, ISyncable
{
    public SyncedFieldMap map = new SyncedFieldMap();
    public boolean syncEnabled;    // Not useless! Accessed by a <? extends ISyncable>.class.getField("syncEnabled") call!

    public Tank tank;
    public double ox, oy, oz;
    public double size = 20;
    public TextWithStyling name;
    public int drawLevel = 9;

    private NameTag(Tank t)
    {
        this(t, 0, 0, 0, "");
    }

    public NameTag(Tank t, double ox, double oy, double oz, String name)
    {
        this(t, ox, oy, oz, name, -9999, 0, 0);
    }

    public NameTag(Movable m, double ox, double oy, double oz, String name, double colR, double colG, double colB)
    {
        this.tank = (Tank) m;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.name = new TextWithStyling(name, colR, colG, colB);
    }


    @Override
    public void draw()
    {
        if (this.name.colorR < -9000)
        {
            this.name.colorR = tank.colorR;
            this.name.colorG = tank.colorG;
            this.name.colorB = tank.colorB;
        }

        this.name.fontSize = size * (1 - this.tank.destroyTimer / Game.tile_size) * Math.min(this.tank.drawAge / Game.tile_size, 1);
        this.name.drawText(tank.posX + ox, tank.posY + oy, tank.posZ + oz);
        this.name.shadowColor().drawText(tank.posX + ox + 2, tank.posY + oy + 2, tank.posZ + oz);
    }

    @Override
    public void addFieldsToSync()
    {
        map.putAllSupportedFields("drawLevel");
    }

    public void writeTo(ByteBuf b)
    {
        this.name.writeTo(b);
        b.writeDouble(this.ox);
        b.writeDouble(this.oy);
        b.writeDouble(this.oz);
    }

    public static NameTag readFrom(ByteBuf b, Tank t)
    {
        TextWithStyling name = TextWithStyling.readFrom(b);
        NameTag n = new NameTag(t);
        n.name = name;
        n.ox = b.readDouble();
        n.oy = b.readDouble();
        n.oz = b.readDouble();
        return n;
    }
}
