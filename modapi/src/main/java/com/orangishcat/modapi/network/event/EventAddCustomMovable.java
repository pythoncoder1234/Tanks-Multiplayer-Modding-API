package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import tanks.CustomMovable;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;

public class EventAddCustomMovable extends PersonalEvent
{
    public double posX;
    public double posY;
    public String drawInstructions;

    public EventAddCustomMovable()
    {
    }

    public EventAddCustomMovable(CustomMovable m)
    {
        this.posX = m.posX;
        this.posY = m.posY;
        this.drawInstructions = m.drawInstructions;
    }

    public EventAddCustomMovable(double x, double y, String drawInstructions)
    {
        this.posX = x;
        this.posY = y;
        this.drawInstructions = drawInstructions;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        NetworkUtils.writeString(b, this.drawInstructions);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.drawInstructions = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {
        Game.movables.add(new CustomMovable(this.posX, this.posY).setDrawInstructions(this.drawInstructions));
    }
}
