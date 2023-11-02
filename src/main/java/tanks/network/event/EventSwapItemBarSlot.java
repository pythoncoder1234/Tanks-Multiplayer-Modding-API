package tanks.network.event;

import tanks.Game;
import tanks.Player;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.tank.TankPlayerRemote;

public class EventSwapItemBarSlot extends EventSetItemBarSlot
{
    public EventSwapItemBarSlot()
    {

    }

    public EventSwapItemBarSlot(int index)
    {
        super(index);
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
        {
            for (int i = 0; i < Game.players.size(); i++)
            {
                Player p = Game.players.get(i);
                if (p.clientID.equals(this.clientID))
                {
                    if (p.hotbar.itemBar != null)
                    {
                        ItemBar bar = p.hotbar.itemBar;
                        Item item = bar.slots[bar.selected];
                        bar.slots[bar.selected] = bar.slots[slot];
                        bar.slots[slot] = item;
                    }

                    if (p.tank instanceof TankPlayerRemote)
                        ((TankPlayerRemote) p.tank).refreshAmmo();
                }
            }
        }
        else
        {
            if (Game.player != null && Game.player.hotbar != null && Game.player.hotbar.enabledItemBar)
                Game.player.hotbar.itemBar.selected = this.slot;
        }
    }
}
