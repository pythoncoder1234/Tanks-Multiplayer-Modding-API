package com.orangishcat.modapi.network.event;

import com.orangishcat.modapi.network.SyncedFieldMap;
import io.netty.buffer.ByteBuf;
import tanks.network.event.PersonalEvent;

import java.util.ArrayList;

public class EventSyncField extends PersonalEvent
{
    public ArrayList<SyncedFieldMap.SyncedField> fields;

    public EventSyncField() {}

    public EventSyncField(ArrayList<SyncedFieldMap.SyncedField> fields)
    {
        this.fields = fields;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(fields.size());
        for (SyncedFieldMap.SyncedField f : fields)
            f.writeTo(b);
    }

    @Override
    public void read(ByteBuf b)
    {
        int size = b.readInt();
        for (int i = 0; i < size; i++)
            SyncedFieldMap.SyncedField.readFrom(b);
    }

    @Override
    public void execute()
    {

    }
}
