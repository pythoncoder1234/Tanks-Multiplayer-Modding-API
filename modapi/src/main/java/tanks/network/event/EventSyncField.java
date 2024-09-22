package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.network.SyncedFieldMap.SyncedField;

import java.util.ArrayList;

public class EventSyncField extends PersonalEvent
{
    public ArrayList<SyncedField> fields;

    public EventSyncField() {}

    public EventSyncField(ArrayList<SyncedField> fields)
    {
        this.fields = fields;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(fields.size());
        for (SyncedField f : fields)
            f.writeTo(b);
    }

    @Override
    public void read(ByteBuf b)
    {
        int size = b.readInt();
        for (int i = 0; i < size; i++)
            SyncedField.readFrom(b);
    }

    @Override
    public void execute()
    {

    }
}
