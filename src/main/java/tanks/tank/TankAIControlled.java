package tanks.tank;

import basewindow.IModel;
import tanks.*;
import tanks.bullet.*;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.network.event.*;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryTank;

import java.lang.reflect.Field;
import java.util.*;

import static tanks.TankReferenceSolver.*;
import static tanks.tank.TankProperty.Category.*;

/** This class is the 'skeleton' tank class.
 *  It can be extended and values can be changed to easily produce an AI for another tank.
 *  Also, the behavior is split into many methods which are intended to be overridden easily.*/
public class TankAIControlled extends Tank
{
	public static int maxReachableCheckChunks = 3;
	public static double maxReachableCheckDist = maxReachableCheckChunks * Chunk.chunkSize * Game.tile_size;

	public static int[] dirX = {1, -1, 0, 0, 1, -1, 1, -1};
	public static int[] dirY = {0, 0, 1, -1, -1, 1, 1, -1};
	protected static TankAIControlled compare;

	private boolean propsCloned;

	/** The type which shows what direction the tank is moving. Clockwise and Counter Clockwise are for idle, while Aiming is for when the tank aims.*/
	protected enum RotationPhase {clockwise, counter_clockwise, aiming}

	// The following are properties which are used externally to determine the behavior settings of the tank.
	// Simple modifications of tanks can just change these values to produce a desired behavior.
	// More complex behaviors may require overriding of methods.
	// These values do not change normally along the course of the game.

	/** When set to true, the tank will vanish when the level begins*/
	@TankProperty(category = appearanceGeneral, id = "invisible", name = "Invisible")
	public boolean invisible = false;

	@TankProperty(category = movementGeneral, id = "enable_movement", name = "Can move")
	public boolean enableMovement = true;

	/** Chance per frame to change direction*/
	@TankProperty(category = movementIdle, id = "motion_change_chance", name = "Turn chance", desc = "Chance of the tank to change the direction in which it is moving")
	public double turnChance = 0.01;
	/** Time waited when changing direction of motion*/
	@TankProperty(category = movementIdle, id = "turn_pause_time", name = "Turn pause time", desc = "Time the tank pauses when changing directions \n \n 1 time unit = 0.01 seconds")
	public double turnPauseTime = 15;
	/** Multiplier of time the tank will hide in a shrub*/
	@TankProperty(category = movementIdle, id = "bush_hide_time", name = "Bush hide time", desc = "Time the tank will stop moving to hide in bushes \n \n 1 time unit = 0.01 seconds")
	public double bushHideTime = 350;

	@TankProperty(category = movementIdle, id = "stay_near_parent", name = "Stay near parent", desc = "If spawned by another tank, whether this tank should try to stay near the tank that spawned it")
	public boolean stayNearParent = false;
	@TankProperty(category = movementIdle, id = "max_distance_from_parent", name = "Parent boundary", desc = "If stay near parent is set and this tank strays farther than this distance from the tank that spawned it, it will return to that tank \n \n 1 tile = 50 units")
	public double maxDistanceFromParent = 300;

	@TankProperty(category = movementAvoid, id = "enable_bullet_avoidance", name = "Avoid bullets")
	public boolean enableBulletAvoidance = true;
	@TankProperty(category = movementAvoid, id = "enable_mine_avoidance", name = "Avoid mines")
	public boolean enableMineAvoidance = true;
	@TankProperty(category = movementAvoid, id = "avoid_seek_open_spaces", name = "Seek open spaces", desc = "If enabled, when this tank avoids farther bullets, it will seek out open spaces around it to make it harder to corner")
	public boolean avoidanceSeekOpenSpaces = false;

	/** The method used to avoid bullets
	 *  Back off = move away from the bullet directly
	 *  Dodge = move at an angle from the bullet
	 *  Aggressive Dodge = move at an angle toward the bullet
	 *  Intersect = move away from where bullet path will intersect tank; less accurate */
	public enum BulletAvoidBehavior
	{intersect, back_off, dodge, aggressive_dodge}

	@TankProperty(category = movementAvoid, id = "bullet_avoid_behavior", name = "Bullet avoid behavior",
		desc = "Method the tank will use to avoid bullets",
		choiceDesc = {
			"Intersect: move away from where the bullet will hit the tank (less accurate)",
			"Back off: move away from the bullet (may back into corners)",
			"Dodge: move at an angle away from the bullet (more accurate)",
			"Aggressive dodge: move at an angle toward the bullet (gigachad)"
		}
	)
	public BulletAvoidBehavior bulletAvoidBehvavior = BulletAvoidBehavior.intersect;
	/** How close the tank needs to get to a mine to avoid it*/
	@TankProperty(category = movementAvoid, id = "mine_avoid_sensitivity", name = "Mine sight radius", desc = "If the tank is within this fraction of a mine's radius, it will move away from the mine")
	public double mineAvoidSensitivity = 1.5;
	/** Time which the tank will avoid a bullet after the bullet is no longer aiming at the tank*/
	@TankProperty(category = movementAvoid, id = "bullet_avoid_timer_base", name = "Bullet flee time", desc = "Time the tank will continue fleeing from a bullet until after it is no longer deemed a threat \n \n 1 time unit = 0.01 seconds")
	public double bulletAvoidTimerBase = 30;

	/** If enabled, the tank may actively seek out enemies*/
	@TankProperty(category = movementPathfinding, id = "enable_pathfinding", name = "Seek targets", desc = "If enabled, the tank may decide to navigate through the level towards its target. If this tank can lay mines, it may also use them to get to the target.")
	public boolean enablePathfinding = false;
	/** Chance per frame to seek the target enemy*/
	@TankProperty(category = movementPathfinding, id = "seek_chance", name = "Seek chance", desc = "Chance for this tank to decide to start navigating to its target")
	public double seekChance = 0.001;
	/** If set to true, when enters line of sight of target enemy, will stop pathfinding to it*/
	@TankProperty(category = movementPathfinding, id = "stop_seeking_on_sight", name = "Stop on sight", desc = "If enabled, navigation to target will end when the this tank enters the target's line of sight \n \n 1 time unit = 0.01 seconds")
	public boolean stopSeekingOnSight = false;
	/** Increasing this value increases how stubborn the tank is in following a path*/
	@TankProperty(category = movementPathfinding, id = "seek_timer_base", name = "Seek patience", desc = "If this tank is blocked from navigating its path for this amount of time, it will abandon the navigation \n \n 1 time unit = 0.01 seconds")
	public double seekTimerBase = 200;

	/** Type of behavior tank should have if its target enemy is in line of sight
	 * 	Approach = go towards the target enemy
	 * 	Flee = go away from the target enemy
	 * 	Strafe = move perpendicular to target enemy
	 * 	Sidewind = move at a 45 degree angle toward target enemy
	 * 	Backwind = move at a 45 degree angle away target enemy
	 * 	Keep Distance = stay a particular distance away from the target enemy*/
	public enum TargetEnemySightBehavior {approach, flee, strafe, sidewind, backwind, keep_distance}

	/** When set to true, will shoot a ray at the target enemy and enable reactions when the target enemy is in sight*/
	@TankProperty(category = movementOnSight, id = "enable_looking_at_target_enemy", name = "Test sight", desc = "When enabled, the tank will test if its target is in its line of sight, and react accordingly")
	public boolean enableLookingAtTargetEnemy = true;
	/** When set to true, will call reactToTargetEnemySight() when an unobstructed line of sight to the target enemy can be made */
	public boolean enableTargetEnemyReaction = true;
	/** Type of behavior tank should have if its target enemy is in line of sight*/
	@TankProperty(category = movementOnSight, id = "target_enemy_sight_behavior", name = "Reaction",
			desc = "How the tank should react upon line of sight",
			choiceDesc = {
			"Approach - move directly toward the target",
			"Flee - move directly away from the target",
			"Strafe around it - move perpendicular to the target",
			"Sidewind - zig-zag toward the target",
			"Backwind - zig-zag away from the target",
			"Keep distance - move to or away from the target until at a specific distance to it"
	})
	public TargetEnemySightBehavior targetEnemySightBehavior = TargetEnemySightBehavior.approach;
	/** If set to strafe upon seeing the target enemy, chance to change orbit direction*/
	@TankProperty(category = movementOnSight, id = "strafe_direction_change_chance", name = "Strafe frequency", desc = "If set to strafe on line of sight, chance the tank should change the direction it is strafing around the target")
	public double strafeDirectionChangeChance = 0.01;
	/** If set to keep a distance, the tank will maintain that distance from its target upon sight*/
	@TankProperty(category = movementOnSight, id = "target_sight_distance", name = "Target distance", desc = "If set to keep distance on line of sight, how far away the tank will try to sit from its target \n \n 1 tile = 50 units")
	public double targetSightDistance = Game.tile_size * 6;

	/** Tank to transform into*/
	@TankProperty(category = transformationOnSight, id = "sight_transform_tank", name = "Transformation tank", desc = "When set, the tank will transform into this tank upon entering line of sight with its target")
	public TankAIControlled sightTransformTank = null;
	/** Time for tank to revert after losing line of sight */
	@TankProperty(category = transformationOnSight, id = "sight_transformation_revert_time", name = "Sight revert time", desc = "After this much time has passed without the target in line of sight, the tank will revert back to its original form \n \n 1 time unit = 0.01 seconds")
	public double sightTransformRevertTime = 500;

	/** Tank to transform into*/
	@TankProperty(category = transformationOnHealth, id = "health_transform_tank", name = "Transformation tank", desc = "When set, the tank will transform into this tank when its health is at or below the health threshold")
	public TankAIControlled healthTransformTank = null;
	/** Health threshold to transform */
	@TankProperty(category = transformationOnHealth, id = "transform_health_threshold", name = "Hitpoint threshold", desc = "Amount of health this tank must have equal to or less than to transform")
	public double transformHealthThreshold = 0;

	@TankProperty(category = transformationOnTime, id = "time_transform_tank", name = "Transformation tank", desc = "When set, the tank will transform into this tank after a certian amount of time")
	public TankAIControlled timeTransformTank = null;

	@TankProperty(category = transformationOnTime, id = "transformation_time", name = "Time before transform", desc = "The tank will transform after this amount of time")
	public double transformTimerBase = 200;

	/** If set, the tank will seek and transform into other tanks in line of sight */
	@TankProperty(category = transformationMimic, id = "transform_mimic", name = "Mimic", desc = "When enabled, the tank will mimic other nearby tanks it sees")
	public boolean transformMimic = false;

	/** Time for tank to revert after losing line of sight */
	@TankProperty(category = transformationMimic, id = "mimic_revert_time", name = "Mimic revert time", desc = "After this much time has passed without the target in line of sight, the tank will revert back to its original form \n \n 1 time unit = 0.01 seconds")
	public double mimicRevertTime = 200;
	/** Range tanks must be in to be mimicked */
	@TankProperty(category = transformationMimic, id = "mimic_range", name = "Mimic range", desc = "Maximum distance between this tank and a tank it mimics")
	public double mimicRange = Game.tile_size * 12;

	@TankProperty(category = mines, id = "enable_mine_laying", name = "Can lay mines")
	public boolean enableMineLaying = true;

	//public double mineFuseLength = 1000;
	/** Minimum time to lay a mine, added to mineTimerRandom * this.random.nextDouble()*/
	@TankProperty(category = mines, id = "mine_timer_base", name = "Base cooldown", desc = "Minimum time between laying mines \n \n 1 time unit = 0.01 seconds \n \n Note - tanks will not lay mines faster than their mine's base cooldown allows!")
	public double mineTimerBase = 2000;
	/** Random factor in calculating time to lay a mine, multiplied by this.random.nextDouble() and added to mineTimerBase*/
	@TankProperty(category = mines, id = "mine_timer_random", name = "Random cooldown", desc = "A random percentage between 0% and 100% of this time value is added to the base cooldown to get the time between laying mines \n \n 1 time unit = 0.01 seconds \n \n Note - tanks will not lay mines faster than their mine's base cooldown allows!")
	public double mineTimerRandom = 4000;

	/** Minimum time in between shooting bullets, added to cooldownRandom * this.random.nextDouble()*/
	@TankProperty(category = firingGeneral, id = "cooldown_base", name = "Base cooldown", desc = "Minimum time between firing bullets \n \n 1 time unit = 0.01 seconds \n \n Note - tanks will not fire faster than their bullet's base cooldown allows!")
	public double cooldownBase = 60;
	/** Random factor in calculating time between shooting bullets, multiplied by this.random.nextDouble() and added to cooldownBase*/
	@TankProperty(category = firingGeneral, id = "cooldown_random", name = "Random cooldown", desc = "A random percentage between 0% and 100% of this time value is added to the base cooldown to get the time between firing bullets \n \n Note - tanks will not fire faster than their bullet's base cooldown allows!")
	public double cooldownRandom = 20;
	/** After every successive shot, cooldown will go down by this fraction */
	@TankProperty(category = firingGeneral, id = "cooldown_speedup", name = "Cooldown speedup", desc = "After every shot fired towards the same target, the cooldown will be decreased by this fraction of its current value")
	public double cooldownSpeedup = 0;
	/** Cooldown resets after no shots for this much time */
	@TankProperty(category = firingGeneral, id = "cooldown_revert_time", name = "Revert time", desc = "If the tank is unable to fire for this much time, the effects of cooldown speedup will reset \n \n 1 time unit = 0.01 seconds")
	public double cooldownRevertTime = 300;
	/** If set, the tank will charge a shot and wait its cooldown on the spot as it prepares to shoot */
	@TankProperty(category = firingGeneral, id = "charge_up", name = "Charge up", desc = "If enabled, the tank will only wait its cooldown while aiming at an enemy tank, playing a charge up animation")
	public boolean chargeUp = false;

	public enum TargetType {allies, enemies}

	@TankProperty(category = firingGeneral, id = "target", name = "Target", desc = "The category of tank it will target")
	public TargetType targetType = TargetType.enemies;

	/** Determines which type of AI the tank will use when shooting.
	 *  None means that the tank will not shoot
	 *  Sprinkler means the tank will just randomly shoot when it is able to
	 *  Straight means that the tank will shoot directly at the target enemy if the target enemy is in line of sight.
	 *  Reflect means that the tank will use a Ray with reflections to find possible ways to hit the target enemy.
	 *  Homing is similar to reflect but for tanks with homing bullets - fires if the bullet endpoint is in line of sight of the target.
	 *  Alternate means that the tank will switch between shooting straight at the target enemy and using the reflect AI with every shot.
	 *  Wander means that the tank will randomly rotate and shoot only if it detects the target enemy*/
	public enum ShootAI {none, sprinkler, wander, straight, homing, alternate, reflect}

	/** Type of shooting AI to use*/
	@TankProperty(category = firingBehavior, id = "shoot_ai_type", name = "Aiming behavior", desc = "Behavior for aiming and firing at targets", choiceDesc = {
			"None: do not shoot at all",
			"Sprinkler: rotate randomly and continuously shoot",
			"Wander: randomly rotate and shoot if target enemy falls in the trajectory",
			"Straight: shoot directly at the target, if in line of sight",
			"Reflect: use obstacles to calculate bounces",
			"Alternate: switch between straight and reflect with every shot",
			"Homing: like reflect, but recommended for homing bullets"
	})
	public ShootAI shootAIType;

	/** Larger values decrease accuracy but make the tank behavior more unpredictable*/
	@TankProperty(category = firingBehavior, id = "aim_accuracy_offset", name = "Inaccuracy", desc = "Random angle added to bullet trajectory upon shooting to make things more unpredictable")
	public double aimAccuracyOffset = 0.2;
	/** Threshold angle difference needed between angle and aimAngle to count as touching the target enemy*/
	public double aimThreshold = 0.05;

	/** Minimum time to randomly change idle direction, added to turretIdleTimerRandom * this.random.nextDouble()*/
	@TankProperty(category = firingBehavior, id = "turret_idle_timer_base", name = "Turret base timer", desc = "Minimum time the turret will idly rotate in one direction before changing direction")
	public double turretIdleTimerBase = 25;
	/** Random factor in calculating time to randomly change idle direction, multiplied by this.random.nextDouble() and added to turretIdleTimerBase*/
	@TankProperty(category = firingBehavior, id = "turret_idle_timer_random", name = "Turret random timer", desc = "A random percentage between 0% and 100% of this time value is added to the turret base rotation timer to get the time between changing idle rotation direction")
	public double turretIdleTimerRandom = 500;

	/** Speed at which the turret moves while idle*/
	@TankProperty(category = firingBehavior, id = "turret_idle_speed", name = "Idle turret speed", desc = "Speed the turret turns at when not actively aiming at a target")
	public double turretIdleSpeed = 0.005;
	/** Speed at which the turret moves while aiming at a target enemy*/
	@TankProperty(category = firingBehavior, id = "turret_aim_speed", name = "Aim turret speed", desc = "Speed the turret turns at when actively aiming toward a target")
	public double turretAimSpeed = 0.03;

	/** When set to true, will calculate target enemy velocity when shooting. Only effective when shootAIType is straight!*/
	@TankProperty(category = firingBehavior, id = "enable_predictive_firing", name = "Predictive", desc = "When enabled, will use the current velocity of the target to predict and fire towards its future position \n Only works with straight aiming behavior!")
	public boolean enablePredictiveFiring = true;
	/** The chance out of 1 that the tank will use predictive firing every shot */
	@TankProperty(category = firingBehavior, id = "predictive_firing_chance", name = "Predictive chance", desc = "The chance out of 1 that the tank will use predictive firing every shot")
	public double predictiveChance = 1;
	/** When set to true, will shoot at bullets aiming towards the tank*/
	@TankProperty(category = firingBehavior, id = "enable_defensive_firing", name = "Deflect bullets", desc = "When enabled, will shoot at incoming bullet threats to deflect them \n Does not work with wander or sprinkler aiming behavior!")
	public boolean enableDefensiveFiring = false;
	/** Will look through destructible walls when set to true for bullet shooting, recommended for explosive bullets*/
	@TankProperty(category = firingBehavior, id = "aim_ignore_destructible", name = "Through walls", desc = "When enabled, will shoot at destructible blocks if the target is hiding behind them. This is useful for tanks with explosive bullets.")
	public boolean aimIgnoreDestructible = false;

	/** Number of bullets in bullet fan*/
	@TankProperty(category = firingPattern, id = "shot_round_count", name = "Shots per round", desc = "Number of bullets to fire per round")
	public int shotRoundCount = 1;
	/** Time to fire a full fan*/
	@TankProperty(category = firingPattern, id = "shot_round_time", name = "Round time", desc = "Amount of time it takes to fire a full round of bullets")
	public double shootRoundTime = 60;
	/** Spread of a round*/
	@TankProperty(category = firingPattern, id = "shot_round_spread", name = "Round spread", desc = "Total angle of spread of a round")
	public double shotRoundSpread = 36;

	public static class SpawnedTankEntry
	{
		public TankAIControlled tank;
		public double weight;

		public SpawnedTankEntry(TankAIControlled t, double weight)
		{
			this.tank = t;
			this.weight = weight;
		}

		public String toString()
		{
			return this.weight + "x" + this.tank.toString();
		}
	}

	@TankProperty(category = spawning, id = "spawned_tanks", name = "Spawned tanks", desc = "Tanks which will be spawned by this tank as support", miscType = TankProperty.MiscType.spawnedTanks)
	public ArrayList<SpawnedTankEntry> spawnedTankEntries = new ArrayList<>();
	/** Tanks spawned on initial load*/
	@TankProperty(category = spawning, id = "spawned_initial_count", name = "Initial count", desc = "Number of tanks spawned immediately when this tank is created")
	public int spawnedInitialCount = 4;
	/** Max number of spawned tanks*/
	@TankProperty(category = spawning, id = "spawned_max_count", name = "Max count", desc = "Maximum number of spawned tanks from this tank that can be on the field at once")
	public int spawnedMaxCount = 6;
	/** Chance for this tank to spawn another tank*/
	@TankProperty(category = spawning, id = "spawn_chance", name = "Spawn chance", desc = "Chance for this tank to spawn another tank")
	public double spawnChance = 0.003;

	/** Whether the tank should commit suicide when there are no allied tanks on the field */
	@TankProperty(category = lastStand, id = "enable_suicide", name = "Last stand", desc = "When enabled and there are no allied tanks on the field, this tank will charge at the nearest enemy and explode")
	public boolean enableSuicide = false;
	/** Base factor in calculating suicide timer: base + random * Math.random()*/
	@TankProperty(category = lastStand, id = "suicide_timer_base", name = "Base timer", desc = "Minimum time this tank will charge at its enemy before blowing up")
	public double suicideTimerBase = 500;
	/** Random factor in calculating suicide timer: base + random * Math.random() */
	@TankProperty(category = lastStand, id = "suicide_timer_random", name = "Random timer", desc = "A random fraction of this value is added to the base timer to get the time this tank will charge before exploding")
	public double suicideTimerRandom = 250;
	/** Suicidal mode maximum speed increase*/
	@TankProperty(category = lastStand, id = "suicide_speed_boost", name = "Speed boost", desc = "Maximum increase in speed while charging as a last stand")
	public double suicideSpeedBoost = 3;

	/** Range which rays will be used to detect a tank after being locked on to it. Larger values detect motion better but are less accurate.*/
	public double searchRange = 0.3;

	public String shotSound = null;

	// The following are values which are internally used for carrying out behavior.
	// These values change constantly during the course of the game.

	protected double updateAge, updateFrequency;

	protected boolean shooting;

	/** Used for tanks which do not use the straight AI, when detecting the target enemy with a ray. Tells the tank to aim towards the found target angle.*/
	protected boolean aim = false;

	/** True for when a tank just laid a mine*/
	protected boolean laidMine = false;

	protected boolean shotMine = false;

	/** Whether the tank is using predictive firing */
	protected boolean predictiveFiring = true;

	/** Alternates for tanks with the alternate AI. Tells tanks to shoot with reflection and then to shoot straight.*/
	protected boolean straightShoot = false;

	/** If a direct line of sight to the target enemy exists, set to true*/
	protected boolean seesTargetEnemy = false;

	/** Stores distances to obstacles or tanks in 8 directions*/
	protected double[] distances = new double[8];

	/** Stores distances to obstacles or tanks in 32 directions*/
	protected double[] fleeDistances = new double[32];

	/** Stores directions a tank may flee from a bullet, relative to that bullet's direction */
	protected double[] fleeDirections = new double[fleeDistances.length];

	/** Cooldown before the tank will turn again if it's running into a wall */
	protected double gentleTurnCooldown = 0;

	/** Time in which the tank will follow its initial flee path from a mine*/
	protected double mineFleeTimer = 0;

	/** Used only in non-straight AI tanks. When detecting the target enemy, set to the angle necessary to hit them. This angle is added to random offsets to search for the target enemy moving.*/
	protected double lockedAngle = 0;

	/** Used only in non-straight AI tanks. Angle at which the tank is searching with its aim ray for the target enemy*/
	protected double searchAngle = 0;

	/** Angle at which the tank aims after having found its target (if non-straight AI, found with a ray, otherwise just the angle to the tank)*/
	protected double aimAngle = 0;

	/** Distance to target; only used for arc bullet shooting tanks*/
	protected double distance = 0;

	/** Direction in which the tank moves when idle*/
	protected double direction;

	/** When enabled, the current motion direction will be kept until the tank decides to change direction*/
	protected boolean overrideDirection = false;

	/** Direction in which the tank moves to avoid a bullet that will hit it*/
	protected double avoidDirection = 0;

	/** Time until the tank will change its idle turret's direction*/
	protected double idleTimer;

	/** Time between shooting bullets*/
	protected double cooldown = 250;

	/** Inaccuracy of next shot*/
	protected double shotOffset = 0;

	/** Time until the next mine will be laid*/
	protected double mineTimer = -1;

	/** Time which the tank will aim at its lockedAngle until giving up and continuing to search*/
	protected double aimTimer = 0;

	/** Time the tank will continue to avoid a bullet*/
	protected double avoidTimer = 0;

	/** Time until reverting transformation */
	protected double transformRevertTimer = 0;

	/**
	 * Time until transforming
	 */
	protected double transformTimer = 0;

	/** Set if the tank will eventually turn back into the original tank it was */
	protected boolean willRevertTransformation = true;

	/** A fraction of the base idle speed */
	protected double idleSpeed;

	/** Nearest bullet aiming at this tank, if avoid timer is > than 0*/
	protected Bullet nearestBullet;

	/** Time until the nearest threat bullet will strike */
	protected double nearestBulletDist;

	/** Nearest deflectable bullet aiming at this tank, if avoid timer is > than 0*/
	protected Bullet nearestBulletDeflect;

	/** Time until the nearest deflectable threat bullet will strike */
	protected double nearestBulletDeflectDist;

	protected ArrayList<Bullet> toAvoid = new ArrayList<>();
	protected ArrayList<Double> toAvoidDist = new ArrayList<>();
	protected ArrayList<Bullet> toAvoidDeflect = new ArrayList<>();
	protected ArrayList<Ray> toAvoidTargets = new ArrayList<>();
	protected boolean avoid = false;

	/** Number of bullet threats that will hit this tank */
	protected int bulletThreatCount;

	/** Disable offset to shoot a bullet*/
	protected boolean disableOffset = false;

	/** Direction added to the bullet's direction to flee a bullet, possibly mirrored*/
	protected double fleeDirection = Math.PI / 4;

	/** Phase the tank is searching in, not used for straight AI*/
	protected RotationPhase searchPhase = RotationPhase.clockwise;

	/** Phase the tank turret is idling in, not used for straight AI*/
	protected RotationPhase idlePhase = RotationPhase.clockwise;

	/** Time until the tank will continue motion*/
	protected double motionPauseTimer = 0;

	/** Changes when the tank's visibility state changes, indicating whether the tank is visible on screen*/
	public boolean currentlyVisible = true;

	/** Time this tank has been invisible for*/
	public double timeInvisible = 0;

	/** Normally the nearest tank not on this tank's team. This is the tank that this tank will fight.*/
	protected Movable targetEnemy;

	/** True if can find an enemy*/
	protected boolean hasTarget = true;

	/** If true, charges towards the nearest enemy and explodes */
	public boolean suicidal = false;

	/** Direction to strafe around target enemy, if set to strafe mode on sight*/
	protected double strafeDirection = Math.PI * 0.45;

	/** True while the tank is actively seeking out an enemy*/
	protected boolean currentlySeeking = false;

	/** If true, tank will pathfind on the first frame possible */
	protected boolean instantSeek = false;

	/** Set to a value to temporarily pause the tank from seeking*/
	protected double seekPause = 0;

	/** Upon reaching zero, the current target path is abandoned*/
	protected double seekTimer = 0;

	/** Describes the path the tank is currently following*/
	protected LinkedList<Tile> path;

	/* Accelerations */
	protected double aX;
	protected double aY;

	/** Tanks that this tank has spawned */
	protected ArrayList<Tank> spawnedTanks = new ArrayList<>();

	/** Time until the tank will commit suicide */
	public double timeUntilDeath;

	/** The random number generator the tank uses to make decisions*/
	protected Random random;

	/** Progress of a shooting fan for tanks firing multiple bullets per round*/
	protected double shootTimer = 0;

	/** Number of shots fired in the current round*/
	protected int shots = 0;

	/** Whether shooting in a fan */
	protected boolean shootingInFan = false;

	/** 1 or -1, indicating direction of fan being fired*/
	protected int fanDirection;

	/** True if the tank charged up this frame*/
	protected boolean justCharged = false;

	/** Used to calculate cooldown when it goes down for each shot (when cooldownSpeedup is not zero)*/
	protected int cooldownStacks = 0;

	/** Time passed since we last had a target ready to shoot at, used to reset cooldown stacks*/
	protected double cooldownIdleTime = 0;

	/** Fan round inaccuracy*/
	protected double fanOffset;

	/** Time until mimicking ends */
	protected double mimicRevertCounter = this.mimicRevertTime;

	/** Mimic target tank */
	protected Movable mimicTarget;

	/** Mimic laser effect*/
	protected Laser laser;

	/** Tank this tank is transformed into*/
	protected TankAIControlled transformTank = null;

	/** True if able to mimic other tanks*/
	protected boolean canCurrentlyMimic = true;

	protected double baseColorR;
	protected double baseColorG;
	protected double baseColorB;
	protected double baseMaxSpeed;

	protected double spaceFrac = 0;

	/** Set if tank transformed in the last frame */
	public boolean justTransformed = false;

	protected double lastCooldown = this.cooldown;

	public TankAIControlled(String name, double x, double y, double size, double r, double g, double b, double angle, ShootAI ai)
	{
		super(name, x, y, size, r, g, b);

		this.random = new Random(Level.random.nextLong());
		this.direction = ((int)(this.random.nextDouble() * 8)) / 2.0;

		if (this.random.nextDouble() < 0.5)
			this.idlePhase = RotationPhase.counter_clockwise;

		this.angle = angle;
		this.orientation = angle;

		this.bullet.maxLiveBullets = 5;
		this.bullet.recoil = 0;
		this.bullet.cooldownBase = 1;

		this.shootAIType = ai;

		for (int i = 0; i < fleeDirections.length; i++)
            fleeDirections[i] = Math.PI / 4 + (i * 2 / fleeDirections.length) * Math.PI / 2 + i * Math.PI / fleeDirections.length;

		this.propsCloned = true;
	}

	protected TankAIControlled()
	{
		this(null, 0, 0, 0, 0, 0, 0, 0, ShootAI.none);
	}

	public void updateVisibility()
	{
		if (this.invisible)
		{
			if (this.currentlyVisible)
			{
				this.currentlyVisible = false;
				Drawing.drawing.playGameSound("transform.ogg", this, Game.tile_size * 25, 1.2f);
				Game.eventsOut.add(new EventTankUpdateVisibility(this.networkID, false));

				if (Game.effectsEnabled)
				{
					for (int i = 0; i < 50 * Game.effectMultiplier; i++)
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.piece);
						double var = 50;
						e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);

						Game.effects.add(e);
					}
				}
			}

			this.timeInvisible += updateFrequency;
		}
		else
			this.timeInvisible = 0;
	}

	@Override
	public void update()
	{
		if (this.age <= 0)
		{
			if (Game.grandpaMode)
			{
				this.bulletAvoidBehvavior = BulletAvoidBehavior.aggressive_dodge;
				this.targetEnemySightBehavior = TargetEnemySightBehavior.sidewind;
			}

			this.transformTimer = this.transformTimerBase;
			this.baseMaxSpeed = this.maxSpeed;
			this.dealsDamage = !this.isSupportTank();
			this.baseColorR = this.colorR;
			this.baseColorG = this.colorG;
			this.baseColorB = this.colorB;
			this.idleTimer = (this.random.nextDouble() * turretIdleTimerRandom) + turretIdleTimerBase;
			this.predictiveFiring = this.enablePredictiveFiring;

			if (this.targetEnemySightBehavior == TargetEnemySightBehavior.sidewind)
				this.strafeDirection /= 2;
			else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.backwind)
				this.strafeDirection *= 1.5;

			if (this.random.nextDouble() < 0.5)
				this.strafeDirection = -this.strafeDirection;

			this.spawnedTankEntries.removeIf(entry -> !entry.tank.solved());
		}

		Tank.updatesPerFrame++;
		this.shooting = false;
		this.updateFrequency = Panel.frameFrequency;
		this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
		this.justTransformed = false;

		if (!this.spawnedTankEntries.isEmpty() && !ScreenGame.finishedQuick && !this.destroy)
			this.updateSpawningAI();

		if (!this.destroy)
		{
			if ((Panel.panel.ageFrames + this.networkID) % 5 == 0)
                this.updateTarget();

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
					this.recoilSpeed *= Math.pow(1 - this.friction * this.frictionModifier, updateFrequency);
				}
			}
			else if (this.inControlOfMotion)
			{
				this.vX *= Math.pow(1 - (this.friction * this.frictionModifier), updateFrequency);
				this.vY *= Math.pow(1 - (this.friction * this.frictionModifier), updateFrequency);

				if (this.enableMovement)
					this.updateMotionAI();
				else
				{
					this.vX *= Math.pow(1 - (0.15 * this.frictionModifier), updateFrequency);
					this.vY *= Math.pow(1 - (0.15 * this.frictionModifier), updateFrequency);

					if (this.enableDefensiveFiring)
						this.checkForBulletThreats();
				}
			}

			if (!ScreenGame.finished)
			{
				this.updateTurretAI();
				this.updateMineAI();
			}

			if (this.enableSuicide)
				this.updateSuicideAI();

			if (this.chargeUp)
				this.checkCharge();

			if (this.transformMimic)
				this.updateMimic();

			if (this.healthTransformTank != null && this.health <= this.transformHealthThreshold && !ScreenGame.finishedQuick)
				this.handleHealthTransformation();

			if (this.timeTransformTank != null)
				this.handleTimeTransformation();

			this.postUpdate();
		}

		if (!this.tookRecoil)
		{
			this.vX += this.aX * maxSpeed * updateFrequency * this.accelerationModifier;
			this.vY += this.aY * maxSpeed * updateFrequency * this.accelerationModifier;

			double currentSpeed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

			if (currentSpeed > maxSpeed * maxSpeedModifier)
				this.setPolarMotion(this.getPolarDirection(), maxSpeed * maxSpeedModifier);
		}

		double rate = updateFrequency / Panel.frameFrequency;
		this.bullet.updateCooldown(rate);
		this.mine.updateCooldown(rate);
		this.updateVisibility();
		super.update();
		updateAge += updateFrequency;
	}

	/** Prepare to fire a bullet*/
	public void shoot()
	{
		if (this.suicidal || this.destroy)
			return;

		this.cooldownIdleTime = 0;
		this.aimTimer = 10;

		if (!chargeUp)
			this.aim = false;

		boolean arc = BulletArc.class.isAssignableFrom(this.bullet.bulletClass);

		if (this.canShoot() && (this.bullet.maxLiveBullets <= 0 || this.bullet.liveBullets < this.bullet.maxLiveBullets))
		{
			if (this.cooldown <= 0)
			{
				this.aim = false;
				double range = this.bullet.getRange();

				if (arc && this.distance <= range)
				{
					if (this.shotRoundCount <= 1)
						this.bullet.attemptUse(this);
					else
					{
						this.shootingInFan = true;
						this.shootTimer = -this.shootRoundTime / 2;
						this.shots = 0;
						this.fanDirection = this.random.nextDouble() < 0.5 ? 1 : -1;
						this.fanOffset = (this.random.nextDouble() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2)) / Math.max((Movable.distanceBetween(this, this.targetEnemy) / 1000.0), 2);
					}
				}
				else if (!arc)
				{
					boolean inRange = (range < 0) || (Movable.distanceBetween(this, this.targetEnemy) <= range);
					if (!inRange)
						return;

					double an = this.angle;

					if (this.targetEnemy != null && this.predictiveFiring && this.shootAIType == ShootAI.straight)
						an = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

					Ray a2 = new Ray(this.posX, this.posY, an, this.bullet.bounces, this);
					a2.size = this.bullet.size;
					a2.getTarget();
					a2.ignoreDestructible = this.aimIgnoreDestructible;
					a2.ignoreShootThrough = true;

					double dist = a2.age;
					// Cancels if the bullet will hit another enemy
					double offset = (this.random.nextDouble() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2)) / Math.max((dist / 100.0), 2);

					if (this.disableOffset || this.targetEnemy instanceof Mine)
					{
						offset = 0;
						this.disableOffset = false;
					}

					if (this.shotRoundCount <= 1)
						this.finalCheckAndShoot(offset);
					else
						this.finalCheckAndShootFan(offset);
				}
			}
			else if (this.chargeUp)
			{
				this.charge();
			}
		}
	}

	public void charge()
	{
		double reload = this.getAttributeValue(AttributeModifier.reload, 1);

		this.cooldown -= updateFrequency * reload;
		this.justCharged = true;

		double frac = this.cooldown / this.lastCooldown;
		this.colorR = ((this.baseColorR + 255) / 2) * (1 - frac) + frac * this.baseColorR;
		this.colorG = ((this.baseColorG + 255) / 2) * (1 - frac) + frac * this.baseColorG;
		this.colorB = ((this.baseColorB + 255) / 2) * (1 - frac) + frac * this.baseColorB;

		if (Tank.shouldUpdate)
		{
			Game.eventsOut.add(new EventTankUpdateColor(this));
			Game.eventsOut.add(new EventTankCharge(this.networkID, frac));
		}

		if (Math.random() * this.lastCooldown * Game.effectMultiplier > cooldown && Game.effectsEnabled)
		{
			Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.charge);

			double var = 50;
			e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
			e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
			e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

			Game.effects.add(e);
		}
	}

	public void checkCharge()
	{
		if (!this.justCharged)
		{
			this.cooldown = Math.pow(1 - this.cooldownSpeedup, this.cooldownStacks) * (this.random.nextDouble() * this.cooldownRandom + this.cooldownBase);
			this.lastCooldown = this.cooldown;

			this.colorR = this.baseColorR;
			this.colorG = this.baseColorG;
			this.colorB = this.baseColorB;
			Game.eventsOut.add(new EventTankUpdateColor(this));
		}

		this.justCharged = false;
	}

	public void finalCheckAndShoot(double offset)
	{
		Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.bullet.bounces, this, 2.5)
				.setSize(this.bullet.size).moveOut(this.size / 2.5);

		Movable m = a.getTarget();

		if (m != this && (this.isSupportTank() || ((!Team.isAllied(this, m) || m instanceof Mine) && this.isTargetSafe(a.posX, a.posY, m))))
		{
			this.shooting = true;
			this.shotOffset = offset;
			this.bullet.attemptUse(this);
		}

		if (this.targetEnemy instanceof Mine)
			this.targetEnemy = null;
	}

	public void finalCheckAndShootFan(double offset)
	{
		boolean cancel = false;
		for (int i = 0; i < this.shotRoundCount; i++)
		{
			double offset2 = (i - ((this.shotRoundCount - 1) / 2.0)) / this.shotRoundCount * (this.shotRoundSpread * Math.PI / 180);

			Ray a = new Ray(this.posX, this.posY, this.angle + offset + offset2, this.bullet.bounces, this, 2.5)
					.setSize(this.bullet.size).moveOut(this.size / 2.5);

			Movable m = a.getTarget();

			if (Team.isAllied(this, m))
			{
				cancel = true;
				break;
			}
		}

		if (!cancel)
		{
			this.shootingInFan = true;
			this.shootTimer = -this.shootRoundTime / 2;
			this.shots = 0;
			this.fanDirection = this.random.nextDouble() < 0.5 ? 1 : -1;
			this.fanOffset = offset;
		}

		if (this.targetEnemy instanceof Mine)
		{
			this.targetEnemy = null;
			this.shotMine = true;
		}
	}

	public boolean isTargetSafe(double x, double y, Movable m)
	{
		double size = -99;
		if (BulletExplosive.class.isAssignableFrom(this.bullet.bulletClass))
			size = Mine.mine_size;
		else if (m instanceof IAvoidObject)
			size = ((IAvoidObject) m).getRadius();

		boolean mine = m instanceof Mine;

		if (size > 0)
		{
			for (Movable m2 : Game.movables)
			{
				if (mine && m2 instanceof TankAIControlled t && m2 != this && Team.isAllied(this, t) && t.targetEnemy == m && this.random.nextDouble() < 0.2)
					return false;

				if (Team.isAllied(m2, this) && m2 instanceof Tank t && !t.resistExplosions && this.team != null && this.team.friendlyFire && Math.pow(m2.posX - x, 2) + Math.pow(m2.posY - y, 2) <= size * size)
					return false;
			}
		}

		return true;
	}

	/** Actually fire a bullet*/
	public void fireBullet(Bullet b, double speed, double offset)
	{
		if (b.itemSound != null)
			Drawing.drawing.playGameSound(b.itemSound, this, b.soundRange, (float) ((Bullet.bullet_size / this.bullet.size) * (1 - (Math.random() * 0.5) * b.pitchVariation)));

		if (this.shotSound != null)
			Drawing.drawing.playGameSound(this.shotSound, this, b.soundRange, (float) ((Bullet.bullet_size / this.bullet.size) * (1 - (Math.random() * 0.5) * b.pitchVariation)));

		b.setPolarMotion(angle + offset + this.shotOffset, speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil * this.getAttributeValue(AttributeModifier.recoil, 1) * b.frameDamageMultipler);
		b.speed = speed;

		if (b instanceof BulletArc)
			b.vZ = this.distance / speed * 0.5 * BulletArc.gravity;
		else
			b.moveOut(50 * this.size / Game.tile_size * this.turretLength / Game.tile_size);

		Game.movables.add(b);
		Game.eventsOut.add(new EventShootBullet(b));

		int r = (this.enableDefensiveFiring && this.avoidTimer > 0 && this.disableOffset && this.bulletThreatCount > 1) ? 0 : 1;

		this.cooldown = Math.pow(1 - this.cooldownSpeedup, this.cooldownStacks) * (r * this.random.nextDouble() * this.cooldownRandom + this.cooldownBase);
		this.lastCooldown = this.cooldown;
		this.bullet.cooldown = Math.max(this.bullet.cooldown, Math.min(this.cooldown - 1, 5));
		this.cooldownStacks++;

		if (this.shootAIType.equals(ShootAI.alternate))
			this.straightShoot = !this.straightShoot;

		if (this.enablePredictiveFiring && this.predictiveChance < 1)
			this.predictiveFiring = this.random.nextDouble() < this.predictiveChance;
	}

	public void updateTarget()
	{
		if ((this.shootAIType == ShootAI.none || this.shootAIType == ShootAI.sprinkler || this.shootAIType == ShootAI.wander)
				&& !this.enableMovement && !this.transformMimic && this.sightTransformTank == null)
			return;

		double nearestDist = Double.MAX_VALUE;
		Movable nearest = null;
		Movable nearestVisible = null;
		Movable secondary = null;
		this.hasTarget = false;

		if (transformMimic && updateTargetMimic())
			return;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);
			if (m.destroy)
				continue;

			boolean correctTeam = isSupportTank() ? Team.isAllied(this, m) && m instanceof Tank t && t.canBeHealed() : !Team.isAllied(this, m);
			if ((m instanceof Tank t && correctTeam && !t.hidden && t.targetable && m != this) ||
					(m instanceof Mine && !BulletAir.class.isAssignableFrom(this.bullet.bulletClass) && !this.isSupportTank() && isTargetSafe(m.posX, m.posY, m)))
			{
				double dist = Movable.distanceBetween(this, m);
				if (dist < nearestDist)
				{
					boolean reachable = BulletArc.class.isAssignableFrom(this.bullet.bulletClass) ||
							(Movable.withinRange(this, m, maxReachableCheckDist) &&
									new Ray(this.posX, this.posY, this.getAngleInDirection(m.posX, m.posY), this.bullet.bounces, this)
											.setMaxChunks(maxReachableCheckChunks).getTarget() == m);
					this.hasTarget = true;

					if (m instanceof Tank)
					{
						nearest = m;
						nearestDist = dist;

						if (reachable)
							nearestVisible = m;
					}
					else if (reachable)
                        secondary = m;
				}
			}
		}

		if (nearestVisible != null)
            nearest = nearestVisible;
		else if (secondary != null)
			nearest = secondary;

		if (this.targetEnemy != nearest)
			this.cooldownStacks = 0;

		this.targetEnemy = nearest;
	}

	public boolean updateTargetMimic()
	{
		double nearestDist = Double.MAX_VALUE;
		Movable nearest = null;
		this.hasTarget = false;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof Tank t && !(m instanceof TankAIControlled t1 && t1.transformMimic) && (t.getTopLevelPossessor() == null || !(t.getTopLevelPossessor().getClass().equals(this.getClass())))
					&& !t.hidden && t.targetable && Movable.distanceBetween(m, this) < this.mimicRange && m.size == this.size && !m.destroy)
			{
				if (new Ray(this.posX, this.posY, this.getAngleInDirection(m.posX, m.posY), 0, this)
						.setMaxDistance(mimicRange).getTarget() != m)
					continue;

				double distance = Movable.distanceBetween(this, m);

				if (distance < nearestDist)
				{
					this.hasTarget = true;
					nearestDist = distance;
					nearest = m;
				}
			}
		}

		this.targetEnemy = nearest;
		this.canCurrentlyMimic = this.targetEnemy != null;

		return this.targetEnemy != null;
	}

	public void updateMotionAI()
	{
		if (this.enableBulletAvoidance || this.enableDefensiveFiring)
			this.checkForBulletThreats();

		if (this.avoidTimer > 0 && this.enableBulletAvoidance)
		{
			this.avoidTimer -= updateFrequency;
			this.setPolarAcceleration(avoidDirection, acceleration * 2);
			this.overrideDirection = true;
		}
		else
		{
//			setIQ();

			fleeDirection = -fleeDirection;

			if (this.targetEnemy != null && this.seesTargetEnemy && this.enableTargetEnemyReaction && this.enableLookingAtTargetEnemy)
			{
				if (this.currentlySeeking)
				{
					this.seekTimer -= updateFrequency;
					this.followPath();

					if (this.seekTimer <= 0)
						this.currentlySeeking = false;
				}
				else
					this.reactToTargetEnemySight();
			}
			else if (currentlySeeking && seekPause <= 0)
				this.followPath();
			else
				this.updateIdleMotion();
		}
	}

	public void reactToTargetEnemySight()
	{
		if (this.targetEnemy == null)
			return;

		this.overrideDirection = true;

		if (this.stopSeekingOnSight)
			this.currentlySeeking = false;

		if (this.suicidal || this.targetEnemySightBehavior == TargetEnemySightBehavior.approach)
			this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.flee)
			this.setAccelerationAwayFromDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.strafe || this.targetEnemySightBehavior == TargetEnemySightBehavior.sidewind || this.targetEnemySightBehavior == TargetEnemySightBehavior.backwind)
		{
			if (this.random.nextDouble() < this.strafeDirectionChangeChance * updateFrequency)
                strafeDirection = -strafeDirection;

			this.setAccelerationInDirectionWithOffset(this.targetEnemy.posX, this.targetEnemy.posY, this.acceleration * 2, strafeDirection);
		}
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.keep_distance)
		{
			if (Movable.distanceBetween(this, this.targetEnemy) < this.targetSightDistance)
				this.setAccelerationAwayFromDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
			else
				this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		}
	}

	public void handleSightTransformation()
	{
		if (this.justTransformed)
			return;

		this.transformRevertTimer = this.sightTransformRevertTime;
		this.willRevertTransformation = true;
		this.transform(this.sightTransformTank);
		Drawing.drawing.playGameSound("timer.ogg", this, Game.tile_size * 75,1.25f);
		Effect e1 = Effect.createNewEffect(this.posX, this.posY, this.posZ + this.sightTransformTank.size * 0.75, Effect.EffectType.exclamation);
		e1.size = this.sightTransformTank.size;
		e1.colR = this.colorR;
		e1.colG = this.colorG;
		e1.colB = this.colorB;
		e1.glowR = this.sightTransformTank.colorR;
		e1.glowG = this.sightTransformTank.colorG;
		e1.glowB = this.sightTransformTank.colorB;
		Game.effects.add(e1);
		Game.eventsOut.add(new EventTankTransformPreset(this, true, false));
	}

	public void handleHealthTransformation()
	{
		if (this.justTransformed)
			return;

		this.willRevertTransformation = false;
		Game.eventsOut.add(new EventTankTransformPreset(this, false, false));
		this.transform(this.healthTransformTank);
	}

	public void handleTimeTransformation()
	{
		this.transformTimer -= Panel.frameFrequency;
		if (this.transformTimer >= 0 || this.justTransformed)
			return;

		this.willRevertTransformation = false;
		this.transformTimer = this.transformTimerBase;
		Game.eventsOut.add(new EventTankTransformPreset(this, false, false));
		this.transform(this.timeTransformTank);
    }

	public void transform(TankAIControlled t)
	{
		t.solve();
		if (!t.propsCloned)
			return;

		this.justTransformed = true;
		this.transformTank = t;
		this.possessingTank = t;
		t.posX = this.posX;
		t.posY = this.posY;
		t.vX = this.vX;
		t.vY = this.vY;
		t.angle = this.angle;
		t.pitch = this.pitch;
		t.team = this.team;
		t.health = this.health;
		t.orientation = this.orientation;
		t.drawAge = this.drawAge;
		t.possessor = this;
		t.skipNextUpdate = true;
		t.attributes = this.attributes;
		t.statusEffects = this.statusEffects;
		t.coinValue = this.coinValue;
		t.currentlyVisible = true;
		t.cooldown = this.cooldown;
		t.age = 0;

		Tank p = this;
		if (this.getTopLevelPossessor() != null)
			p = this.getTopLevelPossessor();

		if (p instanceof TankAIControlled && ((TankAIControlled) p).transformMimic)
		{
			t.baseModel = this.baseModel;
			t.turretModel = this.turretModel;
			t.turretBaseModel = this.turretBaseModel;
		}

		t.crusadeID = this.crusadeID;

		t.setNetworkID(this.networkID);

		Game.movables.add(t);
		Game.removeMovables.add(this);
	}

	public void updateIdleMotion()
	{
		double space = 1000;

		if (!this.overrideDirection && this.gentleTurnCooldown <= 0)
		{
			Ray d = new Ray(this.posX, this.posY, this.getPolarDirection(), 0, this, Game.tile_size);
			d.size = Game.tile_size * this.hitboxSize - 1;

			space = d.getDist();
		}

		if (this.gentleTurnCooldown > 0)
			this.gentleTurnCooldown -= updateFrequency;

		boolean turn = this.random.nextDouble() < this.turnChance * updateFrequency || this.hasCollided;

        if (turn || space <= 50)
        {
			if (this.targetEnemy == null || Movable.distanceBetween(this.targetEnemy, this) > Game.tile_size * 5)
            	this.idleSpeed = this.random.nextDouble() / 2;
			else
				this.idleSpeed = 1;

			this.overrideDirection = false;

			ArrayList<Double> directions = new ArrayList<>();

            boolean[] validDirs = new boolean[8];
            validDirs[(int) (2 * ((this.direction + 0) % 4))] = true;
            validDirs[(int) (2 * ((this.direction + 0.5) % 4))] = true;
            validDirs[(int) (2 * ((this.direction + 3.5) % 4))] = true;
            validDirs[(int) (2 * ((this.direction + 1) % 4))] = true;
            validDirs[(int) (2 * ((this.direction + 3) % 4))] = true;

            if (!turn)
                this.gentleTurnCooldown = 50;

            for (double dir = 0; dir < 4; dir += 0.5)
            {
                Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tile_size);
                r.size = Game.tile_size * this.hitboxSize - 1;
                double dist = r.getDist() / Game.tile_size;

                distances[(int) (dir * 2)] = dist;

                if (validDirs[(int) (dir * 2)])
                {
                    if (dist >= 4)
                        directions.add(dir);
                }
            }

            int chosenDir = (int)(this.random.nextDouble() * directions.size());

            if (directions.isEmpty())
                this.direction = (this.direction + 2) % 4;
            else
                this.direction = directions.get(chosenDir);

            if (turn)	// i changed this for bowling
                this.motionPauseTimer = this.turnPauseTime;

            if (this.canHide && turn)
                this.motionPauseTimer += this.bushHideTime * (this.random.nextDouble() + 1);
        }

		if (this.motionPauseTimer > 0)
		{
			this.aX = 0;
			this.aY = 0;
			this.motionPauseTimer = (Math.max(0, this.motionPauseTimer - updateFrequency));
        }
        else
        {
            if (!this.overrideDirection)
            {
                this.setPolarAcceleration(this.direction / 2 * Math.PI, acceleration * idleSpeed);
				this.addIdleMotionOffset();
			}
		}

		if (!this.currentlySeeking && this.enablePathfinding && (this.random.nextDouble() < this.seekChance * updateFrequency || this.instantSeek) && this.posX > 0 && this.posX < Game.currentSizeX * Game.tile_size && this.posY > 0 && this.posY < Game.currentSizeY * Game.tile_size)
			findPath();

		if (this.seekPause > 0)
			this.seekPause -= updateFrequency;
		
		if (this.seekPause < 0)
		{
			this.seekPause = 0;

			if (!this.path.isEmpty())
			{
				Tile t = this.path.getFirst();
				findPath(t.tileX, t.tileY);
				this.instantSeek = true;
			}
		}

		if (this.parent != null && !seesTargetEnemy && this.stayNearParent)
		{
			if (!this.parent.destroy && Math.sqrt(Math.pow(this.posX - this.parent.posX, 2) + Math.pow(this.posY - this.parent.posY, 2)) > this.maxDistanceFromParent)
            {
                this.overrideDirection = true;
                this.setAccelerationInDirection(this.parent.posX, this.parent.posY, this.acceleration * idleSpeed);
			}
		}
	}

	public void findPath()
	{
		this.instantSeek = false;
		ArrayList<Tank> targets = new ArrayList<>();

		for (Movable m : Game.movables)
		{
			if (this.isInterestingPathTarget(m))
				targets.add((Tank) m);
		}

		if (targets.isEmpty())
			return;

		Tank target = targets.get((int) (Math.random() * targets.size()));

		int endX = (int) (target.posX / Game.tile_size);
		int endY = (int) (target.posY / Game.tile_size);
		findPath(endX, endY);
	}

	public void findPath(int endX, int endY)
	{
		int currX = (int) (this.posX / Game.tile_size);
		int currY = (int) (this.posY / Game.tile_size);

		if (currX < 0 || currX >= Game.currentSizeX || currY < 0 || currY >= Game.currentSizeY)
			return;

		ArrayDeque<Tile> queue = new ArrayDeque<>();
        boolean[][] visited = new boolean[Game.currentSizeX][Game.currentSizeY];

		queue.add(new Tile(currX, currY, null, this));
		visited[currX][currY] = true;

		Tile endingTile = null;

		while (!queue.isEmpty())
		{
			Tile t = queue.remove();

			if (t.unfavorability > 0)
			{
				t.unfavorability--;
				queue.add(t);
				continue;
			}

			if (t.tileX == endX && t.tileY == endY)
			{
				endingTile = t;
				break;
			}

			for (int i = 0; i < 4; i++)
			{
				int x = t.tileX + dirX[i];
				int y = t.tileY + dirY[i];

				if (x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY)
                    continue;

                if (visited[x][y] || t.type == Tile.Type.solid || t.unfavorability >= 75 || (t.type == Tile.Type.destructible && !this.enableMineLaying))
					continue;

				visited[x][y] = true;
				queue.add(new Tile(x, y, t, this));
			}
		}

		if (endingTile != null)
		{
			this.path = new LinkedList<>();
			this.currentlySeeking = true;
			this.seekTimer = this.seekTimerBase;

			while (endingTile.parent != null)
			{
				endingTile = endingTile.parent;
				endingTile.shiftedX = endingTile.shiftSides(this.random, true);
				endingTile.shiftedY = endingTile.shiftSides(this.random, false);
				this.path.addFirst(endingTile);
			}
		}
	}

	public void followPath()
	{
		this.seekTimer -= updateFrequency;

		if (Game.showPathfinding && !ScreenGame.finishedQuick)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);
			for (Tile t : this.path)
			{
				Game.effects.add(Effect.createNewEffect(t.shiftedX, t.shiftedY, 25, Effect.EffectType.laser));
				Drawing.drawing.drawText(t.posX, t.posY, 50, t.surrounded + "");
			}
		}

		if (this.path.isEmpty())
		{
			currentlySeeking = false;
			return;
		}

		Tile t = this.path.getFirst();

		//double frac = Math.max(Math.min(1, (seekTimerBase - seekTimer) / seekTurnBase), 0);

        //double pvX = this.vX;
        //double pvY = this.vY;

        this.setAccelerationInDirection(t.shiftedX, t.shiftedY, this.acceleration * this.accelerationModifier);
		//this.vX = this.vX * frac + pvX * (1 - frac);
		//this.vY = this.vY * frac + pvY * (1 - frac);

		double mul = 1;

		if (!this.path.isEmpty() && this.path.getFirst().type == Tile.Type.destructible)
			mul = 3;
		else if (this.path.size() > 1 && this.path.get(1).type == Tile.Type.destructible)
			mul = 2;

		if (Math.pow(t.shiftedX - this.posX, 2) + Math.pow(t.shiftedY - this.posY, 2) <= Math.pow(Game.tile_size / 2 * mul, 2))
		{
			this.seekTimer = this.seekTimerBase;

			if (this.path.getFirst().type == Tile.Type.destructible)
			{
				this.mine.attemptUse(this);
				this.seekTimer = this.seekTimerBase * 2;
				this.seekPause += this.mine.radius / this.maxSpeed + this.mine.radius / this.bullet.speed + 150;
			}

			this.path.removeFirst();
		}

		if (this.seekTimer < 0)
            this.currentlySeeking = false;
    }

	public void addIdleMotionOffset()
	{
		double offsetMotion = Math.sin(this.age * 0.02);
		double dist;

		if (offsetMotion < 0)
			dist = this.distances[(int) (this.direction * 2 + 6) % 8];
		else
			dist = this.distances[(int) (this.direction * 2 + 2) % 8];

		offsetMotion *= Math.min(1, (dist - 1) / 5.0) * this.acceleration;

		this.addPolarAcceleration((this.direction + 1) / 2 * Math.PI, offsetMotion);
	}

	public void checkForBulletThreats()
	{
		if ((Panel.panel.ageFrames + this.networkID) % 3 == 0)
		{
			avoid = false;
			toAvoid.clear();
			toAvoidDist.clear();
			toAvoidDeflect.clear();
			toAvoidTargets.clear();

			findDangersLoop:
			for (Chunk chunk : Chunk.iterateOutwards(posX, posY, 3))
			{
			/*Game.effects.add(Effect.createNewEffect(
					Chunk.chunkToPixel(chunk.chunkX + 0.5),
					Chunk.chunkToPixel(chunk.chunkY + 0.5),
					60, Effect.EffectType.laser
			));*/

				for (Movable m : chunk.movables)
				{
					if (!(m instanceof Bullet b && !b.destroy))
						continue;

					double dist = Movable.distanceBetween(this, b);

					if (dist > Game.tile_size * 50)
						continue;

					if (!shouldDodge(b, dist))
						continue;

					int c = enableMovement ? 1 : 0;
					for (int o = 0; o <= c; o++)
					{
						int mul = o == 1 ? 3 : 1;

						if (dist < this.size * mul)
						{
							avoid = true;

							if (o == 1)
							{
								toAvoid.add(b);
								toAvoidDist.add(dist);
								toAvoidTargets.add(b.getRay());
							}
							else
								toAvoidDeflect.add(b);
						}
						else
						{
							Ray r = b.getRay();
							r.tankHitSizeMul = 3;
							double d = r.getSquaredTargetDist(mul, this);

							if (d >= 0)
							{
								avoid = true;

								if (o == 1)
								{
									toAvoid.add(b);
									toAvoidDist.add(d);
									toAvoidTargets.add(r);
								}
								else
									toAvoidDeflect.add(b);
							}
						}
					}

					if (toAvoid.size() > 8)
						break findDangersLoop;
				}
			}

			this.bulletThreatCount = toAvoid.size();
		}

		if (!avoid)
			return;

		Bullet nearest = null;
		Ray nearestTarget = null;
		double nearestDist = Double.MAX_VALUE;

		Bullet nearestDeflectable = null;
		double nearestDeflectableDist = Double.MAX_VALUE;

		int j = 0;
		for (int i = 0; i < toAvoid.size(); i++)
		{
			Bullet b = toAvoid.get(i);
			double dist = toAvoidDist.get(i) / b.getSpeed();
			if (dist < nearestDist)
			{
				nearest = b;
				nearestTarget = toAvoidTargets.get(i);
				nearestDist = dist;
			}

			if (j < toAvoidDeflect.size() && toAvoidDeflect.get(j) == b)
			{
				if (!b.heavy && b.canBeCanceled && (!Team.isAllied(this, b) || !this.enableMovement) && dist < nearestDeflectableDist)
				{
					nearestDeflectable = b;
					nearestDeflectableDist = dist;
				}

				j++;
			}
		}

		if (nearest == null)
			return;

		double direction = nearest.getPolarDirection();
		double distance = Movable.distanceBetween(this, nearest);
		double diff = Movable.angleBetween(direction, this.getAngleInDirection(nearest.posX, nearest.posY));

		this.avoidTimer = this.bulletAvoidTimerBase;
		this.nearestBullet = nearest;
		this.nearestBulletDist = nearestDist;

		this.nearestBulletDeflect = nearestDeflectable;
		this.nearestBulletDeflectDist = nearestDeflectableDist;

		double m = distance / nearest.getSpeed() * this.maxSpeed;
		if (m > Game.tile_size * 4 && avoidanceSeekOpenSpaces)
		{
			int count = fleeDistances.length;
			double[] d = fleeDistances;

			for (int dir = 0; dir < count; dir++)
			{
				Ray r = new Ray(this.posX, this.posY, direction + fleeDirections[dir], 0, this, Game.tile_size);
				r.setMaxChunks(2);
				r.ignoreTanks = true;
				r.size = Game.tile_size * this.hitboxSize - 1;

				boolean b = this.targetEnemy != null && this.bulletAvoidBehvavior == BulletAvoidBehavior.aggressive_dodge && Movable.absoluteAngleBetween(fleeDirections[dir] + direction, this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY)) > Math.PI * 0.5;

				double dist = r.getDist();
				d[dir] = dist;

				if (b)
					d[dir] = Math.min(d[dir] - Game.tile_size, Game.tile_size * 3);
			}

			int greatest = -1;
			double gValue = -1;
			for (int i = 0; i < d.length; i++)
			{
				if (d[i] > gValue)
				{
					gValue = d[i];
					greatest = i;
				}
			}

			if (gValue < Game.tile_size * 4)
				this.avoidDirection = direction + fleeDirections[greatest];
			else if (this.avoidTimer <= 0)
			{
				// randomly pick one >= 3 tiles
				for (int i = 0; i < fleeDirections.length; i++)
				{
					int c = (int) (this.random.nextDouble() * count);
                    if (d[c] < Game.tile_size * 4)
                        continue;

                    this.avoidDirection = direction + fleeDirections[greatest];
                    break;
                }
			}
		}
		else
		{
			double frac = Math.max(0, 2 - Math.max(m / (Game.tile_size * 2), 1));

			if (this.bulletAvoidBehvavior == BulletAvoidBehavior.aggressive_dodge || this.bulletAvoidBehvavior == BulletAvoidBehavior.dodge)
			{
				double invert = 1;

				if (this.bulletAvoidBehvavior == BulletAvoidBehavior.aggressive_dodge)
					invert = -1;

				this.avoidDirection = direction + Math.PI * 0.5 * (1 - (1 - frac) * invert / 2) * Math.signum(diff);
			}
			else if (this.bulletAvoidBehvavior == BulletAvoidBehavior.back_off)
			{
				this.avoidDirection = nearest.getAngleInDirection(this.posX, this.posY);
			}
			else if (this.bulletAvoidBehvavior == BulletAvoidBehavior.intersect)
			{
				double targetX = nearestTarget.targetX;
				double targetY = nearestTarget.targetY;

				this.avoidTimer = this.bulletAvoidTimerBase;
				this.avoidDirection = this.getAngleInDirection(targetX, targetY) + Math.PI;
				diff = Movable.angleBetween(this.avoidDirection, direction);

				if (Math.abs(diff) < Math.PI / 4)
					this.avoidDirection = direction + Math.signum(diff) * Math.PI / 4;

				Ray r = new Ray(this.posX, this.posY, this.avoidDirection, 0, this, Game.tile_size);
				r.size = Game.tile_size * this.hitboxSize - 1;
				double d = r.getDist();

				if (d < Game.tile_size * 2)
					this.avoidDirection = direction - diff;
			}
		}
	}

	private boolean shouldDodge(Bullet b, double dist)
	{
		double bulletAngle = b.getAngleInDirection(this.posX, this.posY);
		double distBox = this.enableMovement ? 10 : 20;

		return !(b.tank == this && b.age < 20) && !(this.team != null && Team.isAllied(b, this) && !this.team.friendlyFire)
				&& b.shouldDodge && Math.abs(b.posX - this.posX) < Game.tile_size * distBox && Math.abs(b.posY - this.posY) < Game.tile_size * distBox
				&& (b.getMotionInDirection(bulletAngle) > 0 || dist < this.size * 3);
	}

	public void updateTurretAI()
	{
		if (this.shootingInFan)
		{
			this.updateTurretFan();
			return;
		}

		if ((this.enableLookingAtTargetEnemy || this.straightShoot || this.sightTransformTank != null) && this.targetEnemy instanceof Tank)
			this.lookAtTargetEnemy();

		if (this.shootAIType.equals(ShootAI.homing))
			this.straightShoot = this.seesTargetEnemy;

		if (this.shootAIType.equals(ShootAI.none))
			this.angle = this.orientation;
		else if (this.shootAIType.equals(ShootAI.wander) || this.shootAIType.equals(ShootAI.sprinkler))
			this.updateTurretWander();
		else if (this.shootAIType.equals(ShootAI.straight) || (this.shootAIType == ShootAI.alternate && straightShoot))
			this.updateTurretStraight();
		else
			this.updateTurretReflect();

		if (!BulletArc.class.isAssignableFrom(this.bullet.bulletClass))
			this.pitch -= Movable.angleBetween(this.pitch, 0) / 10 * updateFrequency;

		if (!this.chargeUp)
		{
			double reload = this.getAttributeValue(AttributeModifier.reload, 1);
			this.cooldown -= updateFrequency * reload;
		}

		this.cooldownIdleTime += updateFrequency;

		if (this.cooldownIdleTime >= this.cooldownRevertTime)
			this.cooldownStacks = 0;
	}

	public void updateTurretFan()
	{
		if (this.shootTimer <= -this.shootRoundTime / 2 && this.targetEnemy != null)
		{
			double a = this.aimAngle;

			if (this.shootAIType == ShootAI.sprinkler)
				this.aimAngle = this.angle;

			double originalAimAngle = this.aimAngle;
			this.aimAngle = this.fanOffset + a;

			double speed = this.turretAimSpeed;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.aimAngle, this.angle) > this.turretAimSpeed * updateFrequency)
			{
				if (Movable.angleBetween(this.angle, this.aimAngle) < 0)
					this.angle += speed * updateFrequency;
				else
					this.angle -= speed * updateFrequency;

				this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
			}
			else
			{
				this.angle = this.aimAngle;
				this.shootTimer += updateFrequency;
			}

			this.aimAngle = originalAimAngle;
		}
		else
		{
			this.angle = this.aimAngle + this.fanDirection * (this.shotRoundSpread * Math.PI / 180) * (Math.abs(this.shootTimer / this.shootRoundTime) - 0.5);

			int s = (int) Math.round(this.shootTimer * this.shotRoundCount / this.shootRoundTime);
			if (this.shots < s)
			{
				this.bullet.attemptUse(this);
				this.shots = s;
			}

			if (this.shootTimer > this.shootRoundTime)
			{
				this.shootingInFan = false;
			}

			this.shootTimer += updateFrequency;
		}
	}

	public void updateTurretWander()
	{
		if (this.shootAIType == ShootAI.sprinkler)
		{
			if (this.cooldown <= 0)
			{
				if (this.shotRoundCount <= 1)
					this.bullet.attemptUse(this);
				else
				{
					this.shootingInFan = true;
					this.shootTimer = -this.shootRoundTime / 2;
					this.shots = 0;
					this.fanDirection = this.random.nextDouble() < 0.5 ? 1 : -1;
				}
			}
		}
		else
		{
			Ray a = new Ray(this.posX, this.posY, this.angle, this.bullet.bounces, this);
			a.moveOut(this.size / 10);
			a.size = this.bullet.size;
			a.ignoreDestructible = this.aimIgnoreDestructible;
			a.ignoreShootThrough = true;

			Movable m = a.getTarget();

			if (!(m == null))
				if (!Team.isAllied(m, this) && m instanceof Tank && !((Tank) m).hidden)
					this.shoot();
		}

		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.turretIdleSpeed * updateFrequency;
		else
			this.angle -= this.turretIdleSpeed * updateFrequency;

		this.idleTimer -= updateFrequency;

		if (idleTimer <= 0)
		{
			this.idleTimer = this.random.nextDouble() * turretIdleTimerRandom + turretIdleTimerBase;
			if (this.idlePhase == RotationPhase.clockwise)
				this.idlePhase = RotationPhase.counter_clockwise;
			else
				this.idlePhase = RotationPhase.clockwise;
		}
	}

	public void updateTurretStraight()
	{
		if (this.avoidTimer > 0 && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy && (this.enableMovement || this.nearestBulletDeflectDist <= this.bulletThreatCount * Math.max(this.cooldownBase, 50) * 1.5))
		{
			double a = this.nearestBulletDeflect.getAngleInDirection(this.posX + Game.tile_size / this.bullet.speed * this.nearestBulletDeflect.vX, this.posY + Game.tile_size / this.bullet.speed * this.nearestBulletDeflect.vY);
			double speed = this.nearestBulletDeflect.getLastMotionInDirection(a + Math.PI / 2);

			if (speed < this.bullet.speed)
			{
				double d = this.getAngleInDirection(nearestBulletDeflect.posX, nearestBulletDeflect.posY) - Math.asin(speed / this.bullet.speed);

				if (!Double.isNaN(d))
					this.aimAngle = d;
			}

			this.disableOffset = true;
		}
		else
		{
			if (this.hasTarget && this.targetEnemy != null)
			{
				if (BulletArc.class.isAssignableFrom(this.bullet.bulletClass))
					this.setAimAngleArc();
				else
					this.setAimAngleStraight();
			}
		}

		if (!this.hasTarget || this.targetEnemy == null)
			return;

		if (BulletArc.class.isAssignableFrom(this.bullet.bulletClass))
		{
			double pitch = Math.atan(this.distance / this.bullet.speed * 0.5 * BulletArc.gravity / this.bullet.speed);
			this.pitch -= Movable.angleBetween(this.pitch, pitch) / 10 * updateFrequency;
		}

		this.checkAndShoot();

		double speed = this.turretAimSpeed;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.aimAngle, this.angle) > this.turretAimSpeed * updateFrequency)
		{
			if (Movable.angleBetween(this.angle, this.aimAngle) < 0)
				this.angle += speed * updateFrequency;
			else
				this.angle -= speed * updateFrequency;

			this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
		}
		else
			this.angle = this.aimAngle;

		if (this.seesTargetEnemy && this.targetEnemy != null && Movable.distanceBetween(this, this.targetEnemy) < Game.tile_size * 6 && !chargeUp)
			this.cooldown -= updateFrequency;
	}

	public void setAimAngleStraight()
	{
		if (this.predictiveFiring && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
		{
			Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getLastPolarDirection(), 0, (Tank) targetEnemy);
			r.ignoreDestructible = this.aimIgnoreDestructible;
			r.ignoreShootThrough = true;
			r.size = Game.tile_size * this.hitboxSize - 1;
			r.enableBounciness = false;
			this.disableOffset = false;

			double a = this.targetEnemy.getAngleInDirection(this.posX, this.posY);
			double speed = this.targetEnemy.getLastMotionInDirection(a + Math.PI / 2);

			double distBtwn = Movable.distanceBetween(this, this.targetEnemy);
			double time = distBtwn / Math.sqrt(this.bullet.speed * this.bullet.speed - speed * speed);

			double distSq = Math.pow(targetEnemy.lastFinalVX * time, 2) + Math.pow(targetEnemy.lastFinalVY * time, 2);

			double d = r.getDist();

			if (d * d > distSq && speed < this.bullet.speed)
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY) - Math.asin(speed / this.bullet.speed);
			else
				this.aimAngle = this.getAngleInDirection(r.posX, r.posY);
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.disableOffset = false;
		}
	}

	public void setAimAngleArc()
	{
		if (this.targetEnemy == null)
			return;

		if (this.predictiveFiring && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
		{
			Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getLastPolarDirection(), 0, (Tank) targetEnemy);
			r.size = Game.tile_size * this.hitboxSize - 1;
			r.enableBounciness = false;
			this.disableOffset = false;

			double a = this.targetEnemy.getAngleInDirection(this.posX, this.posY);
			double speed = this.targetEnemy.getLastMotionInDirection(a + Math.PI / 2);

			double distBtwn = Movable.distanceBetween(this, this.targetEnemy);
			double time = distBtwn / Math.sqrt(this.bullet.speed * this.bullet.speed - speed * speed);

			double distSq = Math.pow(targetEnemy.lastFinalVX * time, 2) + Math.pow(targetEnemy.lastFinalVY * time, 2);

			double d = r.getDist();

			if (d * d > distSq && speed < this.bullet.speed)
			{
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY) - Math.asin(speed / this.bullet.speed);

				double c = Math.cos(Movable.absoluteAngleBetween(targetEnemy.getLastPolarDirection(), this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY)));

				double a1 = Math.pow(this.bullet.speed, 2) - Math.pow(targetEnemy.getLastSpeed(), 2);
				double b1 = -2 * targetEnemy.getLastSpeed() * Movable.distanceBetween(this, this.targetEnemy) * c;
				double c1 = -Math.pow(Movable.distanceBetween(this, targetEnemy), 2);
				double t = (-b1 + Math.sqrt(b1 * b1 - 4 * a1 * c1)) / (2 * a1);

				this.distance = Math.sqrt(Math.pow(targetEnemy.posX + t * targetEnemy.lastFinalVX - this.posX, 2) + Math.pow(targetEnemy.posY + t * targetEnemy.lastFinalVY - this.posY, 2));
			}
			else
			{
				this.aimAngle = this.getAngleInDirection(r.posX, r.posY);
				this.distance = Math.sqrt(Math.pow(r.posX - this.posX, 2) + Math.pow(r.posY - this.posY, 2));
			}
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.distance = Math.sqrt(Math.pow(targetEnemy.posX - this.posX, 2) + Math.pow(targetEnemy.posY - this.posY, 2));

			this.disableOffset = false;
		}
	}


	public void checkAndShoot()
	{
		Movable m = null;

		boolean arc = BulletArc.class.isAssignableFrom(this.bullet.bulletClass);

		if (this.targetEnemy != null && !arc)
		{
			Ray r = new Ray(this.posX, this.posY, this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY), 0, this);
			r.moveOut(this.size / 10);
			r.size = this.bullet.size;
			r.ignoreDestructible = this.aimIgnoreDestructible;
			r.ignoreShootThrough = true;
			m = r.getTarget();
		}

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) <= this.aimThreshold)
			if ((arc && this.targetEnemy != null) || (m != null && m.equals(this.targetEnemy) || (this.avoidTimer > 0 && this.disableOffset && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy)))
				this.shoot();
	}

	public void updateTurretReflect()
	{
		if (this.seesTargetEnemy && this.targetEnemy != null && Movable.distanceBetween(this, this.targetEnemy) <= Game.tile_size * 20 && !chargeUp)
		{
			aim = true;
			this.aimAngle = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);
			this.cooldown -= updateFrequency;
		}

		this.search();

		if (this.avoidTimer > 0 && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy && (this.enableMovement || this.nearestBulletDist <= this.bulletThreatCount * Math.max(Math.max(Math.max(this.cooldownBase, this.bullet.cooldownBase), this.bullet.cooldownBase), 50) * 1.5))
		{
			double a = this.nearestBulletDeflect.getAngleInDirection(this.posX + Game.tile_size / this.bullet.speed * this.nearestBulletDeflect.vX, this.posY + Game.tile_size / this.bullet.speed * this.nearestBulletDeflect.vY);
			double speed = this.nearestBulletDeflect.getLastMotionInDirection(a + Math.PI / 2);

			if (speed < this.bullet.speed)
			{
				double d = this.getAngleInDirection(nearestBulletDeflect.posX, nearestBulletDeflect.posY) - Math.asin(speed / this.bullet.speed);

				if (!Double.isNaN(d))
				{
					this.aimAngle = d;
					this.aim = true;
				}
			}

			this.disableOffset = true;
		}

		if (aim && (this.hasTarget || (this.avoidTimer > 0 && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy)))
			this.updateAimingTurret();
		else if (currentlySeeking && this.seekPause <= 0)
			this.updateSeekingTurret();
		else
			this.updateIdleTurret();
	}

	public void search()
	{
		if (this.straightShoot)
		{
			this.searchAngle = this.aimAngle;
		}
		else if (this.searchPhase == RotationPhase.clockwise)
		{
			searchAngle += this.random.nextDouble() * 0.1 * updateFrequency;
		}
		else if (this.searchPhase == RotationPhase.counter_clockwise)
		{
			searchAngle -= this.random.nextDouble() * 0.1 * updateFrequency;
		}
		else
		{
			searchAngle = this.lockedAngle + this.random.nextDouble() * this.searchRange - this.searchRange / 2;
			this.aimTimer -= updateFrequency;
			if (this.aimTimer <= 0)
			{
				this.aimTimer = 0;
				if (this.random.nextDouble() < 0.5)
					this.searchPhase = RotationPhase.clockwise;
				else
					this.searchPhase = RotationPhase.counter_clockwise;
			}
		}

		Ray ray = new Ray(this.posX, this.posY, this.searchAngle, this.bullet.bounces, this);
		ray.setSize(this.bullet.size).moveOut(this.size / 10);
		ray.ignoreDestructible = this.aimIgnoreDestructible;
		ray.ignoreShootThrough = true;

		Movable target = ray.getTarget();

		if (target == null && this.shootAIType == ShootAI.homing && this.targetEnemy != null)
		{
			Ray ray2 = new Ray(ray.posX, ray.posY, ray.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY), 0, this);
			ray2.setSize(this.bullet.size).moveOut(this.size / 50);
			ray2.ignoreDestructible = this.aimIgnoreDestructible;
			ray2.ignoreShootThrough = true;

			target = ray2.getTarget();
		}

		if (target != null)
		{
			if (target.equals(this.targetEnemy))
			{
				this.lockedAngle = this.angle;
				this.searchPhase = RotationPhase.aiming;
				this.aim = true;
				this.aimAngle = this.searchAngle % (Math.PI * 2);
			}
			else if (target instanceof Tank && !((Tank) target).hidden && ((Tank) target).targetable && !Team.isAllied(target, this))
			{
				this.targetEnemy = target;
				this.lockedAngle = this.angle;
				this.searchPhase = RotationPhase.aiming;
				this.aim = true;
				this.aimAngle = this.searchAngle % (Math.PI * 2);
			}
		}
	}

	public void lookAtTargetEnemy()
	{
		if (!this.hasTarget || this.targetEnemy == null)
			return;

		double a;

		a = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

		Ray rayToTarget = new Ray(this.posX, this.posY, a, 0, this);
		rayToTarget.size = this.bullet.size;
		rayToTarget.moveOut(this.size / 10);
		rayToTarget.ignoreDestructible = this.aimIgnoreDestructible;
		rayToTarget.ignoreShootThrough = true;
		Movable target = rayToTarget.getTarget();

		if (target != null)
			this.seesTargetEnemy = target.equals(this.targetEnemy);
		else
			this.seesTargetEnemy = false;

		if (this.straightShoot)
		{
			if (target != null)
			{
				if (target.equals(this.targetEnemy))
					this.aimAngle = a;
				else
					this.straightShoot = false;
			}
			else
				this.straightShoot = false;
		}

		if (this.sightTransformTank != null && seesTargetEnemy && this.inControlOfMotion && !ScreenGame.finishedQuick)
			this.handleSightTransformation();
	}

	public void updateAimingTurret()
	{
		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.turretAimSpeed * updateFrequency)
		{
			this.angle = this.aimAngle;
			this.shoot();
		}
		else
		{
			if (this.chargeUp)
				this.charge();

			double speed = this.turretAimSpeed;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.aimAngle, this.angle) > this.turretAimSpeed * updateFrequency)
			{
				if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
					this.angle += speed * updateFrequency;
				else
					this.angle -= speed * updateFrequency;

				this.angle = this.angle % (Math.PI * 2);
			}
			else
				this.angle = this.aimAngle;

			this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
		}
	}

	public void updateIdleTurret()
	{
		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.turretIdleSpeed * updateFrequency;
		else
			this.angle -= this.turretIdleSpeed * updateFrequency;

		this.idleTimer -= updateFrequency;

		if (this.idleTimer <= 0)
		{
			if (this.idlePhase == RotationPhase.clockwise)
				this.idlePhase = RotationPhase.counter_clockwise;
			else
				this.idlePhase = RotationPhase.clockwise;

			this.idleTimer = (this.random.nextDouble() * this.turretIdleTimerRandom) + this.turretIdleTimerBase;
		}
	}

	public void updateSeekingTurret()
	{
		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.turretIdleSpeed * updateFrequency;
		else
			this.angle -= this.turretIdleSpeed * updateFrequency;

		double dir = this.getPolarDirection();
		if (Movable.absoluteAngleBetween(dir, this.angle) > Math.PI / 8)
		{
			if (Movable.angleBetween(dir, this.angle) < 0)
				this.idlePhase = RotationPhase.counter_clockwise;
			else
				this.idlePhase = RotationPhase.clockwise;
		}
	}

    public boolean isInterestingPathTarget(Movable m)
    {
		if (!(m instanceof Tank t))
			return false;
		if (m.posX < 0 || m.posX >= Game.currentSizeX * Game.tile_size ||
				m.posY < 0 || m.posY >= Game.currentSizeY * Game.tile_size)
			return false;

        if (this.transformMimic)
            return !(m.getClass().equals(this.getClass())) && m.size == this.size;
        if (this.isSupportTank())
            return Team.isAllied(m, this) && m != this && t.canBeHealed()
					&& !(m.getClass().equals(this.getClass()));
        return !Team.isAllied(m, this) && !t.hidden && t.targetable;
    }

    public void setPathfindingTileProperties(Tile t, Obstacle o)
    {
        if (o != null)
            t.unfavorability = o.unfavorability(this);

        if (o == null || !o.tankCollision)
            t.type = Tile.Type.empty;
        else if (o.destructible)
            t.type = Tile.Type.destructible;
        else
            t.type = Tile.Type.solid;

        for (int i = 0; i < dirX.length; i++)
        {
            if (t.isSolid(dirX[i], dirY[i]))
            {
                t.unfavorability++;
                break;
            }
        }
    }

	public void updateMineAI()
	{
		double worstSeverity = Double.MAX_VALUE;

		if (this.mineTimer == -1)
			this.mineTimer = (this.random.nextDouble() * mineTimerRandom + mineTimerBase);

		Object nearest = null;

		if (!laidMine && mineFleeTimer <= 0)
		{
			for (IAvoidObject o: IAvoidObject.avoidances)
			{
				double distSq;

				if (o instanceof Movable)
					distSq = Math.pow(((Movable) o).posX - this.posX, 2) + Math.pow(((Movable) o).posY - this.posY, 2);
				else
					distSq = Math.pow(((Obstacle) o).posX - this.posX, 2) + Math.pow(((Obstacle) o).posY - this.posY, 2);

				if (distSq <= Math.pow(o.getRadius() * this.mineAvoidSensitivity, 2) &&
						!(o instanceof Movable && !(this.team != null && this.team.friendlyFire) && Team.isAllied(this, (Movable) o)))
				{
					double d = o.getSeverity(this.posX, this.posY);

					if (d < worstSeverity)
					{
						worstSeverity = d;
						nearest = o;
					}
				}
			}
		}

		if (this.mineFleeTimer > 0)
			this.mineFleeTimer = Math.max(0, this.mineFleeTimer - updateFrequency);

		laidMine = false;

		if (nearest != null)
		{
			if (this.enableMineAvoidance && this.enableMovement)
			{
				if (nearest instanceof Movable)
					this.setAccelerationAwayFromDirection(((Movable) nearest).posX, ((Movable) nearest).posY, acceleration);
				else
					this.setAccelerationAwayFromDirection(((Obstacle) nearest).posX, ((Obstacle) nearest).posY, acceleration);

				this.overrideDirection = true;
			}
		}
		else
		{
			if (this.mineTimer <= 0 && this.enableMineLaying && !this.disabled)
			{
				boolean layMine = true;
				for (int i = 0; i < Game.movables.size(); i++)
				{
					Movable m = Game.movables.get(i);
					if (!(m instanceof Tank t) || !Team.isAllied(this, m) || m == this)
						continue;

					if (Math.pow(t.posX - this.posX, 2) + Math.pow(t.posY - this.posY, 2) <= Math.pow(200, 2))
					{
						layMine = false;
						break;
					}
				}

				if (layMine)
                    this.mine.attemptUse(this);
			}

			if (!this.currentlySeeking)
				this.mineTimer = Math.max(0, this.mineTimer - updateFrequency);
		}

		if (worstSeverity <= 1 && this.mineFleeTimer <= 0 && this.enableMovement)
		{
			this.overrideDirection = true;
			this.setPolarAcceleration(this.random.nextDouble() * 2 * Math.PI, acceleration);
		}
	}

	public void layMine(Mine m)
	{
		Drawing.drawing.playGameSound("lay_mine.ogg", this, Game.tile_size * 20, (float) (Mine.mine_size / m.size));

		Game.eventsOut.add(new EventLayMine(m));
		Game.movables.add(m);
		this.mineTimer = (this.random.nextDouble() * mineTimerRandom + mineTimerBase);

		int count = fleeDistances.length;
		double[] d = fleeDistances;
		this.mineFleeTimer = 100;

		int k = 0;
		for (double dir = 0; dir < 4; dir += 4.0 / count)
		{
			Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tile_size);
			r.size = Game.tile_size * this.hitboxSize - 1;

			double dist = r.getDist();

			d[k] = dist;
			k++;
		}

		int greatest = -1;
		double gValue = -1;
		for (int i = 0; i < d.length; i++)
		{
			if (d[i] > gValue)
			{
				gValue = d[i];
				greatest = i;
			}
		}

		if (this.enableMovement) // Otherwise stationary tanks will take off when they lay mines :P
		{
			this.setPolarAcceleration(greatest * 2.0 / count * Math.PI, acceleration);
			this.overrideDirection = true;
		}

		laidMine = true;
	}

	public void updateSpawningAI()
	{
		if (this.updateAge <= 0 && !this.destroy && !ScreenGame.finishedQuick)
		{
			for (int i = 0; i < this.spawnedInitialCount; i++)
                spawnTank();
		}

		if (this.random.nextDouble() < this.spawnChance * updateFrequency && this.spawnedTanks.size() < this.spawnedMaxCount && !this.destroy && !ScreenGame.finishedQuick)
			spawnTank();

		for (int i = 0; i < this.spawnedTanks.size(); i++)
		{
			if (this.spawnedTanks.get(i).destroy)
			{
				this.spawnedTanks.remove(i);
				i--;
			}
		}
	}

	public void spawnTank()
	{
		try
		{
			double x = 0, y = 0;

			int attempts;
			for (attempts = 0; attempts <= 10; attempts++)
			{
				double pos = (this.random.nextDouble() - 0.5) * (this.size + Game.tile_size);
				int side = (int) (this.random.nextDouble() * 4);

				x = pos;
				y = pos;

				if (side == 0)
					x = -(this.size / 2 + Game.tile_size / 2);
				else if (side == 1)
					x = (this.size / 2 + Game.tile_size / 2);
				else if (side == 2)
					y = -(this.size / 2 + Game.tile_size / 2);
				else if (side == 3)
					y = (this.size / 2 + Game.tile_size / 2);

				boolean retry;
				if (this.posX + x > Game.tile_size / 2 && this.posX + x < (Game.currentSizeX - 0.5) * Game.tile_size &&
						this.posY + y > Game.tile_size / 2 && this.posY + y < (Game.currentSizeY - 0.5) * Game.tile_size)
                    retry = Game.getObstacle(x, y) != null;
				else
					retry = true;

				if (!retry)
					break;
			}

			if (attempts >= 10)
				return;

			Tank t;
			Tank t2 = null;

			double totalWeight = 0;
			for (SpawnedTankEntry s: this.spawnedTankEntries)
                totalWeight += s.weight;

			double selected = this.random.nextDouble() * totalWeight;

			for (SpawnedTankEntry s: this.spawnedTankEntries)
			{
				selected -= s.weight;

				if (selected <= 0)
				{
					if (s.tank.getClass().equals(TankAIControlled.class))
					{
						t2 = new TankAIControlled("", this.posX + x, this.posY + y, 0, 0, 0, 0, this.angle, ShootAI.none);
						s.tank.cloneProperties((TankAIControlled) t2);
					}
					else
					{
						t2 = Game.registryTank.getEntry(s.tank.name).getTank(this.posX + x, this.posY + y, 0);
					}

					break;
				}
			}

			t = t2;

			t.team = this.team;
			t.crusadeID = this.crusadeID;
			t.parent = this;

			this.spawnedTanks.add(t);

			Game.spawnTank(t, this);
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public void updateSuicideAI()
	{
		if (!this.suicidal)
		{
			boolean die = true;
			for (Movable m : Game.movables)
			{
				if (m != this && m instanceof Tank && Team.isAllied(m, this) && m.dealsDamage && !m.destroy)
				{
					die = false;
					break;
				}
			}

			if (die)
			{
				this.suicidal = true;
				this.timeUntilDeath = this.random.nextDouble() * this.suicideTimerRandom + this.suicideTimerBase;
			}

			return;
		}

		double frac = Math.min(this.timeUntilDeath / this.suicideTimerBase, 1);

		if (!this.disabled)
		{
			this.timeUntilDeath -= updateFrequency;
			this.maxSpeed = this.baseMaxSpeed + this.suicideSpeedBoost * (1 - frac);
			this.enableBulletAvoidance = false;
			this.enableMineAvoidance = false;
		}

		if (this.timeUntilDeath < this.suicideTimerBase)
		{
			this.colorR = frac * this.baseColorR + (1 - frac) * 255;
			this.colorG = frac * this.baseColorG;
			this.colorB = frac * this.baseColorB;

			if (this.timeUntilDeath < 150 && ((int) this.timeUntilDeath % 16) / 8 == 1)
			{
				this.colorR = 255;
				this.colorG = 255;
				this.colorB = 0;
			}

			Game.eventsOut.add(new EventTankUpdateColor(this));
		}

		if (this.timeUntilDeath <= 0)
		{
			if (!this.disabled)
			{
				Explosion e = new Explosion(this.posX, this.posY, this.mine.radius, this.mine.damage, this.mine.destroysObstacles, this);
				e.explode();
			}

			this.destroy = true;
			this.health = 0;
		}
	}

	@Override
	public void updatePossessing()
	{
		this.justTransformed = false;

		if (this.transformMimic)
			this.updatePossessingMimic();
		else
			this.updatePossessingTransform();
	}

	public void updatePossessingTransform()
	{
		if (this.transformTank.destroy)
			this.destroy = true;

		if (this.transformTank.destroy || this.destroy || ScreenGame.finishedQuick || this.positionLock || !this.willRevertTransformation || this.justTransformed)
			return;

		Movable m = null;

		this.posX = this.transformTank.posX;
		this.posY = this.transformTank.posY;
		this.vX = this.transformTank.vX;
		this.vY = this.transformTank.vY;
		this.angle = this.transformTank.angle;

		if (this.transformTank.targetEnemy != null)
		{
			this.targetEnemy = this.transformTank.targetEnemy;
			m = new Ray(this.transformTank.posX, this.transformTank.posY,
					this.transformTank.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY), 0, this)
					.moveOut(5).getTarget();
		}

		if (this.targetEnemy == null || m != this.targetEnemy || this.targetEnemy.destroy)
			this.transformRevertTimer -= updateFrequency;
		else
			this.transformRevertTimer = this.sightTransformRevertTime;

		if (this.transformRevertTimer <= 0 && this.targetable && !this.hidden)
		{
			Game.removeMovables.add(this.sightTransformTank);
			Tank.idMap.put(this.networkID, this);
			this.health = this.sightTransformTank.health;
			this.orientation = this.sightTransformTank.orientation;
			this.pitch = this.sightTransformTank.pitch;
			this.drawAge = this.sightTransformTank.drawAge;
			this.attributes = this.sightTransformTank.attributes;
			this.statusEffects = this.sightTransformTank.statusEffects;
			this.possessingTank = null;
			this.currentlyVisible = true;
			this.targetEnemy = null;
			this.cooldown = Math.min(this.cooldownBase, this.sightTransformTank.cooldown);
			Drawing.drawing.playGlobalSound("slowdown.ogg", 0.75f);
			Game.eventsOut.add(new EventTankTransformPreset(this, false, true));
			Game.movables.add(this);
			this.skipNextUpdate = true;
			this.justTransformed = true;
			this.seesTargetEnemy = false;
		}

		if (this.possessor != null)
			this.possessor.updatePossessing();
	}

	public void updatePossessingMimic()
	{
		if (this.possessingTank.destroy || this.destroy || ScreenGame.finishedQuick || this.positionLock)
			return;

		Class<? extends Movable> c = null;

		this.posX = this.possessingTank.posX;
		this.posY = this.possessingTank.posY;
		this.vX = this.possessingTank.vX;
		this.vY = this.possessingTank.vY;
		this.angle = this.possessingTank.angle;

		Tank t = this.possessingTank.getBottomLevelPossessing();

		if (this.targetEnemy != null)
		{
			if (Panel.panel.ageFrames % 5 == 0 || mimicRevertCounter < 15)
			{
				Ray r = new Ray(this.possessingTank.posX, this.possessingTank.posY, 0, 0, this);
				r.vX = this.targetEnemy.posX - this.possessingTank.posX;
				r.vY = this.targetEnemy.posY - this.possessingTank.posY;

				double ma = Math.sqrt(r.vX * r.vX + r.vY * r.vY) / r.speed;
				r.vX /= ma;
				r.vY /= ma;

				r.angle = Movable.getPolarDirection(r.vX, r.vY);
				r.setMaxDistance(mimicRange).moveOut(5);

				mimicTarget = r.getTarget(2, (Tank) this.targetEnemy);
			}

			if (((Tank) this.targetEnemy).possessor != null)
				c = ((Tank) this.targetEnemy).getTopLevelPossessor().getClass();
			else
				c = this.targetEnemy.getClass();

			if (c == TankPlayer.class || c == TankPlayerRemote.class)
				c = TankPurple.class;
		}

		boolean targetInvalid = this.targetEnemy == null || mimicTarget != this.targetEnemy || this.targetEnemy.destroy ||
				c != this.possessingTank.getClass() || Movable.distanceBetween(this, this.targetEnemy) > this.mimicRange;
		if (targetInvalid)
			this.mimicRevertCounter -= updateFrequency;
		else
			this.mimicRevertCounter = this.mimicRevertTime;

		if (!targetInvalid)
		{
			this.laser = new Laser(t.posX, t.posY, t.size / 2, this.targetEnemy.posX, this.targetEnemy.posY, this.targetEnemy.size / 2,
					(this.mimicRange - Movable.distanceBetween(t, this.targetEnemy)) / this.mimicRange * 10, this.targetEnemy.getAngleInDirection(t.posX, t.posY),
					((Tank) this.targetEnemy).colorR, ((Tank) this.targetEnemy).colorG, ((Tank) this.targetEnemy).colorB);
			Game.movables.add(this.laser);
			Game.eventsOut.add(new EventTankMimicLaser(t, (Tank) this.targetEnemy, this.mimicRange));
		}
		else
			Game.eventsOut.add(new EventTankMimicLaser(t, null, this.mimicRange));

		if (this.mimicRevertCounter <= 0 && this.targetable && !this.hidden && !this.disabled)
		{
			Tank.idMap.put(this.networkID, this);
			this.health = t.health;
			this.orientation = t.orientation;
			this.drawAge = t.drawAge;
			this.attributes = t.attributes;
			this.statusEffects = t.statusEffects;
			this.targetEnemy = null;

			if (t instanceof TankAIControlled)
				this.cooldown = Math.min(this.cooldownBase, ((TankAIControlled) t).cooldown);

			Drawing.drawing.playGlobalSound("slowdown.ogg", 1);

			Game.movables.add(this);
			Game.removeMovables.add(t);

			this.skipNextUpdate = true;
			Game.eventsOut.add(new EventTankMimicTransform(this, this));

			this.tryPossess();
		}
	}

	public void tryPossess()
	{
		if (!this.seesTargetEnemy || !this.hasTarget || !(this.targetEnemy instanceof Tank) || this.destroy || !this.canCurrentlyMimic)
			return;

		try
		{
			this.mimicRevertCounter = this.mimicRevertTime;

			Class<? extends Movable> c = this.targetEnemy.getClass();
			Tank ct;

			ct = (Tank) this.targetEnemy;

			if (((Tank) this.targetEnemy).possessor != null)
			{
				ct = ((Tank) this.targetEnemy).getTopLevelPossessor();
				c = ct.getClass();
			}

			boolean player = false;

			if (c.equals(TankRemote.class))
				c = ((TankRemote) this.targetEnemy).tank.getClass();

			if (c.equals(TankPlayer.class) || c.equals(TankPlayerRemote.class))
			{
				c = TankPurple.class;
				player = true;
			}

			Tank t;
			if (c.equals(TankAIControlled.class))
			{
				t = new TankAIControlled(this.name, this.posX, this.posY, this.size, this.colorR, this.colorG, this.colorB, this.angle, ((TankAIControlled) ct).shootAIType);
				((TankAIControlled) ct).cloneProperties((TankAIControlled) t);
			}
			else
			{
				t = (Tank) c.getConstructor(String.class, double.class, double.class, double.class).newInstance(this.name, this.posX, this.posY, this.angle);
				t.fromRegistry = true;
				t.bullet.className = ItemBullet.classMap2.get(t.bullet.bulletClass);
				t.musicTracks = Game.registryTank.tankMusics.get(ct.name);

				if (t.musicTracks == null)
					t.musicTracks = new HashSet<>();
			}

			t.vX = this.vX;
			t.vY = this.vY;
			t.team = this.team;
			t.health = this.health;
			t.orientation = this.orientation;
			t.drawAge = this.drawAge;
			this.possessingTank = t;
			t.possessor = this;
			t.skipNextUpdate = true;
			t.attributes = this.attributes;
			t.statusEffects = this.statusEffects;
			t.coinValue = this.coinValue;

			t.baseModel = this.baseModel;
			t.turretModel = this.turretModel;
			t.turretBaseModel = this.turretBaseModel;

			if (t instanceof TankAIControlled)
				((TankAIControlled) t).cooldown = this.cooldown;

			t.age = 0;

			t.crusadeID = this.crusadeID;

			t.setNetworkID(this.networkID);

			this.justTransformed = true;

			Game.movables.add(t);
			Game.removeMovables.add(this);

			Drawing.drawing.playGameSound("transform.ogg", this, Game.tile_size * 36, 1f);

			if (player)
			{
				this.possessingTank.colorR = ((Tank) this.targetEnemy).colorR;
				this.possessingTank.colorG = ((Tank) this.targetEnemy).colorG;
				this.possessingTank.colorB = ((Tank) this.targetEnemy).colorB;

				this.possessingTank.secondaryColorR = ((Tank) this.targetEnemy).secondaryColorR;
				this.possessingTank.secondaryColorG = ((Tank) this.targetEnemy).secondaryColorG;
				this.possessingTank.secondaryColorB = ((Tank) this.targetEnemy).secondaryColorB;
			}

			for (RegistryTank.TankEntry e: Game.registryTank.tankEntries)
			{
				if (e.tank.equals(c))
					t.name = e.name;
			}

			Game.eventsOut.add(new EventTankMimicTransform(this, (Tank) this.targetEnemy));

			if (Game.effectsEnabled)
			{
				for (int i = 0; i < 50 * Game.effectMultiplier; i++)
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.piece);
					double var = 50;
					e.colR = Math.min(255, Math.max(0, this.possessingTank.colorR + Math.random() * var - var / 2));
					e.colG = Math.min(255, Math.max(0, this.possessingTank.colorG + Math.random() * var - var / 2));
					e.colB = Math.min(255, Math.max(0, this.possessingTank.colorB + Math.random() * var - var / 2));

					if (Game.enable3d)
						e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, 1 + Math.random() * this.size / 50.0);
					else
						e.setPolarMotion(Math.random() * 2 * Math.PI, 1 + Math.random() * this.size / 50.0);

					Game.effects.add(e);
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public void updateMimic()
	{
		if (this.justTransformed)
			return;

		this.tryPossess();
	}

	/** Called after updating but before applying motion. Intended to be overridden.*/
	public void postUpdate()
	{

	}

	public boolean isSupportTank()
	{
		return !this.suicidal && targetType == TargetType.allies;
	}

	public void setPolarAcceleration(double angle, double acceleration)
	{
		double accX = acceleration * Math.cos(angle);
		double accY = acceleration * Math.sin(angle);
		this.aX = accX;
		this.aY = accY;
	}

	public void addPolarAcceleration(double angle, double acceleration)
	{
		double accX = acceleration * Math.cos(angle);
		double accY = acceleration * Math.sin(angle);
		this.aX += accX;
		this.aY += accY;
	}

	public void setAccelerationInDirection(double x, double y, double accel)
	{
		x -= this.posX;
		y -= this.posY;

		double angle = 0;
		if (x > 0)
			angle = Math.atan(y/x);
		else if (x < 0)
			angle = Math.atan(y/x) + Math.PI;
		else
		{
			if (y > 0)
				angle = Math.PI / 2;
			else if (y < 0)
				angle = Math.PI * 3 / 2;
		}
		double accX = accel * Math.cos(angle);
		double accY = accel * Math.sin(angle);
		this.aX = accX;
		this.aY = accY;
	}

	public void setAccelerationAwayFromDirection(double x, double y, double accel)
	{
		this.setAccelerationInDirectionWithOffset(x, y, accel, Math.PI);
	}

	public void setAccelerationInDirectionWithOffset(double x, double y, double accel, double a)
	{
		x -= this.posX;
		y -= this.posY;

		double angle = 0;
		if (x > 0)
			angle = Math.atan(y/x);
		else if (x < 0)
			angle = Math.atan(y/x) + Math.PI;
		else
		{
			if (y > 0)
				angle = Math.PI / 2;
			else if (y < 0)
				angle = Math.PI * 3 / 2;
		}
		angle += a;
		double accX = accel * Math.cos(angle);
		double accY = accel * Math.sin(angle);
		this.aX = accX;
		this.aY = accY;
	}

	@Override
	public void draw()
	{
		if (this.currentlyVisible || this.destroy)
			super.draw();
		else
		{
			if (this.size * 4 > this.timeInvisible * 2)
			{
				Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);

				if (Game.enable3d)
					Drawing.drawing.fillGlow(this.posX, this.posY, this.size / 4, this.size * 4 - this.age * 2, this.size * 4 - this.age * 2, true, false);
				else
					Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4 - this.age * 2, this.size * 4 - this.age * 2);
			}
		}
	}

	public static TankAIControlled fromString(String s, Level l, String[] remainder)
	{
		if (fieldMap == null)
		{
			fieldMap = new HashMap<>();

			for (Field f : TankAIControlled.class.getFields())
			{
				TankProperty a = f.getAnnotation(TankProperty.class);
				if (a == null)
					continue;

				fieldMap.put(a.id(), f);
				if (a.id().equals("spawned_tanks"))
					fieldMap.put("spawned_tank", f);

				if (Tank.class.isAssignableFrom(f.getType()))
					referenceFields.add(f);
			}
		}

		s = s.strip();
		String original = s;
		String[] r = new String[1];
		TankAIControlled t = new TankAIControlled();

		try
		{
			if (!s.startsWith("["))
			{
				int i = s.indexOf(",");
				int j = s.indexOf("]");
				int k = s.indexOf(";");
				if (i == -1 || j < i)
					i = j;

				if (i == -1 || k < i)
					i = k;

				t.name = s.substring(0, i);

				if (remainder != null)
					remainder[0] = "]" + s.substring(i);
				return t;
			}

			s = s.substring(s.indexOf("[") + 1);
			while (s.charAt(0) != ']')
			{
				int equals = s.indexOf("=");
				String value = s.substring(equals + 1, s.indexOf(";"));
				String propname = s.substring(0, equals);

				Field f = fieldMap.get(propname);

				if (f == null)
				{
					System.err.println("Field " + propname + " not found");
					s = s.substring(s.indexOf(";") + 1);
					continue;
				}

                TankProperty a = f.getAnnotation(TankProperty.class);
                if (f.getType().equals(int.class))
                    f.set(t, Integer.parseInt(value));
                else if (f.getType().equals(double.class))
                    f.set(t, Double.parseDouble(value));
                else if (f.getType().equals(boolean.class))
                    f.set(t, Boolean.parseBoolean(value));
                else if (f.getType().equals(String.class))
                {
                    if (value.equals("*"))
                        f.set(t, null);
                    else if (value.startsWith("\u00A7"))
                    {
                        s = s.substring(equals + 2);
                        int end = s.indexOf("\u00A7");
                        value = s.substring(0, end);
                        s = s.substring(end + 1);
                        f.set(t, value);
                    }
                    else if (value.startsWith("<"))
                    {
                        s = s.substring(equals + 2);
                        int end = s.indexOf(">");
                        int length = Integer.parseInt(s.substring(0, end));
                        value = s.substring(end + 1, end + 1 + length);
                        s = s.substring(end + 1 + length);
                        f.set(t, value);
                    }
                    else
                        f.set(t, value);
                }
                else if (a.miscType() == TankProperty.MiscType.music)
                {
                    int end = s.indexOf("]");
                    String[] csv = s.substring(s.indexOf("[") + 1, end).split(", ");
                    HashSet<String> hashSet;
                    if (csv[0].isEmpty())
                        hashSet = new HashSet<>();
                    else
                        hashSet = new HashSet<>(Arrays.asList(csv));

                    f.set(t, hashSet);
                }
                else if (a.miscType() == TankProperty.MiscType.spawnedTanks && !propname.equals("spawned_tank"))
                {
                    s = s.substring(s.indexOf("[") + 1);
                    ArrayList<SpawnedTankEntry> entries = (ArrayList<SpawnedTankEntry>) f.get(t);

                    TankAIControlled target;
                    while (!s.startsWith("]"))
                    {
                        int x = s.indexOf("x");
                        String s1 = s.substring(0, x);
                        s = s.substring(x + 1);
                        if (s.equals("*"))
                            target = null;
                        else if (s.startsWith("<"))
                        {
                            String tank = s.substring(s.indexOf("<") + 1, s.indexOf(">"));
                            s = s.substring(s.indexOf(">") + 1);
                            target = (TankAIControlled) Game.registryTank.getEntry(tank).getTank(0, 0, 0);
                        }
                        else
                        {
                            TankAIControlled t2 = TankAIControlled.fromString(s, l, r);
							handleValueTank(l, t, t2);

							s = r[0];
                            target = t2;
                            s = s.substring(s.indexOf("]") + 1);
                        }

                        if (s.startsWith(", "))
                            s = s.substring(2);
                        entries.add(new SpawnedTankEntry(target, Double.parseDouble(s1)));
                    }

                    s = s.substring(1);
                }
                else if (IModel.class.isAssignableFrom(f.getType()))
                {
                    if (value.equals("*"))
                        f.set(t, null);
                    else
                        f.set(t, Drawing.drawing.createModel(value));
                }
                else if (f.getType().isEnum())
                {
                    f.set(t, Enum.valueOf((Class<? extends Enum>) f.getType(), value));
                }
                else if (Item.class.isAssignableFrom(f.getType()))
                {
                    Item i = Item.parseItem(null, s);
                    i.unlimitedStack = true;
                    f.set(t, i);
                    s = s.substring(s.indexOf("]") + 1);
                }
                else if (Tank.class.isAssignableFrom(f.getType()) || propname.equals("spawned_tank"))
                {
                    TankAIControlled target;

                    if (value.equals("*"))
                        target = null;
                    else if (value.startsWith("<"))
                    {
                        String tank = s.substring(s.indexOf("<") + 1, s.indexOf(">"));
                        s = s.substring(s.indexOf(">") + 1);
                        target = (TankAIControlled) Game.registryTank.getEntry(tank).getTank(0, 0, 0);
                        target.fromRegistry = true;
                    }
                    else
                    {
                        s = s.substring(s.indexOf("=") + 1);
                        TankAIControlled t2 = TankAIControlled.fromString(s, l, r);
						handleValueTank(l, t, t2);

                        s = r[0];
                        target = t2;
                        s = s.substring(s.indexOf("]") + 1);
                    }

                    if (propname.equals("spawned_tank"))
                    {
                        if (target != null)
                            t.spawnedTankEntries.add(new SpawnedTankEntry(target, 1));
                    }
                    else
                        f.set(t, target);
                }

                s = s.substring(s.indexOf(";") + 1);
			}
		}
		catch (Exception e)
		{
			Game.logger.println("Failed to load tank: " + original);
			System.err.println("Failed to load tank: " + original);
			Game.exitToCrash(e);
		}

		if (remainder != null)
			remainder[0] = s;

		return t;
	}

	private static void handleValueTank(Level level, TankAIControlled parent, TankAIControlled valueTank)
	{
		level.valueTanks.add(valueTank);
		parent.addReference(valueTank, level);
	}

	/** Registers that this tank is used in tank <code>t</code>.
	 * @param t The tank that this tank is referred in */
	public void addReference(TankAIControlled t, Level l)
	{
		if (l == null)
			return;
		l.references.computeIfAbsent(t, k -> new ArrayList<>()).add(this);
	}

	/** Returns the name of the tank if tank references are enabled, otherwise returns {@link #tankString()}. */
	@Override
	public String toString()
	{
		if (useTankReferences && !fromRegistry)
			return this.name;

        return tankString();
    }

	/** Returns the string representation of the tank. */
	public String tankString()
	{
		if (compare == null)
			compare = new TankAIControlled();

		if (fromRegistry)
			return "<" + this.name + ">";

		try
		{
			StringBuilder s = new StringBuilder("[");

			for (Field f : this.getClass().getFields())
			{
				TankProperty a = f.getAnnotation(TankProperty.class);
				if (a == null)
					continue;

				Object obj = f.get(this);
				if (Objects.equals(obj, f.get(compare)))
					continue;

				s.append(a.id()).append("=");

				if (obj != null)
				{
					if (a.miscType() == TankProperty.MiscType.description)
					{
						String desc = (String) obj;
						s.append("<").append(desc.length()).append(">").append(desc);
					}
					else
                        s.append(obj);
				}
				else
					s.append("*");

				s.append(";");
			}

			return s.append("]").toString();
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		return null;
	}

	public static TankAIControlled fromString(String s)
	{
		return fromString(s, Game.currentLevel, null);
	}

	public static TankAIControlled fromString(String s, Level l)
	{
		return fromString(s, l, null);
	}

    public TankAIControlled instantiate(String name, double x, double y, double angle)
    {
        TankAIControlled t = new TankAIControlled(name, x, y, this.size, this.colorR, this.colorG, this.colorB, angle, this.shootAIType);
        this.cloneProperties(t);
		t.registerSelectors();
        return t;
    }

	public static class Tile
	{
		public enum Type {empty, destructible, solid}
		public Tile parent;

		public double posX;
		public double posY;

		public double shiftedX;
		public double shiftedY;

		public int tileX;
        public int tileY;

		public int surrounded = 0;

        public Type type;

        public int unfavorability = 0;

        public Tile(int x, int y, Tile parent, TankAIControlled t)
        {
            this.posX = (x + 0.5) * Game.tile_size;
            this.posY = (y + 0.5) * Game.tile_size;

            this.shiftedX += this.posX;
            this.shiftedY += this.posY;

            this.tileX = x;
            this.tileY = y;

            this.parent = parent;

			Obstacle o = Game.getObstacle(x, y);
			t.setPathfindingTileProperties(this, o);

			for (int i = 0; i < 4; i++)
			{
				int x1 = x + dirX[i];
				int y1 = y + dirY[i];

				if (x1 < 0 || x1 >= Game.currentSizeX || y1 < 0 || y1 >= Game.currentSizeY)
					surrounded++;
				else if (Game.isSolid(x1, y1))
                    surrounded++;
			}

//			if (surrounded > 0)
//                unfavorability++;
		}

        public double shiftSides(Random r, boolean x)
        {
            boolean left, right;
            if (x)
            {
                left = isSolid(-1, 0);
                right = isSolid(1, 0);
			}
			else
			{
				left = isSolid(0, -1);
				right = isSolid(0, 1);
			}

			double d = r.nextDouble();
			if (left && right)
				d -= 0.5;
			else if (right)
				d *= 0.5;
			else if (left)
				d = d * 0.5 - 0.5;
            else
                d = 0;

            return (x ? this.posX : this.posY) + d * (Game.tile_size / 2);
        }

        public boolean isSolid(int x, int y)
        {
            int x1 = this.tileX + x;
            int y1 = this.tileY + y;

            if (x1 < 0 || x1 >= Game.currentSizeX || y1 < 0 || y1 >= Game.currentSizeY)
				return false;

			return Game.isSolid(x1, y1);
		}
	}

	public void solve()
	{
		if (Game.currentLevel == null)
			return;

		Game.currentLevel.customTanks.stream().filter(t1 -> name.equals(t1.name)).findAny().ifPresent(t1 ->
        {
            t1.cloneProperties(this);
			propsCloned = true;
        });
	}

	public boolean solved()
	{
		return propsCloned || fromRegistry;
	}

	/** Clones properties from this tank to the <code>t</code> parameter. */
	public void cloneProperties(TankAIControlled t)
	{
		try
		{
			for (Field f : TankAIControlled.class.getFields())
			{
				TankProperty a = f.getAnnotation(TankProperty.class);
				if (a != null)
				{
					if (Item.class.isAssignableFrom(f.getType()))
					{
						Item i1 = (Item) f.get(this);
						Item i2 = i1.clone();
						f.set(t, i2);
					}
					else if (Tank.class.isAssignableFrom(f.getType()))
					{
						Tank t1 = (Tank) f.get(this);
						if (t1 != null)
						{
							TankAIControlled t2 = new TankAIControlled();

							if (t1 instanceof TankAIControlled)
								((TankAIControlled) t1).cloneProperties(t2);
							f.set(t, t2);
						}
						else
							f.set(t, null);
					}
					else if (a.miscType() == TankProperty.MiscType.spawnedTanks)
					{
						ArrayList<SpawnedTankEntry> a1 = (ArrayList<SpawnedTankEntry>) f.get(this);

						ArrayList<SpawnedTankEntry> al = new ArrayList<>();
						for (SpawnedTankEntry o: a1)
							al.add(new SpawnedTankEntry(o.tank, o.weight));

						f.set(t, al);
					}
					else if (a.miscType() == TankProperty.MiscType.music)
					{
						f.set(t, f.get(this));
					}
					else
						f.set(t, f.get(this));
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		t.health = t.baseHealth;
	}
}
