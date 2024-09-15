package com.orangishcat.modapi.network.event;

import com.orangishcat.modapi.TextWithStyling;
import com.orangishcat.modapi.menus.Animation;
import com.orangishcat.modapi.menus.FixedMenu;
import io.netty.buffer.ByteBuf;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;

import java.util.ArrayList;

public class EventAddFixedMenu extends PersonalEvent
{
    public double posX;
    public double posY;
    public double duration;

    public TextWithStyling styling;

    public ArrayList<Animation> animations = new ArrayList<>();

    public EventAddFixedMenu() {}

    public EventAddFixedMenu(FixedMenu m)
    {
        this.posX = m.posX;
        this.posY = m.posY;
        this.duration = m.duration;

        this.styling = m.styling;
        this.animations = m.animations;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.duration);

        this.styling.writeTo(b);

        b.writeInt(this.animations.size());
        for (Animation a : this.animations)
            NetworkUtils.writeString(b, a.toString());
    }

    @Override
    public void read(ByteBuf b)
    {
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.duration = b.readDouble();

        this.styling = TextWithStyling.readFrom(b);

        int size = b.readInt();
        for (int i = 0; i < size; i++)
            this.animations.add(Animation.readFrom(b));
    }

    @Override
    public void execute()
    {

    }
}
