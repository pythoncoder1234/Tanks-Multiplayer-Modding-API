package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.hotbar.ItemBar;

public class EventSetHotbar extends PersonalEvent
{
    public boolean itemBar;
    public int coins;

    public EventSetHotbar() {}

    public EventSetHotbar(boolean itemBar, int coins)
    {
        this.itemBar = itemBar;
        this.coins = coins;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeBoolean(this.itemBar);
        b.writeInt(this.coins);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.itemBar = b.readBoolean();
        this.coins = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        ItemBar.forceEnabled = true;
        Game.player.hotbar.enabledItemBar = this.itemBar;
        Game.player.hotbar.enabledCoins = this.coins > -1;

        if (this.coins > -1)
            Game.player.hotbar.coins = this.coins;
    }
}
