package tanks.eventlistener;

import tanks.Movable;
import tanks.bullet.Bullet;
import tanks.network.event.EventTankUpdateHealth;
import tanks.network.event.INetworkEvent;
import tanks.tank.Explosion;
import tanks.tank.Tank;

import java.util.Collections;
import java.util.HashSet;

public class DamageListener extends EventListener
{
    public DamageListener(DamageListenerFunc f, boolean killOnly)
    {
        super(getFunc(f, killOnly), new HashSet<>(Collections.singletonList(EventTankUpdateHealth.class)));
    }

    public static EventListenerFunc getFunc(DamageListenerFunc f, boolean killOnly)
    {
        return events ->
        {
            HashSet<DamagedTank> tanks = new HashSet<>();

            for (INetworkEvent event : events)
            {
                Tank attacker = null;
                Movable source = null;
                EventTankUpdateHealth e = (EventTankUpdateHealth) event;

                if (e.source == null)
                    continue;

                if (killOnly && e.health > 0)
                    continue;

                if (e.source instanceof Movable)
                {
                    Movable m = (Movable) e.source;
                    source = m;
                }

                if (e.source instanceof Bullet)
                {
                    Bullet b = (Bullet) e.source;
                    attacker = b.tank;
                }
                else if (e.source instanceof Explosion)
                {
                    Explosion m = (Explosion) e.source;
                    attacker = m.tank;
                }

                tanks.add(new DamagedTank(e.tank, attacker, source));
            }

            if (!tanks.isEmpty())
                f.apply(tanks);
        };
    }

    public static void add(DamageListenerFunc func, boolean killOnly)
    {
        addListener(new DamageListener(func, killOnly));
    }

    @FunctionalInterface
    public interface DamageListenerFunc
    {
        void apply(HashSet<DamagedTank> tanks);
    }

    public static void add(DamageListenerFunc e)
    {
        add(e, false);
    }

    public static class DamagedTank
    {
        public Tank target, attacker;
        public Movable source;

        DamagedTank(Tank target, Tank attacker, Movable source)
        {
            this.target = target;
            this.attacker = attacker;
            this.source = source;
        }

        @Override
        public int hashCode()
        {
            return target.hashCode();
        }
    }
}
