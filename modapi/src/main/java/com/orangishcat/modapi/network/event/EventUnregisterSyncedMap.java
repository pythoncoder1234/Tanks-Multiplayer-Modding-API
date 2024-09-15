package com.orangishcat.modapi.network.event;

import com.orangishcat.modapi.network.SyncedFieldMap;
import io.netty.buffer.ByteBuf;
import tanks.network.event.PersonalEvent;

public class EventUnregisterSyncedMap extends PersonalEvent
{
    public int id;

    public EventUnregisterSyncedMap() {}

    public EventUnregisterSyncedMap(SyncedFieldMap map)
    {
        this.id = map.networkID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
    }

    @Override
    public void execute()
    {
        SyncedFieldMap.mapIDs.remove(id);
        SyncedFieldMap.freeIDs.add(id);
    }
}
