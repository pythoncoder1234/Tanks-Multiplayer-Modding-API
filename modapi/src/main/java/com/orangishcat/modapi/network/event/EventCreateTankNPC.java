package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.event.EventTankCreate;
import tanks.tank.Tank;
import tanks.tank.TankNPC;

public class EventCreateTankNPC extends EventTankCreate
{
    public TankNPC npc;

    public EventCreateTankNPC() {}

    public EventCreateTankNPC(Tank t)
    {
        super(t);
        npc = ((TankNPC) t);
    }

    @Override
    public void execute()
    {
        Game.movables.add(npc);
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        npc.writeTo(b);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        npc = TankNPC.readFrom(b, posX, posY, angle);
    }
}
