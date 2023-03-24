package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Team;
import tanks.hotbar.item.Item;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

import java.util.ArrayList;

public class EventTankCreate extends PersonalEvent
{
	public ArrayList<Item> items = new ArrayList<>();

	public String type;
	public double posX;
	public double posY;
	public double angle;
	public boolean collisionPush = false;
	public String team;
	public int id;
	public double drawAge;

	public EventTankCreate()
	{
		
	}
	
	public EventTankCreate(Tank t)
	{
		this.type = t.name;
		this.posX = t.posX;
		this.posY = t.posY;
		this.angle = t.angle;
		this.id = t.networkID;
		this.collisionPush = t.collisionPush;
		this.drawAge = t.drawAge;

		if (t.team == null)
			this.team = "*";
		else if (t.team == Game.enemyTeam)
			this.team = "**";
		else
			this.team = t.team.name;
	}
	
	@Override
	public void execute()
	{
		if (this.clientID != null)
			return;

		Tank t = Game.registryTank.getEntry(type).getTank(posX, posY, angle);

		Team tm = Game.currentLevel.teamsMap.get(team);

		if (this.team.equals("**"))
			tm = Game.enemyTeam;

		t.team = tm;
		t.collisionPush = this.collisionPush;
		t.setNetworkID(id);
		t.drawAge = drawAge;

		Game.movables.add(new TankRemote(t));
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.type);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.angle);
		NetworkUtils.writeString(b, this.team);
		b.writeBoolean(this.collisionPush);
		b.writeInt(this.id);
		b.writeDouble(this.drawAge);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.type = NetworkUtils.readString(b);
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.angle = b.readDouble();
		this.team = NetworkUtils.readString(b);
		this.collisionPush = b.readBoolean();
		this.id = b.readInt();
		this.drawAge = b.readDouble();
	}
}
