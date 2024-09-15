package com.orangishcat.modapi.network.event;

import io.netty.buffer.ByteBuf;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;
import tanks.tank.Tank;
import tanks.tank.TankNPC;

public class EventChangeNPCMessage extends PersonalEvent
{
    public String[] messages;
    public int id;

    public EventChangeNPCMessage()
    {
    }

    public EventChangeNPCMessage(TankNPC t)
    {
        this.messages = t.messages.raw;
        this.id = t.networkID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);

        NetworkUtils.writeString(b, String.join("\n", messages));
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.messages = NetworkUtils.readString(b).split("\n");
    }

    @Override
    public void execute()
    {
        TankNPC t = (TankNPC) Tank.idMap.get(this.id);
        t.messages.raw = this.messages;
        t.messageNum = 0;
        t.initMessageScreen();
    }
}
