package com.orangishcat.modapi.network.event;

import com.orangishcat.modapi.ModAPI;
import com.orangishcat.modapi.menus.FixedText;
import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.network.NetworkUtils;

public class EventDisplayText extends EventAddFixedMenu
{
    public boolean customPos = false;
    public FixedText.types location = null;
    public boolean afterGameStarted;
    public int duration;
    public boolean hasItems = Game.currentLevel != null && (!Game.currentLevel.shop.isEmpty() || !Game.currentLevel.startingItems.isEmpty() || Crusade.crusadeMode);


    public EventDisplayText()
    {

    }

    public EventDisplayText(FixedText t)
    {
        super(t);

        this.customPos = t.location == null;

        if (this.customPos)
        {
            this.posX = t.posX;
            this.posY = t.posY;
        }
        else
            this.location = t.location;

        this.styling = t.styling;
        this.duration = (int) t.duration;
        this.afterGameStarted = t.afterGameStarted;
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);

        b.writeBoolean(this.location == null);
        if (this.location != null)
            NetworkUtils.writeString(b, this.location.toString());

        b.writeBoolean(this.afterGameStarted);
        b.writeBoolean(this.hasItems);
        b.writeInt(this.duration);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);

        this.customPos = b.readBoolean();
        if (!this.customPos)
            this.location = FixedText.types.valueOf(NetworkUtils.readString(b));

        this.afterGameStarted = b.readBoolean();
        this.hasItems = b.readBoolean();
        this.duration = b.readInt();
    }

    @Override
    public void execute()
    {
        FixedText t;

        if (!customPos)
            t = new FixedText(this.location, "");
        else
            t = new FixedText(this.posX, this.posY, "");

        t.styling = this.styling;
        t.afterGameStarted = this.afterGameStarted;
        t.duration = this.duration;
        t.hasItems = this.hasItems;
        t.animations = this.animations;
        ModAPI.fixedMenus.add(t);
    }
}
