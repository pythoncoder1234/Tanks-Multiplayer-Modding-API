package tanks.bullet;

import tanks.*;
import tanks.AttributeModifier.Operation;
import tanks.event.EventTankUpdateHealth;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletHealing extends BulletInstant
{
	public static String bullet_name = "healing";

	public boolean hitTank = false;

	public BulletHealing(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, ib);
		this.playPopSound = false;
		this.baseColorR = 0;
		this.baseColorG = 255;
		this.baseColorB = 0;
		this.name = bullet_name;
		this.effect = BulletEffect.none;
		this.baseDamage = 0.01;
		this.shouldDodge = false;
		this.dealsDamage = false;

		this.itemSound = null;
		// this.itemSound = "heal.ogg";
	}

	public BulletHealing(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, false, ib);
	}

	@Override
	public void update()
	{
		if (!this.expired)
		{
			this.shoot();

			float freq = (float) (Panel.frameFrequency / 10);

			if (Game.game.window.touchscreen)
				freq = 1;

			if (!hitTank)
				Drawing.drawing.playGlobalSound("heal1.ogg", 1f, freq / 2);
		}

		super.update();
	}

	@Override
	public void collidedWithTank(Tank t)
	{
		if (!heavy)
			this.destroy = true;

		hitTank = true;

		float freq = (float) (Panel.frameFrequency / 10);

		if (Game.game.window.touchscreen)
			freq = 1;

		double before = t.healRayHealth;

		if (t.healRayHealth < 1)
		{
			t.healRayHealth = Math.min(1, t.healRayHealth + this.damage * this.baseDamage * this.frameDamageMultipler);
			t.health += t.healRayHealth - before;
		}

		t.checkHit(this.tank, this);

		Drawing.drawing.playGlobalSound("heal2.ogg", (float) (t.healRayHealth * 0.12 + 0.63), freq / 2);

		Game.eventsOut.add(new EventTankUpdateHealth(t));

		t.addAttribute(new AttributeModifier("healray", "healray", Operation.add, 1.0));

		if (t.health > 6 && (int) (before) != (int) (t.healRayHealth))
		{
			Effect e = Effect.createNewEffect(t.posX, t.posY, t.posZ + t.size * 0.75, Effect.EffectType.shield);
			e.size = t.size;
			e.radius = t.health - 1;
			Game.effects.add(e);
		}
	}

	@Override
	public void collidedWithObject(Movable o)
	{

	}

	@Override
	public void addDestroyEffect()
	{
		if (Game.effectsEnabled)
		{
			for (int i = 0; i < this.size / 4 * Game.effectMultiplier; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
				double var = 50;
				e.maxAge /= 2;
				e.colR = Math.min(255, Math.max(0, this.baseColorR + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, this.baseColorG + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, this.baseColorB + Math.random() * var - var / 2));

				if (Game.enable3d)
					e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0 * 4);
				else
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);

				Game.effects.add(e);
			}
		}
	}

}
