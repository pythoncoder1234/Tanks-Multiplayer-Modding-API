package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.ButtonList;
import tanks.gui.screen.ScreenGame;

import java.util.ArrayList;

public class EventClearShop extends PersonalEvent
{
    public EventClearShop()
    {

    }

    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

    @Override
    public void execute()
    {
        ScreenGame g = ScreenGame.getInstance();
        if (g != null)
        {
            g.npcShopList = new ButtonList(new ArrayList<>(), 0, 0, (int) ScreenGame.shopOffset, -30);
            g.shopList = new ButtonList(new ArrayList<>(), 0, 0, (int) ScreenGame.shopOffset, -30);
            g.shopItemButtons = new ArrayList<>();
            g.shop = new ArrayList<>();
        }
    }
}
