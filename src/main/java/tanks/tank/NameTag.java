package tanks.tank;

import tanks.Game;
import tanks.IDrawable;
import tanks.Movable;
import tanks.gui.TextWithStyling;
import tanks.network.ISyncable;
import tanks.network.SyncedFieldMap;

public class NameTag implements IDrawable, ISyncable
{
    public SyncedFieldMap map = new SyncedFieldMap();
    public boolean syncEnabled;

    public Tank tank;
    public double ox, oy, oz;
    public double size = 20;
    public TextWithStyling name;
    public int drawLevel = 9;

    public NameTag(Tank t, double ox, double oy, double oz, String name)
    {
        this(t, ox, oy, oz, name, t.colorR, t.colorG, t.colorB);
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
        this.name.fontSize = size * (1 - this.tank.destroyTimer / Game.tile_size) * Math.min(this.tank.drawAge / Game.tile_size, 1);
        this.name.drawText(tank.posX + ox, tank.posY + oy, tank.posZ + oz);
        this.name.shadowColor().drawText(tank.posX + ox + 2, tank.posY + oy + 2, tank.posZ + oz);
    }

    @Override
    public void addFieldsToSync()
    {
        map.putAllSupportedFields("drawLevel");
    }
}
