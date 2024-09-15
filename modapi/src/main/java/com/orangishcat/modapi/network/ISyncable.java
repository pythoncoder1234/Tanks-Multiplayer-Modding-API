package com.orangishcat.modapi.network;

import com.orangishcat.modapi.ModAPI;
import com.orangishcat.modapi.NetworkFieldUtils;
import tanks.Game;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.reflect.Modifier.isStatic;

/**
 * An interface that allows an object to implement synced fields
 * using a {@link SyncedFieldMap}.
 * */
public interface ISyncable
{
    default void addFieldsToSync() {}

    default boolean skipSyncCheck() { return false; }

    /**
     * Registers the object into the ID system, do not call more than once.<br>
     * Both the server and the client must call this for syncing to work,
     * and the number of synced fields must be the same on both sides.<br>
     * */
    default void initSync()
    {
        try
        {
            for (Field f : this.getClass().getFields())
            {
                if (f.getType() == SyncedFieldMap.class)
                {
                    Object obj = f.get(this);
                    if (obj != null)
                        ((SyncedFieldMap) obj).register(this);
                }
                else if (ISyncable.class.isAssignableFrom(f.getType()))
                {
                    ISyncable obj = ((ISyncable) f.get(this));
                    if (obj != null)
                        obj.initSync();
                }
            }

            addFieldsToSync();
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * Stops the object from being synced across the network.
     * Server-side call only, and only call once. Not mandatory.
     * */
    default void removeSync()
    {
        if (ScreenPartyLobby.isClient)
            return;

        try
        {
            for (Field f : this.getClass().getFields())
            {
                if (f.getType() == SyncedFieldMap.class)
                {
                    Object obj = f.get(this);
                    if (obj != null)
                        ((SyncedFieldMap) obj).unregister();
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    default HashMap<SyncedFieldMap, ArrayList<Field>> getSyncedFields()
    {
        try
        {
            HashMap<SyncedFieldMap, ArrayList<Field>> syncedFields = new HashMap<>();

            for (Field f : this.getClass().getFields())
            {
                if (f.getType() == SyncedFieldMap.class)
                {
                    Object obj = f.get(this);
                    if (obj != null)
                        syncedFields.put((SyncedFieldMap) obj, ((SyncedFieldMap) obj).fieldsToSync);
                }
            }

            return syncedFields;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    default ArrayList<String> getSupportedFieldNames(String... excluded)
    {
        return ISyncable.getSupportedFieldNames(this.getClass(), excluded);
    }

    static ArrayList<String> getSupportedFieldNames(Class<?> cls, String... excluded)
    {
        ArrayList<String> fields = new ArrayList<>();

        for (Field f : cls.getFields())
        {
            if (!isStatic(f.getModifiers()) && ModAPI.listContains(f.getType(), NetworkFieldUtils.supportedTypes) && !ModAPI.listContains(f.getName(), excluded))
                fields.add(f.getName());
        }

        return fields;
    }
}
