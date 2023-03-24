package tanks.tank;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;
import tanks.Movable;
import tanks.network.ISyncable;
import tanks.network.NetworkUtils;
import tanks.network.SyncedFieldMap;

public class NameTag implements IDrawable, ISyncable
{
    public SyncedFieldMap map = new SyncedFieldMap();
    public boolean syncEnabled;

    public Movable movable;
    public double ox;
    public double oy;
    public double oz;
    public double size = 20;
    public String name;
    public int drawLevel = 9;

    public double colorR;
    public double colorG;
    public double colorB;

    private static final String[] fieldNames = ISyncable.getSupportedFieldNames(NameTag.class, "drawLevel", "syncEnabled").toArray(new String[0]);

    public NameTag(Movable m, double ox, double oy, double oz, String name, double r, double g, double b)
    {
        this(m, ox, oy, oz, name, r, g, b, false);
    }

    public NameTag(Movable m, double ox, double oy, double oz, String name, double r, double g, double b, boolean syncEnabled)
    {
        this.movable = m;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.name = name;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;

        this.syncEnabled = syncEnabled;
    }

    private NameTag() {}

    @Override
    public void draw()
    {
        if (this.movable instanceof TankPlayerRemote)
            ((TankPlayerRemote) this.movable).drawName();
        else
        {
            Drawing.drawing.setFontSize(size);
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

            if (Game.enable3d)
                Drawing.drawing.drawText(movable.posX + ox, movable.posY + oy, movable.posZ + oz, name);
            else
                Drawing.drawing.drawText(movable.posX + ox, movable.posY + oy, name);
        }
    }

    @Override
    public void addFieldsToSync()
    {
        map.putAllSupportedFields("drawLevel");
    }

    public void writeTo(ByteBuf b)
    {
        NetworkUtils.writeFields(b, this, fieldNames);
    }

    public static NameTag readFrom(ByteBuf b)
    {
        NameTag t = new NameTag();
        NetworkUtils.readFields(b, t, fieldNames);
        return t;
    }
}
