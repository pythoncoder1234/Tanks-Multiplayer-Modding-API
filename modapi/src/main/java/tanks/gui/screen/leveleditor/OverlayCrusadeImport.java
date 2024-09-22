package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.*;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenCrusades;
import tanks.translation.Translation;

import java.util.HashSet;

import static tanks.gui.screen.ScreenCrusades.page;
import static tanks.gui.screen.ScreenCrusades.sortByTime;

public class OverlayCrusadeImport extends ScreenLevelEditorOverlay
{
    public static String importName = null;
    public static boolean itemsToShop = true;
    static HashSet<String> internalCrusades = new HashSet<>();

    public static boolean undo = false;
    public static int coins = 50;

    public static Button selected = null;
    public Button back = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", this::escape
    );
    public SavedFilesList fullCrusadesList;
    public SavedFilesList crusadesList;
    public Button next = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Continue", () -> Game.screen = new OverlayCrusadeImportOptions(this, editor));

    public OverlayCrusadeImport(Screen previous, ScreenLevelEditor editor)
    {
        this(previous, editor, false);
    }

    public OverlayCrusadeImport(Screen previous, ScreenLevelEditor editor, boolean undo)
    {
        super(previous, editor);

        OverlayCrusadeImport.undo = undo;

        search.enableCaps = true;

        next.enabled = false;

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
    }

    public void createNewCrusadesList()
    {
        crusadesList.buttons.clear();
        crusadesList.buttons.addAll(fullCrusadesList.buttons);
        crusadesList.sortButtons();
    }

    SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
    {
        @Override
        public void run()
        {
            createNewCrusadesList();
            crusadesList.filter(search.inputText);
            crusadesList.sortButtons();
        }
    }, "");

    Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
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

    @Override
    public void update()
    {
        crusadesList.update();
        search.update();

        next.update();
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
        next.draw();
        back.draw();

        search.draw();

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
                next.enabled = true;
                b.enabled = false;
                break;
            }
        }
    }
}
