package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.GameObject;
import tanks.hotbar.item.ItemShield;
import tanks.tank.Tank;

public class EventTankUpdateHealth extends PersonalEvent
{
	public Tank tank;
	public double health;
	public GameObject source;
	public boolean shieldSound;
	
	public EventTankUpdateHealth()
	{
		
	}
	
	public EventTankUpdateHealth(Tank t, GameObject source)
	{
		tank = t;
		health = t.health;
		this.source = source;
		shieldSound = source instanceof ItemShield;
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

		if (shieldSound)
			Drawing.drawing.playGameSound("shield.ogg", tank, Game.tile_size * 20, 1f);

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

		if (!Game.vanillaMode)
			b.writeBoolean(shieldSound);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.tank = Tank.idMap.get(b.readInt());
		this.health = b.readDouble();

		if (!Game.vanillaMode)
			shieldSound = b.readBoolean();
	}
}
