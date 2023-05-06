package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.hotbar.ItemBar;

public class EventBeginCrusade extends PersonalEvent
{
    public int levelSize;
    public int bonusLifeFreq;

    public EventBeginCrusade() {}

    public EventBeginCrusade(Crusade c)
    {
        this.levelSize = c.levelSize;
        this.bonusLifeFreq = c.bonusLifeFrequency;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.player.hotbar.coins = 0;
            Game.player.hotbar.itemBar = new ItemBar(Game.player);
            Crusade.crusadeMode = true;
            Crusade.currentCrusade = new Crusade(this.levelSize, this.bonusLifeFreq);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.levelSize);
        b.writeInt(this.bonusLifeFreq);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.levelSize = b.readInt();
        this.bonusLifeFreq = b.readInt();
    }
}
