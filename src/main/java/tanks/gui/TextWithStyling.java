package tanks.gui;

import io.netty.buffer.ByteBuf;
import tanks.network.ISyncable;
import tanks.network.NetworkUtils;
import tanks.network.SyncedFieldMap;

public class TextWithStyling implements ISyncable
{
    public SyncedFieldMap map = new SyncedFieldMap();
    public boolean changedByAnimation = false;

    public String text;

    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA;
    public double fontSize;

    public TextWithStyling(String text, double r, double g, double b, double a, double fontSize)
    {
        this.text = text;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
        this.fontSize = fontSize;
    }

    public TextWithStyling()
    {

    }

    @Override
    public void addFieldsToSync()
    {
        map.putAllSupportedFields();
    }

    public void writeTo(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.text);
        b.writeDouble(this.fontSize);

        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
        b.writeDouble(this.colorA);
    }

    public static TextWithStyling readFrom(ByteBuf b)
    {
        TextWithStyling t = new TextWithStyling();
        t.text = NetworkUtils.readString(b);
        t.fontSize = b.readDouble();

        t.colorR = b.readDouble();
        t.colorG = b.readDouble();
        t.colorB = b.readDouble();
        t.colorA = b.readDouble();

        return t;
    }
}
