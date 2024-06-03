package tanks.eventlistener;

import tanks.Movable;
import tanks.bullet.Bullet;
import tanks.network.event.EventTankUpdateHealth;
import tanks.network.event.INetworkEvent;
import tanks.tank.Explosion;
import tanks.tank.Tank;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DamageListener extends EventListener
{
    public DamageListener(DamageListenerFunc f, boolean killOnly)
    {
        super(getFunc(f, killOnly), of(EventTankUpdateHealth.class));
    }

    public static <E> Set<E> of(E e1)
    {
        Set<E> classes1 = new HashSet<>();
        classes1.add(e1);
        return classes1;
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

    public static final class DamagedTank
    {
        private final Tank target;
        private final Tank attacker;
        private final Movable source;

        public DamagedTank(Tank target, Tank attacker, Movable source)
        {
            this.target = target;
            this.attacker = attacker;
            this.source = source;
        }

        public Tank target()
        {
            return target;
        }

        public Tank attacker()
        {
            return attacker;
        }

        public Movable source()
        {
            return source;
        }

        @Override
        public int hashCode()
        {
            return target.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            DamagedTank that = (DamagedTank) obj;
            return Objects.equals(this.target, that.target) &&
                   Objects.equals(this.attacker, that.attacker) &&
                   Objects.equals(this.source, that.source);
        }

        @Override
        public String toString()
        {
            return "DamagedTank[" +
                   "target=" + target + ", " +
                   "attacker=" + attacker + ", " +
                   "source=" + source + ']';
        }

    }
}
