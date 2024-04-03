package tanks.tank;

import tanks.Chunk;
import tanks.Effect;
import tanks.Game;
import tanks.Movable;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Face;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.TreeSet;

public class Ray
{
	public double size = 10;
	public double tankHitSizeMul = 1;

	public int bounces;
	public int bouncyBounces = 100;
	public double posX;
	public double posY;
	public double vX;
	public double vY;
	public double angle;

	public boolean enableBounciness = true;
	public boolean ignoreTanks = false;
	public boolean ignoreDestructible = false;
	public boolean ignoreShootThrough = false;

	public boolean trace = Game.traceAllRays;
	public boolean dotted = false;

	public double speed = 10;

	public double age = 0;
	public int traceAge;

	public Tank tank;
	public Tank targetTank;

	public ArrayList<Double> bounceX = new ArrayList<>();
	public ArrayList<Double> bounceY = new ArrayList<>();

	public double targetX;
	public double targetY;
	public boolean acquiredTarget = false;

	public Ray(double x, double y, double angle, int bounces, Tank tank)
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);
		this.angle = angle;

		this.posX = x;
		this.posY = y;
		this.bounces = bounces;

		this.tank = tank;
	}

	public Ray(double x, double y, double angle, int bounces, Tank tank, double speed)
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);
		this.angle = angle;

		this.posX = x;
		this.posY = y;
		this.bounces = bounces;

		this.tank = tank;
	}

	public Movable getTarget(double mul, Tank targetTank)
	{
		this.targetTank = targetTank;
		this.targetTank.removeFaces();
		this.targetTank.size *= mul;
		this.targetTank.addFaces();

		Movable m = this.getTarget();

		this.targetTank.removeFaces();
		this.targetTank.size /= mul;
		this.targetTank.addFaces();

		return m;
	}

	public Movable getTarget()
	{
		double remainder = 0;
		acquiredTarget = true;

		if (isInsideObstacle(this.posX - size / 2, this.posY - size / 2) ||
				isInsideObstacle(this.posX + size / 2, this.posY - size / 2) ||
				isInsideObstacle(this.posX + size / 2, this.posY + size / 2) ||
				isInsideObstacle(this.posX - size / 2, this.posY + size / 2))
			return null;

		for (Movable m: Game.movables)
		{
			if (m instanceof Tank t && m != this.tank)
			{
				if (this.posX + this.size / 2 >= t.posX - t.size / 2 &&
						this.posX - this.size / 2 <= t.posX + t.size / 2 &&
						this.posY + this.size / 2 >= t.posY - t.size / 2 &&
						this.posY - this.size / 2 <= t.posY + t.size / 2)
					return t;
			}
		}

		boolean firstBounce = this.targetTank == null;

		while (this.bounces >= 0 && this.bouncyBounces >= 0)
		{
			double t = Double.MAX_VALUE;
			double collisionX = -1;
			double collisionY = -1;
			Result result = null;

			for (int chunksChecked = 1; chunksChecked <= 12; chunksChecked++)
			{
				double moveX = Chunk.chunkSize * Game.tile_size * Math.cos(angle);
				double moveY = Chunk.chunkSize * Game.tile_size * Math.signum(angle);
				Chunk chunk = Chunk.getChunk(posX + moveX * chunksChecked, posY + moveY * chunksChecked);

				result = checkCollisionIn(chunk, firstBounce, t, collisionX, collisionY);
				if (result.collisionFace != null)
					break;
			}


			this.age += result.t();

			firstBounce = false;

			if (result.collisionFace() != null)
			{
				if (trace)
				{
					double dx = result.collisionX() - posX;
					double dy = result.collisionY() - posY;

					double steps = (Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)) / (1 + Math.pow(this.vX, 2) + Math.pow(this.vY, 2))) + 1);

					if (dotted)
						steps /= 2;

					double s;
					for (s = remainder; s <= steps; s++)
					{
						double x = posX + dx * s / steps;
						double y = posY + dy * s / steps;

						this.traceAge++;

						double frac = 1 / (1 + this.traceAge / 100.0);
						double z = this.tank.size / 2 + this.tank.turretSize / 2 * frac + (Game.tile_size / 4) * (1 - frac);
						if (Game.screen instanceof ScreenGame && !ScreenGame.finished)
							Game.effects.add(Effect.createNewEffect(x, y, z, Effect.EffectType.ray));
					}

					remainder = s - steps;
				}

				this.posX = result.collisionX();
				this.posY = result.collisionY();

				if (result.collisionFace().owner instanceof Movable)
				{
					this.targetX = result.collisionX();
					this.targetY = result.collisionY();
					bounceX.add(result.collisionX());
					bounceY.add(result.collisionY());

					return (Movable) result.collisionFace().owner;
				}
				else if (result.collisionFace().owner instanceof Obstacle && ((Obstacle) result.collisionFace().owner).bouncy)
					this.bouncyBounces--;
				else if (result.collisionFace().owner instanceof Obstacle && !((Obstacle) result.collisionFace().owner).allowBounce)
					this.bounces = -1;
				else
					this.bounces--;

				bounceX.add(result.collisionX());
				bounceY.add(result.collisionY());

				if (this.bounces >= 0)
				{
					if (result.corner())
					{
						this.vX = -this.vX;
						this.vY = -this.vY;
					}
					else if (result.collisionFace().horizontal)
						this.vY = -this.vY;
					else
						this.vX = -this.vX;
				}
			}
			else
				return null;
		}

		return null;
	}

	public Result checkCollisionIn(Chunk c, boolean firstBounce, double t, double collisionX, double collisionY)
	{
		Face collisionFace = null;
		if (vX > 0)
		{
			for (int i = 0; i < Game.verticalFaces.size(); i++)
			{
				double size = this.size;

				Face f = Game.verticalFaces.get(i);
				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				boolean passThrough = false;
				if (f.owner instanceof Obstacle o && !o.bouncy)
					passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);

				if (ignoreTanks && f.owner instanceof Tank)
					passThrough = true;

				if (f.startX < this.posX + size / 2 || !f.solidBullet || !f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
					continue;

				double y = (f.startX - size / 2 - this.posX) * vY / vX + this.posY;
				if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
				{
					t = (f.startX - size / 2 - this.posX) / vX;
					collisionX = f.startX - size / 2;
					collisionY = y;
					collisionFace = f;
					break;
				}
			}
		}
		else if (vX < 0)
		{
			for (int i = Game.verticalFaces.size() - 1; i >= 0; i--)
			{
				Face f = Game.verticalFaces.get(i);

				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				boolean passThrough = false;
				if (f.owner instanceof Obstacle o && !o.bouncy)
					passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);

				if (ignoreTanks && f.owner instanceof Tank)
					passThrough = true;

				if (f.startX > this.posX - size / 2 || !f.solidBullet || f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
					continue;

				double y = (f.startX + size / 2 - this.posX) * vY / vX + this.posY;
				if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
				{
					t = (f.startX + size / 2 - this.posX) / vX;
					collisionX = f.startX + size / 2;
					collisionY = y;
					collisionFace = f;
					break;
				}
			}
		}

		boolean corner = false;
		if (vY > 0)
		{
			for (int i = 0; i < Game.horizontalFaces.size(); i++)
			{
				Face f = Game.horizontalFaces.get(i);

				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				boolean passThrough = false;
				if (f.owner instanceof Obstacle o && !o.bouncy)
					passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);

				if (ignoreTanks && f.owner instanceof Tank)
					passThrough = true;

				if (f.startY < this.posY + size / 2 || !f.solidBullet || !f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
					continue;

				double x = (f.startY - size / 2 - this.posY) * vX / vY + this.posX;
				if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
				{
					double t1 = (f.startY - size / 2 - this.posY) / vY;

					if (t1 == t)
						corner = true;
					else if (t1 < t)
					{
						collisionX = x;
						collisionY = f.startY - size / 2;
						collisionFace = f;
						t = t1;
					}

					break;
				}
			}
		}
		else if (vY < 0)
		{
			for (int i = Game.horizontalFaces.size() - 1; i >= 0; i--)
			{
				Face f = Game.horizontalFaces.get(i);

				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				boolean passThrough = false;
				if (f.owner instanceof Obstacle o && !o.bouncy)
					passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);

				if (ignoreTanks && f.owner instanceof Tank)
					passThrough = true;

				if (f.startY > this.posY - size / 2 || !f.solidBullet || f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
					continue;

				double x = (f.startY + size / 2 - this.posY) * vX / vY + this.posX;
				if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
				{
					double t1 = (f.startY + size / 2 - this.posY) / vY;

					if (t1 == t)
						corner = true;
					else if (t1 < t)
					{
						collisionX = x;
						collisionY = f.startY + size / 2;
						collisionFace = f;
						t = t1;
					}
					break;
				}
			}
		}

		int i = 0;
		for (TreeSet<Face> faces : c.faces)
		{
			for (Face f : (positive ? faces.descendingSet() : faces))
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				boolean passThrough = false;
				if (f.owner instanceof Obstacle o && !o.bouncy)
					passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);

				if (ignoreTanks && f.owner instanceof Tank)
					passThrough = true;

				double a1 = x ? f.startX : f.startY;
				double b1 = x ? f.startY : f.startX;
				double a2 = x ? this.posX : this.posY;
				double b2 = x ? this.posY : this.posX;
				double v1 = x ? this.vX : this.vY;
				double v2 = x ? this.vY : this.vX;

				if (f.startY > this.posY - size / 2 || !f.solidBullet || f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
					continue;

				double x = (f.startY + size / 2 - this.posY) * vX / vY + this.posX;
				if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
				{
					double t1 = (f.startY + size / 2 - this.posY) / vY;

					if (t1 == t)
						corner = true;
					else if (t1 < t)
					{
						collisionX = x;
						collisionY = f.startY + size / 2;
						collisionFace = f;
						t = t1;
					}
					break;
				}
			}
		}

		return new Result(t, collisionX, collisionY, collisionFace, corner);
	}

	public record Result(double t, double collisionX, double collisionY, Face collisionFace, boolean corner) {}

	public double getDist()
	{
		this.bounceX.add(0, this.posX);
		this.bounceY.add(0, this.posY);

		if (!acquiredTarget)
			this.getTarget();

		return getFinalDist();
	}

	public double getTargetDist(double mul, Tank m)
	{
		this.bounceX.add(0, this.posX);
		this.bounceY.add(0, this.posY);

		if (this.getTarget(mul, m) != m)
			return -1;

		return getFinalDist();
	}

	private double getFinalDist()
	{
		double dist = 0;
		for (int i = 0; i < this.bounceX.size() - 1; i++)
            dist += Math.sqrt(Math.pow(this.bounceX.get(i + 1) - this.bounceX.get(i), 2) + Math.pow(this.bounceY.get(i + 1) - this.bounceY.get(i), 2));

		return dist;
	}

	public double getAngleInDirection(double x, double y)
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

		return angle;
	}

	public static boolean isInsideObstacle(double x, double y)
	{
		int ox = (int) (x / Game.tile_size);
		int oy = (int) (y / Game.tile_size);

		return !(ox >= 0 && ox < Game.currentSizeX && oy >= 0 && oy < Game.currentSizeY) || Game.isSolid(ox, oy);
	}

	public void moveOut(double amount)
	{
		this.posX += this.vX * amount;
		this.posY += this.vY * amount;
	}
}
