package tanks.tank;

import basewindow.InputCodes;
import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.input.InputBinding;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.menus.NPCMessage;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemRemote;
import tanks.network.ISyncable;
import tanks.network.NetworkUtils;
import tanks.network.SyncedFieldMap;
import tanks.network.event.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static tanks.gui.screen.ScreenGame.shopOffset;

public class TankNPC extends TankDummy implements ISyncable, IModdedTank
{
    public SyncedFieldMap map = new SyncedFieldMap();

    public static final String shopCommand = "/shop";
    public static final InputBindingGroup select = new InputBindingGroup("viewNPC", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_E));

    public String npcName;
    public MessageList messages = null;
    public String tagName;
    public ButtonList npcShopList;
    public ArrayList<Item> shopItems;

    public boolean overrideDisplayState = false;
    public boolean isChatting = false;
    public boolean playerNear = false;

    public String currentLine = "";
    public boolean draw = false;
    public int messageNum = 0;
    public double counter = 0;

    public final TankDummy icon = new TankDummy("icon", 200, 60, ModAPI.right);
    public NPCMessage messageDisplay;

    public TankNPC(String name, int x, int y, double angle, MessageList messageList, double r, double g, double b)
    {
        this(name, x, y, angle, messageList, "", r, g, b);
    }

    public TankNPC(String name, int x, int y, double angle, MessageList messageList, String tagName, double r, double g, double b)
    {
        this(name, x, y, angle, messageList, tagName, r, g, b, r, g, b, new ArrayList<>());
    }

    public TankNPC(String name, int x, int y, double angle, MessageList messageList, String tagName, double r, double g, double b, Item... shop)
    {
        this(name, x, y, angle, messageList, tagName, r, g, b, r, g, b, new ArrayList<>(Arrays.asList(shop)));
    }

    public TankNPC(String name, int x, int y, double angle, MessageList messageList, String tagName, double r, double g, double b, double nameR, double nameG, double nameB, Item... shop)
    {
        this(name, x, y, angle, messageList, tagName, r, g, b, nameR, nameG, nameB, new ArrayList<>(Arrays.asList(shop)));
    }

    public TankNPC(String name, int x, int y, double angle, MessageList messageList, String tagName, double r, double g, double b, ArrayList<Item> shop)
    {
        this(name, x, y, angle, messageList, tagName, r, g, b, r, g, b, shop);
    }

    public TankNPC(String name, int x, int y, double angle, MessageList messageList, String tagName, double r, double g, double b, double nameR, double nameG, double nameB, ArrayList<Item> shop)
    {
        super("npc", x, y * 50 + 25, angle);

        if (messageList != null && messageList.size() > 0)
            this.messages = messageList;

        this.npcName = name;
        this.shopItems = shop;
        this.tagName = tagName;
        this.showName = tagName != null && tagName.length() > 0;

        if (this.showName)
            this.nameTag = new NameTag(this, 0, this.size / 7 * 5, this.size / 2, tagName, nameR, nameG, nameB);

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.secondaryColorR = Turret.calculateSecondaryColor(this.colorR);
        this.secondaryColorG = Turret.calculateSecondaryColor(this.colorG);
        this.secondaryColorB = Turret.calculateSecondaryColor(this.colorB);

        this.invulnerable = true;
        this.targetable = false;
        this.collisionPush = false;
        this.mandatoryKill = false;

        icon.colorR = r;
        icon.colorG = g;
        icon.colorB = b;
        icon.secondaryColorR = Turret.calculateSecondaryColor(this.colorR);
        icon.secondaryColorG = Turret.calculateSecondaryColor(this.colorG);
        icon.secondaryColorB = Turret.calculateSecondaryColor(this.colorB);

        this.messageDisplay = new NPCMessage(this);
    }

    public void initShop(ArrayList<Item> shop)
    {
        Game.eventsOut.add(new EventClearNPCShop(this.networkID));
        ArrayList<Button> shopItemButtons = new ArrayList<>();

        for (int i = 0; i < shop.size(); i++)
        {
            final int j = i;
            Item item = shop.get(j);
            if (item instanceof ItemRemote)
                continue;

            String price = item.price + " ";
            if (item.price == 0)
                price = "Free!";
            else if (item.price == 1)
                price += "coin";
            else
                price += "coins";

            Button b = new Button(0, 0, Drawing.drawing.objWidth, 40, item.name, () -> {
                if (!ScreenPartyLobby.isClient)
                {
                    int pr = shop.get(j).price;
                    if (Game.player.hotbar.coins >= pr)
                    {
                        if (Game.player.hotbar.itemBar.addItem(shop.get(j)))
                            Game.player.hotbar.coins -= pr;
                    }
                }
                else
                    Game.eventsOut.add(new EventPurchaseNPCItem(j, this.networkID));

                Game.game.window.pressedButtons.remove(InputCodes.MOUSE_BUTTON_1);
            });

            b.image = item.icon;
            b.imageXOffset = -145;
            b.imageSizeX = 35;
            b.imageSizeY = 35;
            b.subtext = price;

            shopItemButtons.add(b);

            Game.eventsOut.add(new EventAddNPCShopItem(i, item.name, price, item.price, item.icon, this.networkID));
        }

        this.npcShopList = new ButtonList(shopItemButtons, 0, 0, (int) shopOffset, -30);
        Game.eventsOut.add(new EventSortNPCShopButtons(this.networkID));
    }

    @Override
    public void draw()
    {
        super.draw();

        if (this.messages != null && !overrideDisplayState && playerNear)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawImage("talk.png", this.posX, this.posY - 50, 64, 64);
        }
    }

    @Override
    public void update()
    {
        super.update();

        playerNear = ModAPI.withinRange((this.posX - 25) / 50, (this.posY - 25) / 50, 3).contains(Game.playerTank);

        if (((ScreenGame) Game.screen).npcShopScreen && !playerNear)
            ((ScreenGame) Game.screen).npcShopScreen = false;

        if (!overrideDisplayState)
        {
            if (playerNear && select.isValid())
            {
                select.invalidate();
                if (!isChatting)
                {
                    isChatting = true;

                    if (!ModAPI.fixedMenus.contains(this.messageDisplay))
                        ModAPI.fixedMenus.add(this.messageDisplay);

                    initMessageScreen();
                }
                else if (messageNum == messages.size() - 1)
                {
                    isChatting = false;
                    messageNum = 0;
                }
                else if (this.currentLine.length() < messages.get(messageNum).length())
                {
                    this.currentLine = messages.get(messageNum);
                }
                else
                {
                    messageNum++;
                    initMessageScreen();
                }
            }

            if (isChatting && playerNear)
                draw = true;
            else
            {
                isChatting = false;
                draw = false;
                messageNum = 0;
            }
        }

        if (this.draw && (!((ScreenGame) Game.screen).paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && this.messages != null && this.messages.get(messageNum) != null)
        {
            if (this.counter <= 0)
            {
                if (this.currentLine.length() < this.messages.get(messageNum).length())
                    this.currentLine += this.messages.get(messageNum).charAt(this.currentLine.length());

                this.counter = 3;
            }
            this.counter -= Panel.frameFrequency * 4;
        }
    }

    /** Only writes the properties that are used in this NPC and isn't written already by {@link EventTankCreate}. */
    public void writeTo(ByteBuf b)
    {
        NetworkUtils.writeString(b, String.join("&#13;", messages.raw));

        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);

        b.writeInt(this.networkID);

        b.writeBoolean(this.nameTag != null);
        if (this.nameTag != null)
            this.nameTag.writeTo(b);

        b.writeInt(this.shopItems.size());
        for (Item i : this.shopItems)
            NetworkUtils.writeString(b, i.toString());
    }

    /** Only reads the properties that are used in this NPC and isn't read already by {@link EventTankCreate}. */
    public static TankNPC readFrom(ByteBuf b, double posX, double posY, double angle)
    {
        TankNPC t = new TankNPC("npc", (int) posX, (int) (posY / Game.tile_size), angle,
                new MessageList(Objects.requireNonNull(NetworkUtils.readString(b)).split("&#13;")),
                b.readDouble(), b.readDouble(), b.readDouble());

        t.networkID = b.readInt();

        if (b.readBoolean())
        {
            t.nameTag = NameTag.readFrom(b);
            t.nameTag.movable = t;
            t.showName = true;
        }

        int size = b.readInt();
        for (int i = 0; i < size; i++)
            t.shopItems.add(Item.parseItem(null, Objects.requireNonNull(NetworkUtils.readString(b))));

        return t;
    }

    public void setMessages(MessageList messages)
    {
        this.messages = messages;
        this.initMessageScreen();

        Game.eventsOut.add(new EventChangeNPCMessage(this));
    }

    public void setOverrideState(boolean displayState)
    {
        setOverrideState(displayState, true);
    }

    public void setOverrideState(boolean displayState, boolean override)
    {
        this.overrideDisplayState = override;
        this.draw = displayState;

        if (this.draw)
            initMessageScreen();

        Game.eventsOut.add(new EventOverrideNPCState(this));
    }

    public void initMessageScreen()
    {
        if (this.messages == null || this.messages.get(messageNum) == null)
            return;

        if (this.messages.get(messageNum).equals(shopCommand))
        {
            this.initShop(this.shopItems);

            ((ScreenGame) Game.screen).npcShopScreen = true;
            ((ScreenGame) Game.screen).npcShopList = this.npcShopList;

            if (Game.followingCam)
                Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

            return;
        }

        this.currentLine = String.valueOf(this.messages.get(messageNum).charAt(0));
    }

    @Override
    public void addFieldsToSync()
    {
        map.putAll("colorR", "colorG", "colorB");
    }

    @Override
    public Class<? extends EventTankCreate> getCreateEvent()
    {
        return EventAddTankNPC.class;
    }

    public static class MessageList
    {
        public String[] raw;

        public MessageList(String... messages)
        {
            this.raw = messages;
        }

        public String get(int index)
        {
            return raw[index];
        }

        public int size()
        {
            return raw.length;
        }
    }
}
