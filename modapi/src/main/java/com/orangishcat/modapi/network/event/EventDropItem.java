package com.orangishcat.modapi.network.event;

import tanks.Game;
import tanks.ItemDrop;
import tanks.Player;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.ItemEmpty;
import tanks.network.event.EventItemDrop;
import tanks.network.event.EventSetItemBarSlot;
import tanks.tank.TankPlayerRemote;

public class EventDropItem extends EventSetItemBarSlot
{
    public EventDropItem()
    {

    }

    public EventDropItem(int slot)
    {
        super(slot);
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
        {
            for (Player p : Game.players)
            {
                if (p.clientID.equals(this.clientID))
                {
                    ItemBar b = p.hotbar.itemBar;
                    if (b != null && p.tank != null && !(b.slots[slot] instanceof ItemEmpty))
                    {
                        ItemDrop id = new ItemDrop(p.tank.posX, p.tank.posY, b.slots[slot]);
                        id.registerNetworkID();
                        id.cooldown = 200;
                        Game.movables.add(id);
                        Game.eventsOut.add(new EventItemDrop(id));

                        b.slots[slot] = new ItemEmpty();
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
