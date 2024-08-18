package tanks.tank;

import basewindow.InputCodes;
import basewindow.InputPoint;
import basewindow.Model;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletElectric;
import tanks.gui.Button;
import tanks.gui.Joystick;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenTitle;
import tanks.hotbar.Hotbar;
import tanks.hotbar.item.*;
import tanks.network.event.EventLayMine;
import tanks.network.event.EventShootBullet;

/**
 * A tank that is controlled by the player. TankPlayerController is used instead if we are connected to a party as a client.
 */
public class TankPlayer extends Tank implements ILocalPlayerTank, IServerPlayerTank
{
	public static boolean nightVision = true;

    public static ItemBullet default_bullet;
    public static ItemMine default_mine;

    public static Joystick controlStick;
    public static Joystick shootStick;
    public static Button mineButton;

    public static boolean controlStickSnap = false;
    public static boolean controlStickMobile = true;

	public Player player = Game.player;
	public static boolean enableDestroyCheat = false;

	public boolean drawTouchCircle = false;
	public double touchCircleSize = 400;
	public long prevTap = 0;

	public static boolean shootStickEnabled = false;
	public static boolean shootStickHidden = false;

	protected double prevDistSq;

	protected long lastTrace = 0;
	protected static boolean lockTrace = false;

	protected Ray ray;
	protected double drawRange = -1;

	public double mouseX;
	public double mouseY;

	public static Model sunglassesModel;
	public static boolean hi = false;

	public TankPlayer(double x, double y, double angle)
    {
        super("player", x, y, Game.tile_size, 0, 150, 255);

        if (sunglassesModel == null)
            sunglassesModel = Drawing.drawing.createModel("/models/sunglasses/");

        this.angle = angle;
        this.orientation = angle;
        this.player.tank = this;

        this.colorR = Game.player.colorR;
        this.colorG = Game.player.colorG;
        this.colorB = Game.player.colorB;
        this.secondaryColorR = Game.player.turretColorR;
        this.secondaryColorG = Game.player.turretColorG;
		this.secondaryColorB = Game.player.turretColorB;

		if (hi)
			this.baseModel = TankModels.arrow.base;

		if (enableDestroyCheat)
		{
			this.showName = true;
			this.nameTag.name.text = "Destroy cheat enabled!!!";
		}

		if (Game.nameInMultiplayer && ScreenPartyHost.isServer)
		{
			this.nameTag.name.text = Game.player.username;
			this.showName = true;
		}

		if (Game.invulnerable)
		{
			this.resistExplosions = true;
			this.resistBullets = true;
		}
	}

    public void setDefaultColor()
    {
        this.colorR = 0;
        this.colorG = 150;
        this.colorB = 255;
        this.secondaryColorR = Turret.calculateSecondaryColor(this.colorR);
        this.secondaryColorG = Turret.calculateSecondaryColor(this.colorG);
        this.secondaryColorB = Turret.calculateSecondaryColor(this.colorB);
    }

    @Override
    public void postInitSelectors()
    {
        super.postInitSelectors();

        this.teamSelector.id = "player_team";
        this.teamSelector.defaultTeamIndex = 0;

        if (!this.teamSelector.modified)
            this.teamSelector.setChoice(0);
    }

    @Override
    public void draw()
    {
        super.draw();

		if (this.destroy || !hi || !Game.enable3d)
			return;

		double s = (this.size * (Game.tile_size - destroyTimer) / Game.tile_size) * Math.min(this.drawAge / Game.tile_size, 1);

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawModel(sunglassesModel, this.posX, this.posY, this.posZ, s, s, s, this.angle);
    }

    @Override
    public void update()
    {
		boolean up = Game.game.input.moveUp.isPressed();
        boolean down = Game.game.input.moveDown.isPressed();
        boolean left = Game.game.input.moveLeft.isPressed();
        boolean right = Game.game.input.moveRight.isPressed();
        boolean trace = Game.game.input.aim.isPressed();
		ray = null;

		boolean destroy = Game.game.window.pressedKeys.contains(InputCodes.KEY_BACKSPACE);

		if ((Game.game.window.validPressedKeys.contains(InputCodes.KEY_G) || age == 0) && Level.currentLightIntensity < 0.5)
		{
			if (age != 0)
			{
				Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_G);
				nightVision = !nightVision;
			}

			Level.currentLightIntensity = Level.currentLightIntensity == 0.019 ? 0 : Math.max(Level.currentLightIntensity, 0.019);
			double multiplier = nightVision ? -8 * Level.currentLightIntensity + 4 : 0;
			this.glowSize = 0;
			this.lightSize = 20 * multiplier;
			this.lightIntensity = multiplier;
			this.luminance = nightVision ? multiplier : 0.5;
		}

		if (Game.game.input.aim.isValid())
		{
			Game.game.input.aim.invalidate();

			long time = System.currentTimeMillis();

			lockTrace = false;
			if (time - lastTrace <= 500)
			{
				lastTrace = 0;
				lockTrace = true;
			}
			else
				lastTrace = time;
		}

		if (destroy && enableDestroyCheat)
		{
			for (Movable m : Game.movables)
			{
				if (!Team.isAllied(this, m))
					m.destroy = true;
			}
		}

		if (this.tookRecoil)
		{
			if (this.recoilSpeed <= this.maxSpeed * this.maxSpeedModifier * 1.0001)
			{
				this.tookRecoil = false;
				this.inControlOfMotion = true;
			}
			else
			{
				this.setMotionInDirection(this.vX + this.posX, this.vY + this.posY, this.recoilSpeed);
				this.recoilSpeed *= Math.pow(1 - this.friction * this.frictionModifier, Panel.frameFrequency);
			}
		}
		else if (this.inControlOfMotion)
        {
            double acceleration = this.acceleration * this.accelerationModifier;
            double maxVelocity = this.maxSpeed * this.maxSpeedModifier;

            double x = 0;
            double y = 0;

            double a = -1;

			ScreenGame g = ScreenGame.getInstance();
            if (g != null && !g.freecam || ScreenGame.controlPlayer)
            {
                if (left)
                    x -= 1;

                if (right)
                    x += 1;

                if (up)
                    y -= 1;

                if (down)
                    y += 1;
            }

			if (x == 1 && y == 0)
				a = 0;
			else if (x == 1 && y == 1)
				a = Math.PI / 4;
			else if (x == 0 && y == 1)
				a = Math.PI / 2;
			else if (x == -1 && y == 1)
				a = 3 * Math.PI / 4;
			else if (x == -1 && y == 0)
				a = Math.PI;
			else if (x == -1)
				a = 5 * Math.PI / 4;
			else if (x == 0 && y == -1)
				a = 3 * Math.PI / 2;
			else if (x == 1)
				a = 7 * Math.PI / 4;

			double intensity;

			if (a < 0 && Game.game.window.touchscreen)
			{
				intensity = controlStick.inputIntensity;

				if (intensity >= 0.2)
					a = controlStick.inputAngle;
			}

			if (a >= 0)
			{
				if (Game.followingCam)
					a += this.angle + Math.PI / 2;

				this.addPolarMotion(a, acceleration * this.maxSpeed * Panel.frameFrequency);
			}

			if (a == -1)
			{
				this.vX *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);
				this.vY *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);

				if (Math.abs(this.vX) < 0.001)
					this.vX = 0;

				if (Math.abs(this.vY) < 0.001)
					this.vY = 0;
			}

			double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

			if (speed > maxVelocity)
				this.setPolarMotion(this.getPolarDirection(), maxVelocity);
		}

		double reload = this.getAttributeValue(AttributeModifier.reload, 1);

		this.bullet.updateCooldown(reload);
		this.mine.updateCooldown(reload);

		if (Game.player.chromaaa)
		{
			this.colorR = rainbowColor(Game.player.colorR, 1);
			this.colorG = rainbowColor(Game.player.colorG, 3);
			this.colorB = rainbowColor(Game.player.colorB, 2);

			this.secondaryColorR = rainbowColor(Game.player.turretColorR, 1);
			this.secondaryColorG = rainbowColor(Game.player.turretColorG, 3);
			this.secondaryColorB = rainbowColor(Game.player.turretColorB, 2);
		}


		Hotbar h = Game.player.hotbar;
		if (h.enabledItemBar)
		{
			for (Item i: h.itemBar.slots)
			{
				if (i != null && !(i instanceof ItemEmpty))
                    i.updateCooldown(reload);
			}
		}

		boolean shoot = !(Game.currentGame != null && !Game.currentGame.enableShooting) && !Game.game.window.touchscreen && Game.game.input.shoot.isValid();
		boolean mine = !(Game.currentGame != null && !Game.currentGame.enableLayingMines) && !Game.game.window.touchscreen && Game.game.input.mine.isPressed();
//		Game.game.input.shoot.invalidate();

		boolean showRange = false;
		if (h.enabledItemBar && h.itemBar.selected >= 0)
		{
			Item i = h.itemBar.slots[h.itemBar.selected];

			if (i instanceof ItemBullet)
				showRange = ((ItemBullet) i).getRange() >= 0;
			else if (i instanceof ItemRemote)
				showRange = ((ItemRemote) i).range >= 0;
		}

		TankPlayer.shootStickHidden = showRange;

		boolean prevTouchCircle = this.drawTouchCircle;
		this.drawTouchCircle = false;
		if (Game.game.window.touchscreen)
		{
			if (shootStickEnabled)
			{
				if (!Game.bulletLocked && !this.disabled && !this.destroy)
					mineButton.update();

				if (!showRange)
					shootStick.update();
			}

			if (!Game.bulletLocked && !this.disabled && !this.destroy)
			{
				double distSq = 0;

				if (shootStickEnabled)
				{
					if (mineButton.justPressed)
						mine = true;

					if (shootStick.inputIntensity >= 0.2 && !showRange)
					{
						this.angle = shootStick.inputAngle;
						trace = true;

						if (shootStick.inputIntensity >= 1.0)
							shoot = true;
					}
				}

				if (!shootStickEnabled || shootStickHidden)
				{
					for (int i : Game.game.window.touchPoints.keySet())
					{
						InputPoint p = Game.game.window.touchPoints.get(i);

						if (!p.tag.isEmpty() && !p.tag.equals("aim") && !p.tag.equals("shoot"))
							continue;

						double px = Drawing.drawing.getInterfacePointerX(p.x);
						double py = Drawing.drawing.getInterfacePointerY(p.y);

						if (!Game.followingCam)
						{
							this.mouseX = Drawing.drawing.toGameCoordsX(px);
							this.mouseY = Drawing.drawing.toGameCoordsY(py);
							this.angle = this.getAngleInDirection(this.mouseX, this.mouseY);
						}

						distSq = Math.pow(px - Drawing.drawing.toInterfaceCoordsX(this.posX), 2)
								+ Math.pow(py - Drawing.drawing.toInterfaceCoordsY(this.posY), 2);

						if (distSq <= Math.pow(this.touchCircleSize / 4, 2) || p.tag.equals("aim"))
						{
							p.tag = "aim";
							this.drawTouchCircle = true;

							if (!prevTouchCircle)
							{
								if (System.currentTimeMillis() - prevTap <= 500)
								{
									Drawing.drawing.playVibration("heavyClick");
									mine = true;
									this.prevTap = 0;
								}
								else
									prevTap = System.currentTimeMillis();
							}

							trace = true;
						}
						else
						{
							shoot = true;
							p.tag = "shoot";
						}

						double proximity = Math.pow(this.touchCircleSize / 2, 2);

						if (p.tag.equals("aim") && ((distSq <= proximity && prevDistSq > proximity) || (distSq > proximity && prevDistSq <= proximity)))
							Drawing.drawing.playVibration("selectionChanged");

						if (distSq > proximity)
							shoot = true;
					}
				}

                this.prevDistSq = distSq;
            }
        }
        else if (!Game.followingCam)
        {
            this.mouseX = Drawing.drawing.getMouseX();
            this.mouseY = Drawing.drawing.getMouseY();
            this.angle = this.getAngleInDirection(this.mouseX, this.mouseY);
        }

		ScreenGame g = ScreenGame.getInstance();
        if (!(g != null && g.freecam && !ScreenGame.controlPlayer))
        {
            if (shoot && this.getItem(false).cooldown <= 0 && !this.disabled)
                this.shoot();

            if (mine && this.getItem(true).cooldown <= 0 && !this.disabled)
                this.layMine();
        }

        if ((trace || lockTrace) && !Game.bulletLocked && !this.disabled && (g != null || Game.screen instanceof ScreenTitle))
        {
            double range = -1;

            Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);

			if (h.enabledItemBar && h.itemBar.selected >= 0)
			{
				Item i = h.itemBar.slots[h.itemBar.selected];
				if (i instanceof ItemBullet b)
				{
					r.bounces = b.bounces;
					r.size = b.size;
					range = b.getRange();

					if (b.bulletClass.equals(BulletElectric.class))
						r.bounces = 0;
				}
				else if (i instanceof ItemRemote ir)
				{
					r.size = ir.size;
					if (ir.bounces >= 0)
						r.bounces = ir.bounces;

					range = ((ItemRemote) i).range;
				}
			}

			r.vX /= 2;
			r.vY /= 2;
			r.ignoreTanks = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_ALT);
			r.trace = true;
			r.dotted = true;
			r.moveOut(10 * this.size / Game.tile_size);

			if (range >= 0)
				this.drawRange = range;
			else
				this.ray = r;

			if (this.ray != null)
                this.ray.getTarget();
		}

		super.update();
	}

	public static double rainbowColor(double start, double speed)
	{
		double chromaStart = Math.max(0, start - 25);
		if (chromaStart > 230)
			chromaStart -= 25;

		return Math.sin(System.currentTimeMillis() / 2e3 * speed) * 50 + chromaStart;
	}

	public Item getItem(boolean rightClick)
	{
		Item i;

		if (rightClick)
			i = this.mine;
		else
			i = this.bullet;

		if (Game.player.hotbar.enabledItemBar)
		{
			Item i2 = Game.player.hotbar.itemBar.getSelectedItem(rightClick);
			if (i2 != null)
				i = i2;
		}

		return i;
	}

	public void shoot()
	{
		if (Game.bulletLocked || this.destroy)
			return;

		if (Game.player.hotbar.enabledItemBar)
		{
			if (Game.player.hotbar.itemBar.useItem(false))
				return;
		}

		this.bullet.attemptUse(this);
	}

	public void layMine()
	{
		if (Game.bulletLocked || this.destroy)
			return;

		if (Game.player.hotbar.enabledItemBar)
		{
			if (Game.player.hotbar.itemBar.useItem(true))
				return;
		}

		this.mine.attemptUse(this);
	}

	public void fireBullet(Bullet b, double speed, double offset)
	{
		if (speed <= 0)
			speed = Double.MIN_NORMAL;

		if (b.itemSound != null)
			Drawing.drawing.playGameSound(b.itemSound, this, Game.tile_size * 20, (float) ((Bullet.bullet_size / b.size) * (1 - (Math.random() * 0.5) * b.pitchVariation)));

		b.setPolarMotion(this.angle + offset, speed);
		b.speed = speed;
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil * this.getAttributeValue(AttributeModifier.recoil, 1) * b.frameDamageMultipler);

		this.recoilSpeed = this.getSpeed();
		if (this.recoilSpeed > this.maxSpeed * 1.01)
		{
			this.tookRecoil = true;
			this.inControlOfMotion = false;
		}

		if (b.moveOut)
			b.moveOut(50 * this.size / Game.tile_size);

		ScreenGame g;
		if (!Game.followingCam)
			b.setTargetLocation(this.mouseX, this.mouseY);
		else if ((g = ScreenGame.getInstance()) != null)
			b.setTargetLocation(posX + Math.cos(angle) * g.fcArcAim, posY + Math.sin(angle) * g.fcArcAim);

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);

		if (Crusade.crusadeMode && Crusade.currentCrusade != null)
		{
			CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(this.getPlayer());
			cp.addItemUse(b.item);
		}
	}

	public void layMine(Mine m)
	{
		if (Game.bulletLocked || this.destroy)
			return;

		Drawing.drawing.playGameSound("lay_mine.ogg", this, Game.tile_size * 20, (float) (Mine.mine_size / m.size));

		Game.eventsOut.add(new EventLayMine(m));
		Game.movables.add(m);

		if (Crusade.crusadeMode && Crusade.currentCrusade != null)
		{
			CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(this.getPlayer());
			cp.addItemUse(m.item);
		}
	}


	@Override
	public void onDestroy()
	{
		if (Crusade.crusadeMode)
			this.player.remainingLives--;
	}

	@Override
	public double getTouchCircleSize()
	{
		return this.touchCircleSize;
	}

	@Override
	public boolean showTouchCircle()
	{
		return this.drawTouchCircle;
	}

	@Override
	public double getDrawRange()
	{
		return this.drawRange;
	}

	@Override
	public void setDrawRange(double range)
	{
		this.drawRange = range;
	}

	public static void setShootStick(boolean enabled)
	{
		shootStickEnabled = enabled;

		if (controlStick != null && shootStick != null)
		{
			if (enabled)
			{
				controlStick.domain = 1;
				shootStick.domain = 2;
			}
			else
			{
				controlStick.domain = 0;
				shootStick.domain = 0;
			}
		}
	}

	@Override
	public Player getPlayer()
	{
		return this.player;
	}

	@Override
	public void setBufferCooldown(double value)
	{
		super.setBufferCooldown(value);

		Hotbar h = Game.player.hotbar;
		if (h.enabledItemBar)
		{
			for (Item i: h.itemBar.slots)
			{
				if (i != null && !(i instanceof ItemEmpty))
				{
					i.cooldown = Math.max(i.cooldown, value);
				}
			}
		}
	}
}