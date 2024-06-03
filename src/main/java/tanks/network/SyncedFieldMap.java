package tanks.network;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.event.EventSyncField;
import tanks.network.event.EventUnregisterSyncedMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * A map-like object that can sync fields across the network automatically.<br><br>
 * Objects using this must implement the {@link ISyncable} class.<br><br>
 * Must be registered either by calling the <code>register</code> function, or
 * by calling the <code>ISyncable.initSync</code> function.
 */
public class SyncedFieldMap
{
    public static final HashMap<Integer, SyncedFieldMap> mapIDs = new HashMap<>();
    public static final ArrayList<Integer> freeIDs = new ArrayList<>();
    protected static int currentID = 0;

    public int networkID = -1;
    public ISyncable object;
    public Class<?> objectClass;
    public ArrayList<Field> fieldsToSync = new ArrayList<>();
    protected HashMap<String, Object> prevValues = new HashMap<>();

    public SyncedFieldMap()
    {

    }

    protected int nextID()
    {
        if (!freeIDs.isEmpty())
            return freeIDs.remove(0);
        currentID++;
        return currentID - 1;
    }

    public void put(String fieldName)
    {
        try
        {
            Field f = objectClass.getField(fieldName);
            prevValues.put(fieldName, f.get(object));
            fieldsToSync.add(f);
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    public void putAll(String... fieldName)
    {
        for (String field : fieldName)
            put(field);
    }

    /**
     * Puts all fields satisfying the given conditions:<br>
     * <ul>
     *  <li>the field's class is in {@link NetworkUtils#supportedTypes supportedTypes}</li>
     *  <li>the field's name is not in the <code>excludedNames</code> parameter</li>
     *  <li>the field's name is not <code>syncEnabled</code> or <code>changedByAnimation</code>
     *  (variables used internally in many objects)</li>
     * </ul>
     *
     * @param excludedNames The names of the fields that are excluded from being added
     * @return A <code>HashSet</code> of names of fields added
     */
    public HashSet<String> putAllSupportedFields(String... excludedNames)
    {
        HashSet<String> addedFields = new HashSet<>();
        for (Field f : this.objectClass.getFields())
        {
            if (Game.listContains(f.getType(), NetworkUtils.supportedTypes) && !Game.listContains(f.getName(), excludedNames)
                    && !f.getName().equals("syncEnabled") && !f.getName().equals("changedByAnimation"))
            {
                put(f.getName());
                addedFields.add(f.getName());
            }
        }

        return addedFields;
    }

    public void remove(String name)
    {
        try
        {
            Field f = objectClass.getField(name);
            prevValues.remove(name);
            fieldsToSync.remove(f);
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    public void register(ISyncable objToSync)
    {
        if (isRegistered())
            return;

        object = objToSync;
        objectClass = objToSync.getClass();

        networkID = nextID();
        SyncedFieldMap.mapIDs.put(networkID, this);
    }

    /**
     * Unregisters the SyncedFieldMap from the network ID system.
     *
     * @see ISyncable
     */
    public void unregister()
    {
        if (!isRegistered())
            return;

        EventUnregisterSyncedMap e = new EventUnregisterSyncedMap(this);
        e.execute();
        Game.eventsOut.add(e);

        this.networkID = -1;
    }

    EventSyncField update()
    {
        if (object.skipSyncCheck())
            return null;

        try
        {
            ArrayList<SyncedField> fields = new ArrayList<>();

            for (Field f : fieldsToSync)
            {
                Object o = f.get(object);
                if (!prevValues.get(f.getName()).equals(o))
                {
                    fields.add(new SyncedField(f.getName(), o, this.networkID));
                    prevValues.put(f.getName(), o);
                }
            }

            if (!fields.isEmpty())
                return new EventSyncField(fields);

            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public int size()
    {
        return fieldsToSync.size();
    }

    public boolean isEmpty()
    {
        return fieldsToSync.isEmpty();
    }

    public boolean isRegistered()
    {
        return this.networkID > -1;
    }

    public static class SyncedField
    {
        public String name;
        public Object val;
        public int mapID;

        public SyncedField(String name, Object val, int mapID)
        {
            this.name = name;
            this.val = val;
            this.mapID = mapID;
        }

        public void writeTo(ByteBuf b)
        {
            b.writeInt(this.mapID);
            NetworkUtils.writeString(b, this.name);
            NetworkUtils.writeFields(b, this, "val");
        }

        public static void readFrom(ByteBuf b)
        {
            SyncedFieldMap map = mapIDs.get(b.readInt());
            String name = Objects.requireNonNull(NetworkUtils.readString(b));
            NetworkUtils.readFields(b, map.object, name);
        }
    }
}
