package tanks.gui.menus;

import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.network.event.EventAddCustomShape;

public class CustomShape extends FixedMenu
{
    public enum types
    {fillRect, fillOval, drawRect, drawOval}

    public types type;

    private final long defineTime = System.currentTimeMillis();

    public CustomShape(types type, double x, double y, double sizeX, double sizeY, double r, double g, double b)
    {
        this(type, x, y, sizeX, sizeY, 0, r, g, b, 255);
    }

    public CustomShape(types type, double x, double y, double sizeX, double sizeY, double r, double g, double b, double a)
    {
        this(type, x, y, sizeX, sizeY, 0, r, g, b, a);
    }

    public CustomShape(types type, double x, double y, double sizeX, double sizeY, double duration, double r, double g, double b, double a)
    {
        this.type = type;
        this.posX = x;
        this.posY = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        this.duration = duration;
        this.styling.colorR = r;
        this.styling.colorG = g;
        this.styling.colorB = b;
        this.styling.colorA = a;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.styling.colorR, this.styling.colorG, this.styling.colorB, this.styling.colorA);

        double x = Drawing.drawing.interfaceSizeX / 2 + this.posX;
        double y = Drawing.drawing.interfaceSizeY / 2 + this.posY;

        switch (this.type)
        {
            case fillRect:
                Drawing.drawing.fillRect(x, y, this.sizeX, this.sizeY);
                break;
            case fillOval:
                Drawing.drawing.fillOval(x, y, this.sizeX, this.sizeY);
                break;
            case drawRect:
                Drawing.drawing.drawRect(x, y, this.sizeX, this.sizeY);
                break;
            case drawOval:
                Drawing.drawing.drawOval(x, y, this.sizeX, this.sizeY);
                break;
        }
    }

    @Override
    public void update()
    {
        super.update();

        if (this.duration > 0 && System.currentTimeMillis() - defineTime > duration * 10L)
            ModAPI.removeMenus.add(this);
    }

    public void add()
    {
        ModAPI.fixedMenus.add(this);
        Game.eventsOut.add(new EventAddCustomShape(this));
    }
}
