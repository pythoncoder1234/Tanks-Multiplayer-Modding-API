package tanks.eventlistener;

import tanks.Game;
import tanks.network.event.EventShootBullet;
import tanks.network.event.INetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventListener
{
    public EventListenerFunc func;
    public Set<Class<? extends INetworkEvent>> classes;
    public ArrayList<INetworkEvent> eventsThisFrame = new ArrayList<>();

    public EventListener(EventListenerFunc e, Set<Class<? extends INetworkEvent>> classes)
    {
        this.func = e;
        this.classes = classes;
    }

    public void add()
    {
        addListener(this);
    }

    public static void addListener(EventListenerFunc e, Class<? extends INetworkEvent> cls)
    {
        HashSet<Class<? extends INetworkEvent>> set = new HashSet<>();
        set.add(cls);
        addListener(e, set);
    }

    public static void addListener(EventListenerFunc e, Set<Class<? extends INetworkEvent>> classes)
    {
        addListener(new EventListener(e, classes));
    }

    public static void addListener(EventListener l)
    {
        for (Class<? extends INetworkEvent> cls : l.classes)
        {
            ArrayList<EventListener> arr = Game.eventListeners.getOrDefault(cls, new ArrayList<>());
            arr.add(l);
            Game.eventListeners.put(cls, arr);
        }
        Game.eventListenerSet.add(l);
    }

    public static void addBulletListener(EventListenerFunc e)
    {
        addListener(e, EventShootBullet.class);
    }

    @FunctionalInterface
    public interface EventListenerFunc
    {
        void apply(List<INetworkEvent> e);
    }
}
