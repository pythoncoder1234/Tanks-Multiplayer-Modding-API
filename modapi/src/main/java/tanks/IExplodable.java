package tanks;

import tanks.tank.Explosion;

public interface IExplodable
{
    void onExploded(Explosion e);
    default void applyExplosionKnockback(double angle, double power, Explosion e) {}
    default double getSize() { return 0; }
}
