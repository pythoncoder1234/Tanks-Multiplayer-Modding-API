package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.*;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;
import tanks.hotbar.item.Item;
import tanks.tank.TankAIControlled;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static tanks.gui.screen.leveleditor.OverlayCrusadeImport.*;

public class OverlayCrusadeImportOptions extends ScreenLevelEditorOverlay
{
    public static boolean importTanks = true;
    public static boolean importItems = true;
    public static int tankPage = 0;
    public static int itemPage = 0;

    public static boolean[] selectedTanks, selectedItems;
    public static int tankCount, itemCount;
    public static boolean emptySelection = false;

    public enum Mode {tanks, items}
    public static Mode mode = Mode.tanks;

    public Crusade crusade;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public ArrayList<Button> itemButtons = new ArrayList<>();
    public ButtonList tankList, itemList;

    public Button nextTankPage = new Button(this.centerX + 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", () -> editor.tankPage++);
    public Button previousTankPage = new Button(this.centerX - 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", () -> editor.tankPage--);

    public Button tanks = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 3.5, this.objWidth, this.objHeight, "Tanks", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.tanks;
            tanks.enabled = false;
            items.enabled = true;
        }
    });

    public Button items = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 3.5, this.objWidth, this.objHeight, "Items", new Runnable()
    {
        @Override
        public void run()
        {
            mode = Mode.items;
            tanks.enabled = true;
            items.enabled = false;
        }
    });

    public Button tankImport = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            importTanks = !importTanks;
            tankImport.setText("Import tanks: ", importTanks ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    public Button itemImport = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            importItems = !importItems;
            itemImport.setText("Import items: ", importItems ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    public Button all = new Button(this.centerX + this.objXSpace * 0.25, this.centerY - this.objYSpace * 2, this.objWidth / 2, this.objHeight, "All", () ->
    {
        Arrays.fill(mode == Mode.tanks ? selectedTanks : selectedItems, true);

        if (mode == Mode.tanks)
        {
            tankCount = this.tankButtons.size();
            for (Button b : this.tankButtons)
                ((ButtonObject) b).bgColA = 128;
        }
        else
        {
            itemCount = this.itemButtons.size();
            for (Button b : this.itemButtons)
            {
                b.bgColR = b.bgColB = 200;
                b.selectedColR = 200;
                b.selectedColB = 225;
            }
        }
    });

    public Button none = new Button(this.centerX + this.objXSpace * 0.75, this.centerY - this.objYSpace * 2, this.objWidth / 2, this.objHeight, "None", () ->
    {
        Arrays.fill(mode == Mode.tanks ? selectedTanks : selectedItems, false);

        if (mode == Mode.tanks)
        {
            tankCount = 0;
            for (Button b : this.tankButtons)
                ((ButtonObject) b).bgColA = 0;
        }
        else
        {
            itemCount = 0;
            for (Button b : this.itemButtons)
            {
                b.bgColR = b.bgColB = 255;
                b.selectedColR = 240;
                b.selectedColB = 255;
            }
        }
    });

    public TextBox shopCoins = new TextBox(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Shop coins", new Runnable()
    {
        @Override
        public void run()
        {
            if (shopCoins.inputText.isEmpty())
                shopCoins.inputText = shopCoins.previousInputText;
            else
                coins = Integer.parseInt(shopCoins.inputText);
        }
    }, coins + "", "If the items are imported into starting---items, this many coins worth of each---item will be bought and placed into---the player's hotbar.");


    public Selector itemsLocation = new Selector(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Import items to", new String[]{"Shop", "Starting items"}, new Runnable()
    {
        @Override
        public void run()
        {
            itemsToShop = itemsLocation.selectedOption == 0;
        }
    });

    public Button importFromCrusade = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5.5, this.objWidth, this.objHeight, "Import", () -> new Thread(this::importFunc).start());

    public Button importDisabled = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5.5, this.objWidth, this.objHeight, "Import", "You must select something---to import!");

    public Button back = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5.5, this.objWidth, this.objHeight, "Back", this::escape
    );

    public OverlayCrusadeImportOptions(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        this.crusade = getCrusade(true);

        if (selectedTanks == null || selectedItems == null || crusade.customTanks.size() != selectedTanks.length || crusade.crusadeItems.size() != selectedItems.length)
        {
            tankPage = 0;
            itemPage = 0;
            selectedTanks = new boolean[crusade.customTanks.size()];
            selectedItems = new boolean[crusade.crusadeItems.size()];
        }

        if (undo)
        {
            importFromCrusade.setText("Un-import");
            importDisabled.setText("Un-import");
        }

        itemsLocation.selectedOption = itemsToShop ? 0 : 1;
        items.enabled = mode == Mode.tanks;
        tanks.enabled = !items.enabled;

        shopCoins.allowNumbers = true;
        shopCoins.allowDoubles = true;
        shopCoins.allowLetters = false;

        this.nextTankPage.image = "icons/forward.png";
        this.nextTankPage.imageSizeX = 25;
        this.nextTankPage.imageSizeY = 25;
        this.nextTankPage.imageXOffset = 145;

        this.previousTankPage.image = "icons/back.png";
        this.previousTankPage.imageSizeX = 25;
        this.previousTankPage.imageSizeY = 25;
        this.previousTankPage.imageXOffset = -145;

        int index = 0;
        for (TankAIControlled t : crusade.customTanks)
        {
            int j = index;
            ButtonObject b = new ButtonObject(t, 0, 0, 75, 75, () -> {}, t.description);
            b.bgColR = b.bgColB = 25;
            b.bgColG = 100;

            b.function = () ->
            {
                selectedTanks[j] = !selectedTanks[j];
                b.bgColA = selectedTanks[j] ? 128 : 0;
                tankCount += selectedTanks[j] ? 1 : -1;
            };

            b.function.run();
            b.function.run();

            this.tankButtons.add(b);
            index++;
        }

        index = 0;
        for (Item i : crusade.crusadeItems)
        {
            Button b = new Button(0, 0, objXSpace, objYSpace, i.name, () -> {});
            b.image = i.icon;
            b.imageSizeX = 40;
            b.imageSizeY = 40;
            b.imageXOffset = -135;

            int j = index;
            b.function = () ->
            {
                selectedItems[j] = !selectedItems[j];
                b.bgColR = b.bgColB = selectedItems[j] ? 200 : 255;
                b.selectedColR = selectedItems[j] ? 200 : 240;
                b.selectedColB = selectedItems[j] ? 225 : 255;
                itemCount += selectedItems[j] ? 1 : -1;
            };

            b.function.run();
            b.function.run();

            int p = i.price;
            if (p == 0)
                b.setSubtext("Free!");
            else if (p == 1)
                b.setSubtext("1 coin");
            else
                b.setSubtext("%d coins", p);

            this.itemButtons.add(b);

            index++;
        }

        tankList = new ButtonList(tankButtons, tankPage, 0, 70, 30);
        tankList.centerAlign = false;
        tankList.objWidth = tankList.objHeight = 75;
        tankList.objXSpace = tankList.objYSpace = 105;
        tankList.rows = 3;
        tankList.columns = 7;
        tankList.sortButtons();

        itemList = new ButtonList(itemButtons, itemPage, 0, undo ? 90 : 100, -10);
        itemList.columns = 2;
        itemList.rows = undo ? 6 : 3;

        if (undo && itemButtons.size() > itemList.rows * itemList.columns)
        {
            itemList.controlsYOffset = -30;
            importFromCrusade.posY += this.objYSpace;
            importDisabled.posY += this.objYSpace;
            back.posY += this.objYSpace;
        }

        itemList.sortButtons();

        tankImport.setText("Import tanks: ", importTanks ? ScreenOptions.onText : ScreenOptions.offText);
        itemImport.setText("Import items: ", importItems ? ScreenOptions.onText : ScreenOptions.offText);
    }

    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        String title = mode == Mode.tanks ? "Import tanks" : "Import items";
        if (undo)
            title = "Un-" + title;
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, title);

        back.draw();

        if (!emptySelection)
            importFromCrusade.draw();
        else
            importDisabled.draw();

        tanks.draw();
        items.draw();

        all.draw();
        none.draw();

        if (mode == Mode.tanks)
        {
            tankImport.draw();
            tankList.draw();
        }
        else
        {
            itemImport.draw();
            itemList.draw();

            if (!undo)
            {
                itemsLocation.draw();
                shopCoins.draw();
            }
        }
    }

    @Override
    public void update()
    {
        super.update();

        tankPage = tankList.page;
        itemPage = itemList.page;
        emptySelection = !(importTanks && tankCount > 0) && !(importItems && itemCount > 0);

        if (!emptySelection)
            importFromCrusade.update();
        else
            importDisabled.update();

        back.update();

        tanks.update();
        items.update();

        all.update();
        none.update();

        if (mode == Mode.tanks)
        {
            tankImport.update();
            tankList.update();
        }
        else
        {
            itemImport.update();
            itemList.update();

            if (!undo)
            {
                itemsLocation.update();
                shopCoins.update();
            }
        }
    }

    public Crusade getCrusade(boolean replace)
    {
        boolean internal = internalCrusades.contains(selected.text);

        String name = replace ? selected.text.replaceAll(" ", "_") : selected.text;

        String localPath = "/" + name + ".tanks";
        String path = (internal ? "/crusades" : Game.homedir + Game.crusadeDir) + localPath;
        ArrayList<String> al = new ArrayList<>();

        if (!internal)
        {
            BaseFile f = Game.game.fileManager.getFile(path);

            try
            {
                f.startReading();
                while (f.hasNextLine())
                    al.add(f.nextLine());
                f.stopReading();
            }
            catch (Exception e)
            {
                if (replace)
                    return getCrusade(false);
                else
                    throw new RuntimeException(e);
            }
        }
        else
            al = Game.game.fileManager.getInternalFileContents(path.toLowerCase());

        return new Crusade(al, selected.text, localPath);
    }

    public void importFunc()
    {
        if (importTanks)
            addOrRemove(editor.level.customTanks, crusade.customTanks, !undo, selectedTanks);

        if (importItems)
        {
            if (!itemsToShop)
            {
                for (Item i : crusade.crusadeItems)
                    i.stackSize = Math.max(1, Math.min(i.maxStackSize, coins / i.price * i.stackSize));
            }
            else if (!undo)
                editor.level.startingCoins += coins;
            else if (editor.level.startingCoins >=   coins)
                editor.level.startingCoins -= coins;

            for (Item i : crusade.crusadeItems)
                i.importProperties();

            if (itemsToShop)
                addOrRemove(editor.level.shop, crusade.crusadeItems, !undo, selectedItems);

            if (!itemsToShop || undo)
                addOrRemove(editor.level.startingItems, crusade.crusadeItems, !undo, selectedItems);
        }

        escape();
        ((ScreenLevelEditorOverlay) Game.screen).escape();
    }

    public <T> void addOrRemove(ArrayList<T> a, ArrayList<? extends T> b, boolean add, boolean[] filter)
    {
        if (!add)
        {
            if (a.isEmpty())
                return;

            try
            {
                Field f = a.get(0).getClass().getField("name");
                ArrayList<T> remove = new ArrayList<>();
                for (T t : a)
                {
                    int i = 0;
                    for (T t1 : b)
                    {
                        if (!filter[i])
                            continue;

                        if (Objects.equals(f.get(t), f.get(t1)))
                            remove.add(t);
                        i++;
                    }
                }

                a.removeAll(remove);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            int i = 0;
            for (T t1 : b)
            {
                if (filter[i])
                    a.add(t1);
                i++;
            }
        }
    }
}
