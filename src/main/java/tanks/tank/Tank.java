package tanks.tank;

import basewindow.Model;
import basewindow.ModelPart;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.editor.selector.LevelEditorSelector;
import tanks.editor.selector.RotationSelector;
import tanks.editor.selector.TeamSelector;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.ItemBullet;
import tanks.hotbar.item.ItemMine;
import tanks.network.event.*;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleLiquid;

import java.util.*;

import static tanks.tank.TankProperty.Category.*;

public abstract class Tank extends Movable implements ISolidObject, IExplodable
{
    public static int disabledZ = (int) (1.25 * Game.tile_size);
	public static int updatesPerFrame = 0;
	public static ArrayDeque<Integer> prevUPF = new ArrayDeque<>();
	public static int tankCount, lastTankCount;

    public static int updatesPerSecond = 30;
    public static long lastUpdateTime = 0;
    public static boolean shouldUpdate = false;

    public static int currentID = 0;
    public static ArrayList<Integer> freeIDs = new ArrayList<>();
    public static HashMap<Integer, Tank> idMap = new HashMap<>();

    public static ModelPart health_model;

    public RotationSelector<Tank> rotationSelector;
    public TeamSelector<Tank> teamSelector;

    public boolean fromRegistry = false;

    @TankProperty(category = appearanceBody, id = "color_model", name = "Tank body model", miscType = TankProperty.MiscType.colorModel)
    public Model colorModel = TankModels.tank.color;
    @TankProperty(category = appearanceTreads, id = "base_model", name = "Tank treads model", miscType = TankProperty.MiscType.baseModel)
    public Model baseModel = TankModels.tank.base;
    @TankProperty(category = appearanceTurretBase, id = "turret_base_model", name = "Turret base model", miscType = TankProperty.MiscType.turretBaseModel)
    public Model turretBaseModel = TankModels.tank.turretBase;
    @TankProperty(category = appearanceTurretBarrel, id = "turret_model", name = "Turret barrel model", miscType = TankProperty.MiscType.turretModel)
	public Model turretModel = TankModels.tank.turret;

	public double angle = 0;
	public double pitch = 0;

	public boolean depthTest = true;

	public boolean disabled = false;
	public boolean inControlOfMotion = true;
	public boolean positionLock = false;

	public boolean fullBrightness = false;

	public boolean tookRecoil = false;
	public double recoilSpeed = 0;

	/** If spawned by another tank, set to the tank that spawned this tank*/
	protected Tank parent = null;

	@TankProperty(category = general, id = "name", name = "Tank name")
	public String name;

	@TankProperty(category = general, id = "coin_value", name = "Coin value")
	public int coinValue = 0;

	@TankProperty(category = general, id = "base_health", name = "Hitpoints", desc = "The default bullet does one hitpoint of damage")
	public double baseHealth = 1;
	public double health = 1;

	public boolean invulnerable = false;

	@TankProperty(category = general, id = "targetable", name = "Should be targeted")
	public boolean targetable = true;

	@TankProperty(category = general, id = "resist_bullets", name = "Bullet immunity")
	public boolean resistBullets = false;
	@TankProperty(category = general, id = "resist_explosions", name = "Explosion immunity")
	public boolean resistExplosions = false;
	@TankProperty(category = general, id = "resist_freezing", name = "Freezing immunity")
	public boolean resistFreeze = false;

	@TankProperty(category = general, id = "collision_enabled", name = "Enable Collision")
	public boolean enableCollision = true;

	public int networkID = -1;
	public int crusadeID = -1;

	@TankProperty(category = general, id = "description", name = "Tank description", miscType = TankProperty.MiscType.description)
	public String description = "";

    @TankProperty(category = movementGeneral, id = "max_speed", name = "Top speed")
    public double maxSpeed = 1.5;

    @TankProperty(category = movementGeneral, id = "acceleration", name = "Acceleration")
    public double acceleration = 0.05;

    @TankProperty(category = movementGeneral, id = "friction", name = "Friction")
    public double friction = 0.05;

    public double buoyancy = 0;

    public double accelerationModifier = 1;
    public double frictionModifier = 1;
    public double maxSpeedModifier = 1;
    public double damageRate = 0;

    @TankProperty(category = appearanceBody, id = "color_r", name = "Red", miscType = TankProperty.MiscType.color)
    public double colorR;
    @TankProperty(category = appearanceBody, id = "color_g", name = "Green", miscType = TankProperty.MiscType.color)
    public double colorG;
	@TankProperty(category = appearanceBody, id = "color_b", name = "Blue", miscType = TankProperty.MiscType.color)
	public double colorB;

	@TankProperty(category = appearanceGlow, id = "glow_intensity", name = "Aura intensity")
	public double glowIntensity = 0.8;
	@TankProperty(category = appearanceGlow, id = "glow_size", name = "Aura size")
	public double glowSize = 4;
	@TankProperty(category = appearanceGlow, id = "light_intensity", name = "Light intensity")
	public double lightIntensity = 1;
	@TankProperty(category = appearanceGlow, id = "light_size", name = "Light size")
	public double lightSize = 0;
	@TankProperty(category = appearanceGlow, id = "luminance", name = "Tank luminance", desc = "How bright the tank will be in dark lighting. At 0, the tank will be shaded like terrain by lighting. At 1, the tank will always be fully bright.")
	public double luminance = 0.5;

	/** Important: this option only is useful for the tank editor. Secondary color will be treated independently even if disabled. */
	@TankProperty(category = appearanceTurretBarrel, id = "enable_color2", name = "Custom color", miscType = TankProperty.MiscType.color)
	public boolean enableSecondaryColor = false;
	@TankProperty(category = appearanceTurretBarrel, id = "color_r2", name = "Red", miscType = TankProperty.MiscType.color)
	public double secondaryColorR;
	@TankProperty(category = appearanceTurretBarrel, id = "color_g2", name = "Green", miscType = TankProperty.MiscType.color)
	public double secondaryColorG;
	@TankProperty(category = appearanceTurretBarrel, id = "color_b2", name = "Blue", miscType = TankProperty.MiscType.color)
	public double secondaryColorB;
	@TankProperty(category = appearanceTurretBarrel, id = "turret_size", name = "Turret thickness")
	public double turretSize = 8;
	@TankProperty(category = appearanceTurretBarrel, id = "turret_length", name = "Turret length")
	public double turretLength = Game.tile_size;
	@TankProperty(category = appearanceTurretBarrel, id = "multiple_turrets", name = "Multiple turrets", desc = "If enabled, the turret will reflect the bullet count")
	public boolean multipleTurrets = true;

	/** Important: tertiary color values will not be used unless this option is set to true! */
	@TankProperty(category = appearanceTurretBase, id = "enable_color3", name = "Custom color", miscType = TankProperty.MiscType.color)
	public boolean enableTertiaryColor = false;
	@TankProperty(category = appearanceTurretBase, id = "color_r3", name = "Red", miscType = TankProperty.MiscType.color)
	public double tertiaryColorR;
	@TankProperty(category = appearanceTurretBase, id = "color_g3", name = "Green", miscType = TankProperty.MiscType.color)
	public double tertiaryColorG;
	@TankProperty(category = appearanceTurretBase, id = "color_b3", name = "Blue", miscType = TankProperty.MiscType.color)
	public double tertiaryColorB;

	@TankProperty(category = appearanceTracks, id = "enable_tracks", name = "Lays tracks")
	public boolean enableTracks = true;
	@TankProperty(category = appearanceTracks, id = "track_spacing", name = "Track spacing")
	public double trackSpacing = 0.4;

	//public int liveBulletMax;
	//public int liveMinesMax;

	@TankProperty(category = firingGeneral, id = "bullet", name = "Bullet")
	public ItemBullet bullet = (ItemBullet) TankPlayer.default_bullet.clone();

	@TankProperty(category = mines, id = "mine", name = "Mine")
	public ItemMine mine = (ItemMine) TankPlayer.default_mine.clone();

	/** Age in frames*/
	protected double age = 0;

	public double drawAge = 0;
	public double destroyTimer = 0;
	public boolean hasCollided = false;
	public double flashAnimation = 0;
	public double treadAnimation = 0;
	public boolean drawTread = false;

	@TankProperty(category = appearanceEmblem, id = "emblem", name = "Tank emblem", miscType = TankProperty.MiscType.emblem)
	public String emblem = null;
	@TankProperty(category = appearanceEmblem, id = "emblem_r", name = "Red", miscType = TankProperty.MiscType.color)
	public double emblemR;
	@TankProperty(category = appearanceEmblem, id = "emblem_g", name = "Green", miscType = TankProperty.MiscType.color)
	public double emblemG;
	@TankProperty(category = appearanceEmblem, id = "emblem_b", name = "Blue", miscType = TankProperty.MiscType.color)
	public double emblemB;

	public double emblemAngle = 0;
	public double orientation = 0;
	public double basePitch, baseRoll;

	public double hitboxSize = 0.95;

	/** Used for custom tanks, see /music/tank for built-in tanks */
	@TankProperty(category = general, id = "music", name = "Music tracks", miscType = TankProperty.MiscType.music)
	public HashSet<String> musicTracks = new HashSet<>();

	@TankProperty(category = general, id = "explode_on_destroy", name = "Explosive", desc="If set, the tank will explode when destroyed")
	public boolean explodeOnDestroy = false;

	public boolean droppedFromCrate = false;

	/** Whether this tank needs to be destroyed before the level ends. */
	@TankProperty(category = general, id = "mandatory_kill", name = "Must be destroyed", desc="Whether the tank needs to be destroyed to clear the level")
	public boolean mandatoryKill = true;

	@TankProperty(category = general, id = "collision", name = "Pushed during Collision")
	public boolean collisionPush = true;

	public boolean hiddenStatusChanged = false;
	public boolean[][] hiddenPoints = new boolean[3][3];
	public boolean hidden = false;
	public boolean[][] canHidePoints = new boolean[3][3];
	public boolean canHide = false;

	public Turret turret;

	public boolean standardUpdateEvent = true;

	public boolean isBoss = false;
	public Tank possessor;
	public Tank possessingTank = null;
	public boolean overridePossessedKills = true;

	public long lastFarthestInSightUpdate = 0;
	public Tank lastFarthestInSight = null;

	public boolean tiltFirstFrame = true;
	public int tiltDirection;
	public boolean customPosZBehavior;

	public Tank(String name, double x, double y, double size, double r, double g, double b)
	{
		super(x, y);
		this.size = size;
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		turret = new Turret(this);
		this.name = name;
		this.nameTag = new NameTag(this, 0, this.size / 7 * 5, this.size / 2, this.name);

		this.drawLevel = 4;

		this.bullet.unlimitedStack = true;
		this.mine.unlimitedStack = true;
	}

	public boolean canBeHealed()
	{
		return health - baseHealth < 1;
	}

	public void unregisterNetworkID()
	{
		if (idMap.get(this.networkID) == this)
			idMap.remove(this.networkID);

		if (!freeIDs.contains(this.networkID))
			freeIDs.add(this.networkID);
	}

	public static int nextFreeNetworkID()
	{
		if (!freeIDs.isEmpty())
			return freeIDs.remove(0);
        currentID++;
        return currentID - 1;
    }

	public void registerNetworkID()
	{
		if (ScreenPartyLobby.isClient)
			Game.exitToCrash(new RuntimeException("Do not automatically assign network IDs on client!"));

		this.networkID = nextFreeNetworkID();
		idMap.put(this.networkID, this);
	}

	public void setNetworkID(int id)
	{
		this.networkID = id;
		idMap.put(id, this);
	}

	public void fireBullet(Bullet b, double speed, double offset)
	{

	}

	public void layMine(Mine m)
	{

	}


	public void checkCollision()
	{
		if (this.size <= 0 || this.destroy)
			return;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m.skipNextUpdate || m.destroy)
				continue;

			if (this != m && m instanceof Tank t && t.enableCollision && m.size > 0)
			{
				double distSq = Math.pow(this.posX - m.posX, 2) + Math.pow(this.posY - m.posY, 2);

                if (distSq <= Math.pow((this.size + t.size) / 2, 2) && Math.abs(this.posZ - t.posZ) < this.size + t.size)
                {
                    this.hasCollided = true;
                    t.hasCollided = true;

                    this.onCollidedWith(t, distSq);
                }
			}
		}

		hasCollided = false;

		this.size *= this.hitboxSize;

		checkBorderCollision(this);
		checkObstacleCollision();

        this.size /= this.hitboxSize;
    }

	public void checkObstacleCollision()
	{
		double t = Game.tile_size;
		drawTransparent = false;

		int x1 = (int) ((this.posX - this.size / 2) / t - 1);
		int y1 = (int) ((this.posY - this.size / 2) / t - 1);
		int x2 = (int) ((this.posX + this.size / 2) / t + 1);
		int y2 = (int) ((this.posY + this.size / 2) / t + 1);

		for (int x = x1; x <= x2; x++)
		{
			for (int y = y1; y <= y2; y++)
			{
				checkCollisionWith(Game.getObstacle(x, y));
				checkCollisionWith(Game.getSurfaceObstacle(x, y));
			}
		}
	}

	public static boolean checkBorderCollision(Movable m)
	{
		if (Game.currentLevel.mapLoad)
			return false;

		boolean hasCollided = false;

		if (m.posX + m.size / 2 > Drawing.drawing.sizeX)
		{
			m.posX = Drawing.drawing.sizeX - m.size / 2;
			m.vX *= -m.bounciness;
			hasCollided = true;
		}
		if (m.posY + m.size / 2 > Drawing.drawing.sizeY)
		{
			m.posY = Drawing.drawing.sizeY - m.size / 2;
			m.vY *= -m.bounciness;
			hasCollided = true;
		}
		if (m.posX - m.size / 2 < 0)
		{
			m.posX = m.size / 2;
			m.vX *= -m.bounciness;
			hasCollided = true;
		}
		if (m.posY - m.size / 2 < 0)
		{
			m.posY = m.size / 2;
			m.vY *= -m.bounciness;
			hasCollided = true;
		}

		return hasCollided;
	}

	public void onCollidedWith(Tank t, double distSq)
    {
        double ourMass = this.size * this.size;
        double theirMass = t.size * t.size;

        double angle = this.getAngleInDirection(t.posX, t.posY);

        double ourV = Math.sqrt(this.vX * this.vX + this.vY * this.vY);
        double ourAngle = this.getPolarDirection();
        double ourParallelV = ourV * Math.cos(ourAngle - angle);
        double ourPerpV = ourV * Math.sin(ourAngle - angle);

        double theirV = Math.sqrt(t.vX * t.vX + t.vY * t.vY);
        double theirAngle = t.getPolarDirection();
        double theirParallelV = theirV * Math.cos(theirAngle - angle);
        double theirPerpV = theirV * Math.sin(theirAngle - angle);

        double newV = (ourParallelV * ourMass + theirParallelV * theirMass) / (ourMass + theirMass);

        double dist = Math.sqrt(distSq);
        this.moveInDirection(Math.cos(angle), Math.sin(angle), (dist - (this.size + t.size) / 2) * theirMass / (ourMass + theirMass));
        t.moveInDirection(Math.cos(Math.PI + angle), Math.sin(Math.PI + angle), (dist - (this.size + t.size) / 2) * ourMass / (ourMass + theirMass));

        if (distSq > Math.pow((this.posX + this.vX) - (t.posX + t.vX), 2) + Math.pow((this.posY + this.vY) - (t.posY + t.vY), 2))
        {
            this.setMotionInDirection(t.posX, t.posY, newV);
            this.addPolarMotion(angle + Math.PI / 2, ourPerpV);

            t.setMotionInDirection(this.posX, this.posY, -newV);
            t.addPolarMotion(angle + Math.PI / 2, theirPerpV);
        }
    }

    public void checkCollisionWith(Obstacle o)
	{
		hasCollided = checkCollideWith(this, o);
	}

    public static boolean checkCollideWith(Movable m, Obstacle o)
    {
        if (o == null)
            return false;

		if (o instanceof IAvoidObject a)
            IAvoidObject.avoidances.add(a);

        if ((o.isSurfaceTile || !o.enableStacking) && m.posZ > 25)
            return false;

		double horizontalDist = Math.abs(m.posX - o.posX);
		double verticalDist = Math.abs(m.posY - o.posY);

		double distX = m.posX - o.posX;
		double distY = m.posY - o.posY;

		double bound = m.size / 2 + Game.tile_size / 2;

		if (horizontalDist < bound && verticalDist < bound && o.checkForObjects)
            o.onObjectEntry(m);

        if (!o.isSurfaceTile && !Game.lessThan(true, o.startHeight * Game.tile_size, m.posZ, o.startHeight * Game.tile_size + o.getTileHeight()))
            return false;

		boolean hasCollided = false;

        if ((!(m instanceof Tank ? o.tankCollision : o.bulletCollision) && !o.checkForObjects) || o.startHeight >= 1)
            return false;

        if (horizontalDist < bound && verticalDist < bound)
        {
            if (o.checkForObjects)
                o.onObjectEntry(m);

			if (o.isTransparent())
				m.drawTransparent = true;

            if (!o.tankCollision)
                return false;

			double bounciness = m.bounciness + o.getBounciness();

            if (!o.hasLeftNeighbor() && distX <= 0 && distX >= -bound && horizontalDist >= verticalDist)
            {
                hasCollided = true;
				m.vX *= -bounciness;
				m.vY *= bounciness > 1 ? bounciness : 1;
                m.posX += horizontalDist - bound;
            }
            else if (!o.hasUpperNeighbor() && distY <= 0 && distY >= -bound && horizontalDist <= verticalDist)
            {
                hasCollided = true;
				m.vY *= -bounciness;
				m.vX *= bounciness > 1 ? bounciness : 1;
                m.posY += verticalDist - bound;
            }
            else if (!o.hasRightNeighbor() && distX >= 0 && distX <= bound && horizontalDist >= verticalDist)
            {
                hasCollided = true;
				m.vX *= -bounciness;
				m.vY *= bounciness > 1 ? bounciness : 1;
                m.posX -= horizontalDist - bound;
            }
            else if (!o.hasLowerNeighbor() && distY >= 0 && distY <= bound && horizontalDist <= verticalDist)
            {
                hasCollided = true;
				m.vY *= -bounciness;
				m.vX *= bounciness > 1 ? bounciness : 1;
                m.posY -= verticalDist - bound;
            }
        }

		return hasCollided;
    }

    @Override
    public void preUpdate()
    {
		if (Math.abs(this.posZ) < disabledZ && Math.abs(this.lastPosZ) >= disabledZ && !positionLock)
            this.disabled = this.hidden = false;

		if (Math.abs(this.posZ) >= disabledZ && Math.abs(this.lastPosZ) < disabledZ && !positionLock)
            this.disabled = this.hidden = true;

        super.preUpdate();
    }

    @Override
    public void update()
    {
        if (this.networkID < 0)
        {
            // If you get this crash, please make sure you call Game.addTank() to add them to movables, or use registerNetworkID()!
            Game.exitToCrash(new RuntimeException("Network ID not assigned to tank!"));
        }

        if (this.age <= 0)
        {
            if (this.resistFreeze)
                this.attributeImmunities.addAll(Arrays.asList("ice_slip", "ice_accel", "ice_max_speed", "freeze"));

			this.emblemAngle = this.angle;
        }

		tankCount++;

		if (this instanceof IAvoidObject a)
			IAvoidObject.avoidances.add(a);

        this.age += Panel.frameFrequency;

        this.treadAnimation += Math.sqrt(this.lastFinalVX * this.lastFinalVX + this.lastFinalVY * this.lastFinalVY) * Panel.frameFrequency;

        if (this.enableTracks && this.treadAnimation > this.size * this.trackSpacing && !this.destroy)
        {
            this.drawTread = true;

            if (this.size > 0)
                this.treadAnimation %= this.size * this.trackSpacing;
        }

		this.flashAnimation = Math.max(0, this.flashAnimation - 0.05 * Panel.frameFrequency);

		if (destroy)
		{
			if (this.destroyTimer <= 0)
			{
				Game.eventsOut.add(new EventTankUpdateHealth(this, null));
				this.unregisterNetworkID();
			}

			if (this.destroyTimer <= 0 && this.health <= 1e-7)
			{
				Drawing.drawing.playGameSound("destroy.ogg", this, Game.tile_size * 80, (float) (Game.tile_size / this.size));

				this.onDestroy();

				if (Game.effectsEnabled)
				{
					for (int i = 0; i < this.size * 2 * Game.effectMultiplier; i++)
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.piece);
						double var = 50;

						e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.atan(Math.random()), Math.random() * this.size / 50.0);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);

						Game.effects.add(e);
					}
				}
			}

			this.destroyTimer += Panel.frameFrequency;
		}

		if (this.destroyTimer > Game.tile_size)
			Game.removeMovables.add(this);

		if (this.drawTread)
		{
			this.drawTread = false;
			this.drawTread();
		}

		this.accelerationModifier = 1;
		this.frictionModifier = 1;
		this.maxSpeedModifier = 1;

		double boost = 0;
		for (int i = 0; i < this.attributes.size(); i++)
		{
			AttributeModifier a = this.attributes.get(i);

			if (a.name.equals("healray"))
			{
				if (this.health < this.baseHealth)
				{
					this.attributes.remove(a);
                    i--;
                }
            }
        }

        this.accelerationModifier = this.getAttributeValue(AttributeModifier.acceleration, this.accelerationModifier);
		this.frictionModifier = this.getAttributeValue(AttributeModifier.friction, this.frictionModifier);
        this.buoyancy = this.getAttributeValue(AttributeModifier.buoyancy, this.buoyancy);

		if (this instanceof TankAIControlled && this.frictionModifier > 1)
			this.frictionModifier = 1.5;

        this.damageRate = this.getAttributeValue(AttributeModifier.damage, this.damageRate);
        if (this.damageRate > 0.01)
        {
            this.health -= this.damageRate / 100 * Panel.frameFrequency;
            this.flashAnimation = 1;
            this.damageRate = 0;
        }

        this.maxSpeedModifier = this.getAttributeValue(AttributeModifier.max_speed, this.maxSpeedModifier);

		boost = this.getAttributeValue(AttributeModifier.ember_effect, boost);

		if (!ScreenGame.finished && Math.random() * Panel.frameFrequency < boost * Game.effectMultiplier && Game.effectsEnabled)
		{
			Effect e = Effect.createNewEffect(this.posX, this.posY, Game.tile_size / 2, Effect.EffectType.piece);
			e.setColor(255, 180, 0, 50);

			if (Game.enable3d)
				e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random());
			else
				e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random());

			Game.effects.add(e);
		}

		if (basePitch != 0)
			basePitch /= Math.pow(1.04, Panel.frameFrequency);

		if (baseRoll != 0)
			baseRoll /= Math.pow(1.04, Panel.frameFrequency);

		if (Game.effectsEnabled && prevInWater != inWater && inWater)
		{
			double mult = Game.getObstacle(posX, posY) instanceof ObstacleLiquid w ? w.getTileHeight() / 50 : 0;

			for (double a = 0; a < 2 * Math.PI; a += Math.PI / Game.effectMultiplier * 0.05)
			{
				Effect e = Effect.createNewEffect(posX, posY, posZ, Effect.EffectType.snow)
						.setSize(5).setColor(40, 120, 255);
				e.vX = Math.cos(a) * Math.random() * mult;
				e.vY = Math.sin(a) * Math.random() * mult;
				e.vZ = Math.random() * mult;
				Game.effects.add(e);
			}

			Game.effects.add(Effect.createNewEffect(posX, posY, posZ, Effect.EffectType.splash));
			addStatusEffect(StatusEffect.water_speed, 0, 0, 0, 25);
		}

		prevInWater = inWater;
		inWater = false;

		if (!customPosZBehavior)
            updatePosZ();

		super.update();

		if (this.health <= 1e-7)
			this.destroy = true;

		if (this.managedMotion)
		{
			if (this.enableCollision)
				this.checkCollision();

			if (!this.collisionPush)
			{
				this.posX = this.lastPosX;
				this.posY = this.lastPosY;
			}

			this.orientation = (this.orientation + Math.PI * 2) % (Math.PI * 2);

			if (this.collisionPush && !(Math.abs(this.posX - this.lastPosX) < 0.01 && Math.abs(this.posY - this.lastPosY) < 0.01) && !this.destroy && !ScreenGame.finished)
			{
				double dist = Math.sqrt(Math.pow(this.posX - this.lastPosX, 2) + Math.pow(this.posY - this.lastPosY, 2));

				double dir = Math.PI + this.getAngleInDirection(this.lastPosX, this.lastPosY);
				if (Movable.absoluteAngleBetween(this.orientation, dir) <= Movable.absoluteAngleBetween(this.orientation + Math.PI, dir))
					this.orientation -= Movable.angleBetween(this.orientation, dir) / 20 * dist;
				else
					this.orientation -= Movable.angleBetween(this.orientation + Math.PI, dir) / 20 * dist;
			}
		}

		if (!this.isRemote && this.standardUpdateEvent && shouldUpdate && ScreenPartyHost.isServer)
			sendUpdateEvent();

		if (this.hiddenStatusChanged)
		{
			this.canHide = true;
			for (int i = 0; i < this.canHidePoints.length; i++)
			{
				for (int j = 0; j < this.canHidePoints[i].length; j++)
				{
					canHide = canHide && canHidePoints[i][j];
					canHidePoints[i][j] = false;
				}
			}

			this.hidden = true;
			for (int i = 0; i < this.hiddenPoints.length; i++)
			{
				for (int j = 0; j < this.hiddenPoints[i].length; j++)
				{
					hidden = hidden && hiddenPoints[i][j];
					hiddenPoints[i][j] = false;
				}
			}

			this.hiddenStatusChanged = false;
		}

		if (this.hasCollided)
            this.recoilSpeed *= 0.5;

		if (this.possessor != null)
			this.possessor.updatePossessing();
	}

	public void updatePosZ()
	{
		double maxTouchingZ = -9999;
		boolean allow = true;
		double s = Math.max(Game.tile_size, size) / 2;

		for (double x = posX - s; x <= posX + s; x += Game.tile_size)
		{
			for (double y = posY - s; y <= posY + s; y += Game.tile_size)
			{
				Obstacle o = Game.getObstacle(x, y);
				if (!(o instanceof ObstacleLiquid))
				{
					if (o == null || !o.tankCollision)
						maxTouchingZ = Math.max(0, maxTouchingZ);
					continue;
				}

				double horizontalDist = Math.abs(posX - o.posX);
				double verticalDist = Math.abs(posY - o.posY);
				double bound = size * 0.9;

				if ((horizontalDist <= bound || verticalDist <= bound) && maxTouchingZ < o.getTileHeight())
				{
					maxTouchingZ = o.getTileHeight();
					allow = Math.abs(posZ - maxTouchingZ) < Game.tile_size;
				}
			}
		}

		double mult = 1;

		if (posZ < maxTouchingZ)
		{
			if (tiltFirstFrame)
				tiltDirection = (int) -Math.signum(Movable.angleBetween(orientation, getPolarDirection()) - Math.PI / 2);

			tiltFirstFrame = false;
			maxSpeedModifier *= 1 / mult * (allow ? 1 : 0);
			posZ += (allow ? 2 : 1) * mult * Panel.frameFrequency;
			basePitch = Math.min(allow ? 0.3 : 0.6, Math.abs(basePitch) + 0.05 / mult * Panel.frameFrequency) * tiltDirection;
		}
		else if (Math.abs(posZ - maxTouchingZ) < 1)
		{
			posZ = maxTouchingZ;
			tiltFirstFrame = true;
		}
		else if (posZ > maxTouchingZ)
		{
			posZ -= size / 70;

			if (tiltFirstFrame)
				tiltDirection = (int) Math.signum(Movable.angleBetween(orientation, getPolarDirection()) - Math.PI / 2);

			tiltFirstFrame = false;
			basePitch = Math.min(0.2, Math.abs(basePitch) + 0.04 / mult * Panel.frameFrequency) * tiltDirection;
		}
	}

	public void drawTread()
	{
		double a = this.orientation;
		Effect e1 = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.tread);
		Effect e2 = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.tread);
		e1.setPolarMotion(a - Math.PI / 2, this.size * 0.25);
		e2.setPolarMotion(a + Math.PI / 2, this.size * 0.25);
		e1.size = this.size / 5;
		e2.size = this.size / 5;
		e1.posX += e1.vX;
		e1.posY += e1.vY;
		e2.posX += e2.vX;
		e2.posY += e2.vY;
		e1.angle = a;
		e2.angle = a;
		e1.setPolarMotion(0, 0);
		e2.setPolarMotion(0, 0);
		this.setEffectHeight(e1);
		this.setEffectHeight(e2);
		e1.firstDraw();
		e2.firstDraw();
		Game.tracks.add(e1);
		Game.tracks.add(e2);
	}

	public void drawForInterface(double x, double y, double sizeMul)
	{
        double s = this.size;

        if (this.size > Game.tile_size * 1.5)
            this.size = Game.tile_size * 1.5;

        this.size *= sizeMul;
        this.drawForInterface(x, y);
        this.size = s;
    }

    @Override
    public void postInitSelectors()
    {
        this.teamSelector = (TeamSelector<Tank>) this.selectors.get(0);
        this.rotationSelector = (RotationSelector<Tank>) this.selectors.get(1);
    }

    @Override
    public void drawForInterface(double x, double y)
    {
        double x1 = this.posX;
        double y1 = this.posY;
        this.posX = x;
        this.posY = y;
        this.drawTank(true, false);
        this.posX = x1;
		this.posY = y1;	
	}

	public void drawTank(boolean forInterface, boolean interface3d)
	{
		double luminance = this.getAttributeValue(AttributeModifier.glow, this.luminance);
		double glow = this.getAttributeValue(AttributeModifier.glow, 1);

		double s = (this.size * (Game.tile_size - destroyTimer) / Game.tile_size) * Math.min(this.drawAge / Game.tile_size, 1);
		double sizeMod = 1;

		if (forInterface && !interface3d)
			s = Math.min(this.size, Game.tile_size * 1.5);

		Drawing drawing = Drawing.drawing;
		double[] teamColor = Team.getObjectColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB, this);

		Drawing.drawing.setColor(teamColor[0] * glow * this.glowIntensity, teamColor[1] * glow * this.glowIntensity, teamColor[2] * glow * this.glowIntensity, 255, 1);

		if (Game.glowEnabled)
		{
			double size = this.glowSize * s;
			if (forInterface)
				Drawing.drawing.fillInterfaceGlow(this.posX, this.posY, size, size);
			else if (!Game.enable3d)
				Drawing.drawing.fillGlow(this.posX, this.posY, size, size);
			else
				Drawing.drawing.fillGlow(this.posX, this.posY, Math.max(this.size / 4, 11), size, size,true, false);
		}

		if (this.lightIntensity > 0 && this.lightSize > 0)
		{
			double i = this.lightIntensity;

			while (i > 0)
			{
				double size = this.lightSize * s * i / this.lightIntensity;
				Drawing.drawing.setColor(255, 255, 255, i * 255);

				if (!(forInterface && !interface3d))
					Drawing.drawing.fillForcedGlow(this.posX, this.posY, 0, size, size, false, false, false, true);

				i--;
			}
		}

		if (this.fullBrightness)
			luminance = 1;

		if (!forInterface)
		{
			for (AttributeModifier a : this.attributes)
			{
				if (a.name.equals("healray"))
				{
					double mod = 1 + 0.4 * Math.min(1, this.health - this.baseHealth);

					if (this.health > this.baseHealth)
					{
						if (!Game.enable3d)
						{
							Drawing.drawing.setColor(0, 255, 0, 255, 1);
							drawing.drawModel(this.baseModel, this.posX, this.posY, s * mod, s * mod, this.orientation);
						}
						else
						{
							Drawing.drawing.setColor(0, 255, 0, 127, 1);
							drawing.drawModel(this.baseModel, this.posX, this.posY, this.posZ, s * mod, s * mod, s - 2, this.orientation, basePitch, baseRoll);
						}
					}
				}
			}
		}

		Drawing.drawing.setColor(teamColor[0], teamColor[1], teamColor[2], 255, luminance);

		if (forInterface)
		{
			if (interface3d)
				drawing.drawInterfaceModel(this.baseModel, this.posX, this.posY, this.posZ, s, s, s, this.orientation, 0, 0);
			else
				drawing.drawInterfaceModel(this.baseModel, this.posX, this.posY, s, s, this.orientation);
		}
		else
		{
			if (Game.enable3d)
				drawing.drawModel(this.baseModel, this.posX, this.posY, this.posZ, s, s, s, this.orientation, basePitch, baseRoll);
			else
				drawing.drawModel(this.baseModel, this.posX, this.posY, s, s, this.orientation);
		}

		double flash = Math.min(1, this.flashAnimation);

		Drawing.drawing.setColor(this.colorR * (1 - flash) + 255 * flash, this.colorG * (1 - flash), this.colorB * (1 - flash), 255, luminance);

		if (forInterface)
		{
			if (interface3d)
				drawing.drawInterfaceModel(this.colorModel, this.posX, this.posY, this.posZ, s * sizeMod, s * sizeMod, s * sizeMod, this.orientation, 0, 0);
			else
				drawing.drawInterfaceModel(this.colorModel, this.posX, this.posY, s * sizeMod, s * sizeMod, this.orientation);
		}
		else
		{
			if (Game.enable3d)
				drawing.drawModel(this.colorModel, this.posX, this.posY, this.posZ, s, s, s, this.orientation, basePitch, baseRoll);
			else
				drawing.drawModel(this.colorModel, this.posX, this.posY, s, s, this.orientation);
		}

		if (this.health > 1 && this.size > 0 && !forInterface)
		{
			double size = s;
			for (int i = 1; i < Math.min(health, 6); i++)
			{
				if (Game.enable3d)
					drawing.drawModel(health_model,
							this.posX, this.posY, this.posZ + s / 4,
							size, size, s,
							this.orientation, basePitch, baseRoll);
				else
					drawing.drawModel(health_model,
							this.posX, this.posY,
							size, size,
							this.orientation);

				size *= 1.1;
			}
		}

		this.drawTurret(forInterface, interface3d || (!forInterface && Game.enable3d), false);

		sizeMod = 0.5;

		Drawing.drawing.setColor(this.emblemR, this.emblemG, this.emblemB, 255, luminance);
		if (this.emblem != null)
		{
			if (forInterface)
			{
				if (interface3d)
					drawing.drawInterfaceImage(0, this.emblem, this.posX, this.posY, 0.82 * s, s * sizeMod, s * sizeMod);
				else
					drawing.drawInterfaceImage(this.emblem, this.posX, this.posY, s * sizeMod, s * sizeMod);
			}
			else
			{
				double a = age > 0 ? this.angle + this.emblemAngle : 0;

				if (Game.enable3d)
                    drawing.drawImage(a, this.emblem, this.posX, this.posY, this.posZ + 0.81 * s, s * sizeMod, s * sizeMod);
				else
					drawing.drawImage(a, this.emblem, this.posX, this.posY, s * sizeMod, s * sizeMod);
			}
		}

		if (Game.showTankIDs)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setFontSize(30);
			Drawing.drawing.drawText(this.posX, this.posY, 50, this.networkID + "");
		}

		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB);
	}

	public void drawTurret(boolean forInterface, boolean in3d, boolean transparent)
	{
		this.turret.draw(angle, pitch, forInterface, in3d, transparent);
	}

	@Override
	public void draw()
	{
		if (!Game.game.window.drawingShadow)
			drawAge += Panel.frameFrequency;

		updateSelectors();

		this.drawTank(false, false);

		if (this.possessor != null)
		{
			this.possessor.drawPossessing();
			this.possessor.drawGlowPossessing();
		}
	}

	public void drawOutline() 
	{
		drawAge = Game.tile_size;
		Drawing drawing = Drawing.drawing;

		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 127);
		drawing.fillRect(this.posX - this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX + this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX, this.posY - this.size * 0.4, this.size * 0.6, this.size * 0.2);
		drawing.fillRect(this.posX, this.posY + this.size * 0.4, this.size * 0.6, this.size * 0.2);

		this.drawTurret(false, false, true);

		if (this.emblem != null)
		{
			Drawing.drawing.setColor(this.emblemR, this.emblemG, this.emblemB, 127);
			drawing.drawImage(this.angle, this.emblem, this.posX, this.posY, this.size / 2, this.size / 2);
		}

		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB);
	}

	public void drawAt(double x, double y)
	{	
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawTank(false, false);
		this.posX = x1;
		this.posY = y1;	
	}

	public void drawOutlineAt(double x, double y)
	{
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawOutline();
		this.posX = x1;
		this.posY = y1;
	}

	@Override
	public void addAttribute(AttributeModifier m)
	{
		super.addAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventTankAddAttributeModifier(this, m, false));
	}

	@Override
	public void addUnduplicateAttribute(AttributeModifier m)
	{
		super.addUnduplicateAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventTankAddAttributeModifier(this, m, true));
	}

	public void onDestroy()
	{
		if (this instanceof IAvoidObject a)
			IAvoidObject.avoidances.remove(a);

		if (this.explodeOnDestroy && !(this.droppedFromCrate && this.age < 250))
            new Explosion(this.posX, this.posY, this.mine.radius, this.mine.damage, this.mine.destroysObstacles, this).explode();
	}

	public boolean damage(double amount, GameObject source)
	{
		return damage(amount, source, true);
	}

	public boolean damage(double amount, GameObject source, boolean playDamageSound)
	{
		double finalAmount = amount * this.getDamageMultiplier(source);
		this.health -= finalAmount;

		if (this.health <= 1)
		{
			for (int i = 0; i < this.attributes.size(); i++)
			{
				if (this.attributes.get(i).type.name.equals("healray"))
				{
					this.attributes.remove(i);
					i--;
				}
			}
		}

		boolean kill = this.health <= 1e-7;
		float pitch = source instanceof Bullet b ? (float) (Bullet.bullet_size / b.size) : 1;

		if (finalAmount > 0 && !kill && playDamageSound)
			Drawing.drawing.playGameSound("damage.ogg", this, Game.tile_size * 25, pitch);

		Game.eventsOut.add(new EventTankUpdateHealth(this, source));

		Tank owner = null;

		if (source instanceof Bullet)
			owner = ((Bullet) source).tank;
		else if (source instanceof Explosion)
			owner = ((Explosion) source).tank;
		else if (source instanceof Tank)
			owner = (Tank) source;

		if (this.health > 0)
		{
			if (finalAmount > 0)
				this.flashAnimation = 1;
		}
		else
			this.destroy = true;

		this.checkHit(owner, source);

		if (this.health > 6 && (int) (this.health + amount) != (int) (this.health))
		{
			Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ + this.size * 0.75, Effect.EffectType.shield);
			e.size = this.size;
			e.radius = this.health - 1;
			Game.effects.add(e);
		}

		return kill;
	}

    public void checkHit(Tank owner, GameObject source)
    {
		if (this.health <= 1e-7)
		{
			int coins = Game.currentGame != null ? Game.currentGame.playerKillCoins : this.coinValue;

			if (coins > 0)
			{
				if (owner instanceof TankPlayer t)
				{
					t.player.hotbar.coins += coins;
					Game.eventsOut.add(new EventUpdateCoins(t.player));
				}
				else if (owner instanceof TankPlayerRemote t)
				{
					t.player.hotbar.coins += coins;
					Game.eventsOut.add(new EventUpdateCoins(t.player));
				}
			}
		}

        if (Crusade.crusadeMode && Crusade.currentCrusade != null && !ScreenPartyLobby.isClient)
        {
            if (owner instanceof IServerPlayerTank)
            {
                CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(((IServerPlayerTank) owner).getPlayer());

                if (cp != null && this.health <= 1e-7)
                {
                    if (this.possessor != null && this.possessor.overridePossessedKills)
						cp.addKill(this.getTopLevelPossessor());
					else
						cp.addKill(this);
				}

				if (cp != null && (source instanceof Bullet || source instanceof Explosion))
					cp.addItemHit(source);
			}

			if (owner != null && this instanceof IServerPlayerTank && this.health <= 1e-7)
			{
				CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(((IServerPlayerTank) this).getPlayer());

				if (cp != null)
				{
					if (owner.possessor != null && owner.possessor.overridePossessedKills)
						cp.addDeath(owner.getTopLevelPossessor());
					else
						cp.addDeath(owner);
				}
			}
		}
	}

	@Override
	public boolean disableRayCollision()
	{
		return !enableCollision;
	}

	public double getDamageMultiplier(GameObject source)
    {
        if (this.invulnerable || (source instanceof Bullet && this.resistBullets) || (source instanceof Explosion && this.resistExplosions) || ScreenGame.finishedQuick)
            return 0;

		if (source instanceof Movable m && this.team != null && !this.team.friendlyFire && Team.isAllied(m, this))
			return 0;

        return 1;
    }

	@Override
	public void onExploded(Explosion explosion)
	{
		this.damage(explosion.damage, explosion);
	}

	@Override
	public void applyExplosionKnockback(double angle, double power, Explosion explosion)
	{
		this.addPolarMotion(angle, power * explosion.tankKnockback * Math.pow(Game.tile_size, 2) / Math.max(1, Math.pow(this.size, 2)));
		this.recoilSpeed = this.getSpeed();

		if (this.recoilSpeed > this.maxSpeed)
		{
			this.inControlOfMotion = false;
			this.tookRecoil = true;
		}
	}

	@Override
	public double getSize()
	{
		return this.size;
	}

	public void setEffectHeight(Effect e)
	{
		if (Game.enable3d && Game.enable3dBg && Game.glowEnabled)
		{
			e.posZ = posZ;
			for (int i = 0; i < Game.dirX.length; i++)
				e.posZ = Math.max(e.posZ, Game.sampleGroundHeight(e.posX + e.size / 2 * Game.dirX[i], e.posY + e.size / 2 * Game.dirY[i]));
			e.posZ++;
		}
		else
			e.posZ = 1;
	}

    @Override
    public void registerSelectors()
    {
        this.registerSelector(new TeamSelector<Tank>());
        this.registerSelector(new RotationSelector<Tank>());
    }

	@Override
	public boolean supportsTransparency()
	{
		return true;
	}

	public void updatePossessing()
    {

    }

    public void drawPossessing()
    {

    }

	public void drawGlowPossessing()
	{

	}

	public AutoZoom getAutoZoomRaw()
	{
		double nearest = Double.MAX_VALUE;
		Tank nearestTank = null;
		double farthestInSight = -1;

		for (Movable m: Game.movables)
		{
			if (m instanceof Tank && !Team.isAllied(m, this) && m != this && !((Tank) m).hidden && !m.destroy)
			{
				double boundedX = Math.min(Math.max(this.posX, Drawing.drawing.interfaceSizeX * 0.4),
						Game.currentSizeX * Game.tile_size - Drawing.drawing.interfaceSizeX * 0.4);
				double boundedY = Math.min(Math.max(this.posY, Drawing.drawing.interfaceSizeY * 0.4),
						Game.currentSizeY * Game.tile_size - Drawing.drawing.interfaceSizeY * 0.4);

				double xDist = Math.abs(m.posX - boundedX);
				double yDist = Math.abs(m.posY - boundedY);
				double dist = Math.max(xDist / (Drawing.drawing.interfaceSizeX), yDist / (Drawing.drawing.interfaceSizeY)) * 2.2;

				if (dist < nearest)
				{
					nearest = dist;
					nearestTank = (Tank) m;
				}

				if (dist <= 3.5 && dist > farthestInSight)
				{
					Ray r = new Ray(this.posX, this.posY, 0, 0, this);
					r.vX = m.posX - this.posX;
					r.vY = m.posY - this.posY;

					if ((m == this.lastFarthestInSight && System.currentTimeMillis() - this.lastFarthestInSightUpdate <= 1000)
							|| r.getTarget() == m)
					{
						farthestInSight = dist;
						this.lastFarthestInSight = (Tank) m;
						this.lastFarthestInSightUpdate = System.currentTimeMillis();
					}
				}
			}
		}

		Tank focus = nearest > farthestInSight ? nearestTank : this.lastFarthestInSight;
		if (focus == null)
			return new AutoZoom(0, 0, 0);

		return new AutoZoom(Math.max(nearest, farthestInSight), this.posX - focus.posX, this.posY - focus.posY);
	}

	public void setMetadata(String s)
	{
		String[] data = s.split("-");

		for (int i = 0; i < Math.min(data.length, this.selectorCount()); i++)
		{
			LevelEditorSelector<Tank> sel = (LevelEditorSelector<Tank>) this.selectors.get(saveOrder(i));
			sel.setMetadata(data[i]);
		}
	}

	public String getMetadata()
	{
		StringBuilder s = new StringBuilder();
		int sc = this.selectorCount();
		for (int i = 0; i < sc; i++)
			s.append(this.selectors.get(saveOrder(i)).getMetadata()).append("-");

		String s1 = s.toString();
		if (s1.endsWith("-"))
			return s1.substring(0, s1.length() - 1);
		return s1;
	}

	/** Override this method if both the server and clients support a custom creation event for your modded tank. */
	public void sendCreateEvent()
	{
		Game.eventsOut.add(new EventTankCreate(this));
	}

	/** Override this method if both the server and clients support a custom update event for your modded tank. */
	public void sendUpdateEvent()
	{
		updatesPerFrame++;
		Game.eventsOut.add(new EventTankUpdate(this));
	}

	public boolean canShoot()
	{
		return !this.disabled && !Game.bulletLocked;
	}

	public AutoZoom getAutoZoom()
	{
		AutoZoom raw = getAutoZoomRaw();
		double dist = Math.min(3, Math.max(1, raw.zoom));
		double targetScale = Drawing.drawing.interfaceScale / dist;
		double zoom = Math.max(Math.min((targetScale - Drawing.drawing.unzoomedScale) / (Drawing.drawing.interfaceScale - Drawing.drawing.unzoomedScale), 1), 0);
		return new AutoZoom(zoom, Math.min(AutoZoom.maxPanDist, raw.panX), Math.min(AutoZoom.maxPanDist, raw.panY));
	}

	// java 16 :D
	public record AutoZoom(double zoom, double panX, double panY)
	{
		public static double maxPanDist = Game.tile_size * 8;
	}

	public void setBufferCooldown(double value)
	{
		this.bullet.cooldown = Math.max(this.bullet.cooldown, value);
		this.mine.cooldown = Math.max(this.mine.cooldown, value);
	}

	/** This is for backwards compatibility saving with the base game. */
	public int saveOrder(int index)
	{
		if (index < 2)
			return 1 - index;
		return index;
	}

	public Tank getTopLevelPossessor()
	{
		if (this.possessor == null)
			return null;

		Tank p = this.possessor;
		while (p.possessor != null)
			p = p.possessor;
		return p;
	}

	public Tank getBottomLevelPossessing()
	{
		Tank p = this;
		while (p.possessingTank != null)
		{
			p = p.possessingTank;
		}

		return p;
	}

	public void drawSpinny(double s)
	{
		double fade = Math.max(0, Math.sin(Math.min(s, 50) / 100 * Math.PI));

		double frac = (System.currentTimeMillis() % 2000) / 2000.0;
		double size = Math.max(800 * (0.5 - frac), 0) * fade;
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 64 * Math.sin(Math.min(frac * Math.PI, Math.PI / 2)) * fade);

		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.size / 2, size, size, false, false);
		else
			Drawing.drawing.fillOval(this.posX, this.posY, size, size);

		double frac2 = ((250 + System.currentTimeMillis()) % 2000) / 2000.0;
		double size2 = Math.max(800 * (0.5 - frac2), 0) * fade;

		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB, 64 * Math.sin(Math.min(frac2 * Math.PI, Math.PI / 2)) * fade);

		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.size / 2, size2, size2, false, false);
		else
			Drawing.drawing.fillOval(this.posX, this.posY, size2, size2);

		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
		this.drawSpinny(this.posX, this.posY, this.size / 2, 200, 4, 0.3, 75 * fade, 0.5 * fade, false);
		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB);
		this.drawSpinny(this.posX, this.posY, this.size / 2, 198, 3, 0.5, 60 * fade, 0.375 * fade, false);
	}

	public void drawSpinny(double x, double y, double z, int max, int parts, double speed, double size, double dotSize, boolean invert)
	{
		for (int i = 0; i < max; i++)
		{
			double frac = (System.currentTimeMillis() / 1000.0 * speed + i * 1.0 / max) % 1;
			double s = Math.max(Math.abs((i % (max * 1.0 / parts)) / 10.0 * parts), 0);

			if (invert)
			{
				frac = -frac;
			}

			double v = size * Math.cos(frac * Math.PI * 2);
			double v1 = size * Math.sin(frac * Math.PI * 2);

			if (Game.enable3d)
				Drawing.drawing.fillOval(x + v, y + v1, z, s * dotSize, s * dotSize, false, false);
			else
				Drawing.drawing.fillOval(x + v, y + v1, s * dotSize, s * dotSize);
		}
	}

	public static void drawTank(double x, double y, double r1, double g1, double b1, double r2, double g2, double b2)
	{
		drawTank(x, y, r1, g1, b1, r2, g2, b2, Game.tile_size / 2);
	}

	public static void drawTank(double x, double y, double r1, double g1, double b1, double r2, double g2, double b2, double size)
	{
		Drawing.drawing.setColor(r2, g2, b2);
		Drawing.drawing.drawInterfaceModel(TankModels.tank.base, x, y, size, size, 0);

		Drawing.drawing.setColor(r1, g1, b1);
		Drawing.drawing.drawInterfaceModel(TankModels.tank.color, x, y, size, size, 0);

		Drawing.drawing.setColor(r2, g2, b2);

		Drawing.drawing.drawInterfaceModel(TankModels.tank.turret, x, y, size, size, 0);

		Drawing.drawing.setColor((r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2);
		Drawing.drawing.drawInterfaceModel(TankModels.tank.turretBase, x, y, size, size, 0);
	}
}
