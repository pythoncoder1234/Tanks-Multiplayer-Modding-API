package tanks;

import tanks.network.event.EventExplosion;
import tanks.network.event.EventShootBullet;
import tanks.network.event.INetworkEvent;

public class EventListener
{
    public Function<INetworkEvent, Boolean> function;
    public Class<? extends INetworkEvent> event;

    public EventListener(Class<? extends INetworkEvent> event, Function<INetworkEvent, Boolean> func)
    {
        this.event = event;
        this.function = func;
    }

    public static class BulletListener extends EventListener
    {
        public BulletListener(Function<INetworkEvent, Boolean> func)
        {
            super(EventShootBullet.class, func);
        }
    }

    public static class ExplosionListener extends EventListener
    {
        public ExplosionListener(Function<INetworkEvent, Boolean> func)
        {
            super(EventExplosion.class, func);
        }
    }
}
