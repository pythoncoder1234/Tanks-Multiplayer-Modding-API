package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Tank;
import tanks.tank.TankNPC;

public class EventAddTankNPC extends EventTankCreate
{
    public TankNPC npc;

    public EventAddTankNPC() {}

    public EventAddTankNPC(Tank t)
    {
        super(t);
        npc = ((TankNPC) t);
    }

    @Override
    public void execute()
    {
        Game.movables.add(npc);
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        npc.writeTo(b);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        npc = TankNPC.readFrom(b, posX, posY, angle);
    }
}
