package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Tank;
import tanks.tank.TankPlayerController;
import tanks.tank.TankRemote;

public class EventTankControllerUpdateS extends EventTankUpdate
{
    public boolean forced;

    public EventTankControllerUpdateS()
    {

    }

    public EventTankControllerUpdateS(Tank t, boolean forced, boolean recoil)
    {
        super(t);
        this.forced = forced;
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        this.forced = b.readBoolean();
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        b.writeBoolean(this.forced);
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (this.clientID == null && (t instanceof TankRemote || (t instanceof TankPlayerController && (this.forced || !Game.clientID.equals(((TankPlayerController) t).clientID)))))
        {
            if (t instanceof TankPlayerController p && Game.clientID.equals(((TankPlayerController) t).clientID))
            {
                p.interpolatedOffX = this.posX - (t.posX - p.interpolatedOffX * (TankPlayerController.interpolationTime - p.interpolatedProgress) / TankPlayerController.interpolationTime);
                p.interpolatedOffY = this.posY - (t.posY - p.interpolatedOffY * (TankPlayerController.interpolationTime - p.interpolatedProgress) / TankPlayerController.interpolationTime);
                p.interpolatedProgress = 0;
            }

            if (t instanceof TankRemote r)
                setPrevPositions(r);

            t.posX = this.posX;
            t.posY = this.posY;
            t.vX = this.vX;
            t.vY = this.vY;

            t.angle = this.angle;
        }
    }
}
