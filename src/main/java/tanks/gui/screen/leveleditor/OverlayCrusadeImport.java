package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.*;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenCrusades;
import tanks.hotbar.item.Item;
import tanks.translation.Translation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static tanks.gui.screen.ScreenCrusades.page;
import static tanks.gui.screen.ScreenCrusades.sortByTime;

public class OverlayCrusadeImport extends ScreenLevelEditorOverlay
{
    public static String importName = null;
    public static boolean itemsToShop = true;
    public static int coins = 50;

    public Button selected = null;
    public boolean undo;
    public Button back = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 6, this.objWidth, this.objHeight, "Back", this::escape
    );
    public SavedFilesList fullCrusadesList;
    public SavedFilesList crusadesList;
    HashSet<String> internalCrusades = new HashSet<>();
    public Button importFromCrusade = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 6, this.objWidth, this.objHeight, "Import", () -> new Thread(this::importFunc).start());

    public OverlayCrusadeImport(Screen previous, ScreenLevelEditor editor)
    {
        this(previous, editor, false);
    }

    public Selector itemsLocation = new Selector(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Import items to", new String[]{"Shop", "Starting items"}, new Runnable()
    {
        @Override
        public void run()
        {
            itemsToShop = itemsLocation.selectedOption == 0;
        }
    });

    public OverlayCrusadeImport(Screen previous, ScreenLevelEditor editor, boolean undo)
    {
        super(previous, editor);

        this.undo = undo;

        if (undo)
        {
            importFromCrusade.setText("Un-import");
            importFromCrusade.posY -= this.objYSpace;
            back.posY -= this.objYSpace;
        }

        search.enableCaps = true;
        shopCoins.allowNumbers = true;
        shopCoins.allowDoubles = true;
        shopCoins.allowLetters = false;

        itemsLocation.selectedOption = itemsToShop ? 0 : 1;
        importFromCrusade.enabled = false;

        fullCrusadesList = new SavedFilesList(Game.homedir + Game.crusadeDir, page,
                (int) (this.centerX - Drawing.drawing.interfaceSizeX / 2), (int) (-30 + this.centerY - Drawing.drawing.interfaceSizeY / 2),
                (name, file) -> this.selectCrusade(name), (file) -> null);

        addCrusade("adventure_crusade", "Meet all the enemies in---the main crusade of Tanks!");
        addCrusade("classic_crusade", "A retro crusade featuring---levels made long, long ago...");
        addCrusade("castle_crusade", "Invade, defend, and demolish---10 vast castles crawling with---some of the most difficult tanks!");
        addCrusade("beginner_crusade", "An easy crusade serving as---good practice for beginners!");

        fullCrusadesList.sortedByTime = sortByTime;
        fullCrusadesList.sort(sortByTime);
        crusadesList = fullCrusadesList.clone();

        if (importName != null)
            selectCrusade(importName);

        if (fullCrusadesList.sortedByTime)
            sort.setHoverText("Sorting by last modified");
        else
            sort.setHoverText("Sorting by name");

        createNewCrusadesList();
    }    public TextBox shopCoins = new TextBox(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Shop coins", new Runnable()
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

    public void createNewCrusadesList()
    {
        crusadesList.buttons.clear();
        crusadesList.buttons.addAll(fullCrusadesList.buttons);
        crusadesList.sortButtons();
    }    SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
    {
        @Override
        public void run()
        {
            createNewCrusadesList();
            crusadesList.filter(search.inputText);
            crusadesList.sortButtons();
        }
    }, "");

    public void importFunc()
    {
        importFunc(true);
    }    Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            fullCrusadesList.sortedByTime = !fullCrusadesList.sortedByTime;
            fullCrusadesList.sort(fullCrusadesList.sortedByTime);
            createNewCrusadesList();
            crusadesList.filter(search.inputText);
            crusadesList.sortButtons();

            if (fullCrusadesList.sortedByTime)
                sort.setHoverText("Sorting by last modified");
            else
                sort.setHoverText("Sorting by name");
        }
    }, "Sorting by name");

    public void importFunc(boolean replaceSpaces)
    {
        boolean internal = internalCrusades.contains(selected.text);

        String name = replaceSpaces ? selected.text.replaceAll(" ", "_") : selected.text;

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
                if (replaceSpaces)
                {
                    importFunc(false);
                    return;
                }
                else
                    throw new RuntimeException(e);
            }
        }
        else
            al = Game.game.fileManager.getInternalFileContents(path);

        Crusade c = new Crusade(al, selected.text, localPath);
        addOrRemove(editor.level.customTanks, c.customTanks, !undo);

        if (!itemsToShop)
        {
            for (Item i : c.crusadeItems)
                i.stackSize = Math.max(1, Math.min(i.maxStackSize, coins / i.price * i.stackSize));
        }
        else if (!undo)
            editor.level.startingCoins += coins;

        for (Item i : c.crusadeItems)
            i.importProperties();

        if (itemsToShop)
            addOrRemove(editor.level.shop, c.crusadeItems, !undo);

        if (!itemsToShop || undo)
            addOrRemove(editor.level.startingItems, c.crusadeItems, !undo);

        escape();
    }

    @Override
    public void update()
    {
        crusadesList.update();
        search.update();

        if (!undo)
        {
            itemsLocation.update();
            shopCoins.update();
        }

        importFromCrusade.update();
        back.update();

        ScreenCrusades.sortByTime = fullCrusadesList.sortedByTime;

        this.sort.imageSizeX = 25;
        this.sort.imageSizeY = 25;
        this.sort.fullInfo = true;

        if (this.fullCrusadesList.sortedByTime)
            this.sort.image = "icons/sort_chronological.png";
        else
            this.sort.image = "icons/sort_alphabetical.png";

        sort.update();

        page = crusadesList.page;

        super.update();
    }

    @Override
    public void draw()
    {
        super.draw();

        crusadesList.draw();
        importFromCrusade.draw();
        back.draw();

        search.draw();

        if (!undo)
        {
            itemsLocation.draw();
            shopCoins.draw();
        }

        int b = (int) editor.fontBrightness;

        if (crusadesList.buttons.isEmpty())
        {
            Drawing.drawing.setColor(b, b, b);
            Drawing.drawing.setInterfaceFontSize(24);

            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No crusades found");
        }

        sort.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(b, b, b);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, undo ? "Undo import from crusade" : "Import from crusade");
    }

    public void addCrusade(String name1, String desc)
    {
        String name = Game.formatString(name1);
        internalCrusades.add(name);
        fullCrusadesList.buttons.add(new Button(0, 0, fullCrusadesList.objWidth, fullCrusadesList.objHeight, Translation.translate(name), () -> this.selectCrusade(name)));
    }

    public void selectCrusade(String name)
    {
        name = name.replaceAll("_", " ");

        for (Button b : fullCrusadesList.buttons)
        {
            if (b.text.equals(name))
            {
                if (selected != null)
                    selected.enabled = true;

                importName = name;
                selected = b;
                importFromCrusade.enabled = true;
                b.enabled = false;
                break;
            }
        }
    }

    public <T> void addOrRemove(ArrayList<T> a, ArrayList<? extends T> b, boolean add)
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
                    for (T t1 : b)
                    {
                        if (Objects.equals(f.get(t), f.get(t1)))
                            remove.add(t);
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
            a.addAll(b);
    }








}
