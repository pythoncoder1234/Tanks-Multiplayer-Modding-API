package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.GameObject;
import tanks.tank.Tank;

public class EventTankUpdateHealth extends PersonalEvent
{
	public Tank tank;
	public double health;
	public GameObject source;
	
	public EventTankUpdateHealth()
	{
		
	}
	
	public EventTankUpdateHealth(Tank t, GameObject source)
	{
		tank = t;
		health = t.health;
		this.source = source;
	}
	
	@Override
	public void execute() 
	{
		if (tank == null || this.clientID != null)
			return;

		if (tank.health > health && health > 0)
			tank.flashAnimation = 1;

		double before = tank.health;
		tank.health = health;

		if (tank.health > 6 && (int) before != (int) tank.health)
		{
			Effect e = Effect.createNewEffect(tank.posX, tank.posY, tank.posZ + tank.size * 0.75, Effect.EffectType.shield);
			e.size = tank.size;
			e.radius = tank.health - 1;
			Game.effects.add(e);
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank.networkID);
		b.writeDouble(this.health);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.tank = Tank.idMap.get(b.readInt());
		this.health = b.readDouble();
	}
}
