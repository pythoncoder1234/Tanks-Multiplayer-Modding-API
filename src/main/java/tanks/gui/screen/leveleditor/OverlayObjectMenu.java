package tanks.gui.screen.leveleditor;

import basewindow.InputCodes;
import tanks.*;
import tanks.editor.EditorAction;
import tanks.editor.selector.LevelEditorSelector;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.ITankScreen;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenAddSavedTank;
import tanks.gui.screen.ScreenTankEditor;
import tanks.registry.RegistryObstacle;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

import static tanks.editor.selector.LevelEditorSelector.Position;

public class OverlayObjectMenu extends ScreenLevelEditorOverlay implements ITankScreen
{
    public static Button leftButton, rightButton;

    public int draggedIndex = -1;
    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public ArrayList<Button> obstacleButtons = new ArrayList<>();

    public Runnable drawEditTank = () -> this.editTank.draw();
    public static LevelEditorSelector<?> leftSelector, rightSelector;
    public Button nextTankPage = new Button(this.centerX + 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", () -> editor.tankPage++);
    public Button previousTankPage = new Button(this.centerX - 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", () -> editor.tankPage--);
    public Button nextObstaclePage = new Button(this.centerX + 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", () -> editor.obstaclePage++);
    public Button previousObstaclePage = new Button(this.centerX - 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", () -> editor.obstaclePage--);
    public Button exitObjectMenu = new Button(this.centerX, this.centerY + 240, 350, 40, "Ok", () ->
    {
        Game.screen = editor;
        editor.paused = false;
        editor.clickCooldown = 20;
    }
    );

    public Button editStartingHeight = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "", () -> Game.screen = new OverlayBlockStartingHeight(Game.screen, this.editor));

    public Button placePlayer = new Button(this.centerX - 380, this.centerY - 180, 350, 40, "Player", () ->
    {
        saveSelectors(editor);
        ScreenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.playerTank;
        editor.mouseTank = new TankPlayer(0, 0, 0);
        editor.mouseTank.registerSelectors();
        ((TankPlayer) editor.mouseTank).setDefaultColor();
        loadSelectors(editor.mouseTank, this);
    }
    );

    public Button placeEnemy = new Button(this.centerX, this.centerY - 180, 350, 40, "Tank", () ->
    {
        saveSelectors(editor);
        ScreenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.enemyTank;
        this.editor.refreshMouseTank();
        loadSelectors(editor.mouseTank, this);
    }
    );

    public Button placeObstacle = new Button(this.centerX + 380, this.centerY - 180, 350, 40, "Block", () ->
    {
        saveSelectors(editor);
        ScreenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.obstacle;
        loadSelectors(editor.mouseObstacle, this);
    });

    public Button editTank = new Button(0, 0, 40, 40, "", () ->
    {
        int ind = editor.tankNum - Game.registryTank.tankEntries.size();

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
            this.refreshTanks(editor.level.customTanks.remove(ind));
        else
            Game.screen = new ScreenTankEditor(editor.level.customTanks.get(ind), this);

        editor.modified = true;
    }, "Edit custom tank");

    public Button sort = new Button(this.centerX + this.objXSpace * 1.5 - 40, this.centerY + this.objYSpace * 3, 40, 40, "", () ->
    {
        editor.level.customTanks.sort(this::compareTo);
        Game.screen = new OverlayObjectMenu(previous, editor);
    }, "Sort tanks (irreversible)");

    public ButtonObject playerSpawnsButton = new ButtonObject(new TankSpawnMarker("player", 0, 0, 0), this.centerX + 50, this.centerY, 75, 75, () -> editor.movePlayer = false, "Add multiple player spawn points");

    public ButtonObject movePlayerButton;

    public OverlayObjectMenu(Screen previous, ScreenLevelEditor editor)
    {
        super(previous, editor);

        this.musicInstruments = true;

        TankPlayer tp = new TankPlayer(0, 0, 0);
        tp.setDefaultColor();
        movePlayerButton = new ButtonObject(tp, this.centerX - 50, this.centerY, 75, 75, () -> editor.movePlayer = true, "Move the player");

        editStartingHeight.imageXOffset = -155;
        editStartingHeight.imageSizeX = 30;
        editStartingHeight.imageSizeY = 30;
        editStartingHeight.image = "icons/obstacle_startheight.png";

        sort.imageSizeX = 25;
        sort.imageSizeY = 25;
        sort.image = "icons/sort_alphabetical.png";
        sort.fullInfo = true;

        if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
            loadSelectors(editor.mouseObstacle, this);
        else
            loadSelectors(editor.mouseTank, this);

        if (leftButton != null)
            loadButton(leftButton, leftSelector);

        if (rightButton != null)
            loadButton(rightButton, rightSelector);

        int count = Game.registryTank.tankEntries.size() + this.editor.level.customTanks.size();
        int rows = objectButtonRows;
        int cols = objectButtonCols;

        for (int i = 0; i <= count; i++)
        {
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            Tank t;

            if (i < Game.registryTank.tankEntries.size())
            {
                t = Game.registryTank.tankEntries.get(i).getTank(x, y, 0);
            }
            else if (i == count)
            {
                Button b = new Button(x, y, 50, 50, "+", () ->
                {
                    ScreenAddSavedTank s = new ScreenAddSavedTank(this);
                    s.drawBehindScreen = true;
                    editor.modified = true;
                    Game.screen = s;
                }, "Create a new custom tank!");
                this.tankButtons.add(b);
                b.fullInfo = true;

                continue;
            }
            else
                t = this.editor.level.customTanks.get(i - Game.registryTank.tankEntries.size());

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                saveSelectors(editor);

                editor.tankNum = j;

                if (j < Game.registryTank.tankEntries.size())
                {
                    editor.mouseTank = Game.registryTank.getEntry(j).getTank(0, 0, editor.mouseTank.angle);
                    editor.mouseTank.selectors = t.selectors;
                }
                else
                    editor.mouseTank = ((TankAIControlled) t).instantiate(t.name, 0, 0, 0);

                loadSelectors(editor.mouseTank, this);
            }
                    , t.description);

            if (i >= Game.registryTank.tankEntries.size())
            {
                b.draggable = true;
                int fi = i - Game.registryTank.tankEntries.size();
                b.finishDrag = (b1, canceled) -> this.finishDrag(fi, canceled);
            }

            b.drawObjectForInterface = () -> minimizeTankGlow(t, b);

            if (t.description.isEmpty())
                b.enableHover = false;

            this.tankButtons.add(b);
        }

        int hidden = 0;
        for (int i = 0; i < Game.registryObstacle.obstacleEntries.size(); i++)
        {
            int index = (i-hidden) % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i-hidden;

            RegistryObstacle.ObstacleEntry entry = Game.registryObstacle.obstacleEntries.get(i);
            if (!Game.debug && entry.hidden)
            {
                hidden++;
                continue;
            }

            tanks.obstacle.Obstacle o = entry.getObstacle(x, y);

            ButtonObject b = new ButtonObject(o, x, y, 75, 75, () ->
            {
                saveSelectors(editor);

                editor.obstacleNum = j;
                editor.mouseObstacle = Game.registryObstacle.getEntry(j).getObstacle(0, 0);

                leftButton = null;
                rightButton = null;

                loadSelectors(editor.mouseObstacle, this);
            }
                    , o.description);

            if (o.description.isEmpty())
                b.enableHover = false;

            this.obstacleButtons.add(b);
        }

        this.nextObstaclePage.image = "icons/forward.png";
        this.nextObstaclePage.imageSizeX = 25;
        this.nextObstaclePage.imageSizeY = 25;
        this.nextObstaclePage.imageXOffset = 145;

        this.previousObstaclePage.image = "icons/back.png";
        this.previousObstaclePage.imageSizeX = 25;
        this.previousObstaclePage.imageSizeY = 25;
        this.previousObstaclePage.imageXOffset = -145;

        this.nextTankPage.image = "icons/forward.png";
        this.nextTankPage.imageSizeX = 25;
        this.nextTankPage.imageSizeY = 25;
        this.nextTankPage.imageXOffset = 145;

        this.previousTankPage.image = "icons/back.png";
        this.previousTankPage.imageSizeX = 25;
        this.previousTankPage.imageSizeY = 25;
        this.previousTankPage.imageXOffset = -145;

        this.editTank.imageSizeX = 25;
        this.editTank.imageSizeY = 25;
        this.editTank.fullInfo = true;
    }

    public static void minimizeTankGlow(Tank t, ButtonObject b)
    {
        double prevGlow = t.glowSize;
        double prevLight = t.lightSize;
        t.glowSize = Math.min(t.glowSize, 5);
        t.lightSize = Math.min(t.lightSize, 5);
        t.drawForInterface(b.posX, b.posY);
        t.glowSize = prevGlow;
        t.lightSize = prevLight;
    }

    public static void loadSelectors(GameObject o, OverlayObjectMenu menu)
    {
        loadSelectors(o, menu, menu != null ? menu.editor : null);
    }

    public static void loadSelectors(GameObject o, OverlayObjectMenu menu, ScreenLevelEditor editor)
    {
        leftButton = null;
        rightButton = null;

        o.initSelectors(editor);

        o.forAllSelectors(s ->
        {
            LevelEditorSelector<?> s1 = ScreenLevelEditor.selectors.get(s.id);
            s.objectMenu = menu;
            s.gameObject = o;

            if (s1 == null)
                ScreenLevelEditor.selectors.put(s.id, s);
            else
                s.cloneProperties(s1);

            s.addShortcutButton();

            if (s.position == LevelEditorSelector.Position.object_menu_left)
            {
                leftButton = s.button;
                leftSelector = s;
                loadButton(leftButton, s);
            }

            if (s.position == LevelEditorSelector.Position.object_menu_right)
            {
                rightButton = s.button;
                rightSelector = s;
                loadButton(rightButton, s);
            }
        });
    }

    public static void loadButton(Button b, LevelEditorSelector<?> s)
    {
        b.setPosition(Drawing.drawing.interfaceSizeX / 2 + (s.position == Position.object_menu_left ? -380 : 380), Drawing.drawing.interfaceSizeY / 2 + 240);
        s.button = b;
        s.load();
    }

    public void update()
    {
        this.placePlayer.enabled = (ScreenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.playerTank);
        this.placeEnemy.enabled = (ScreenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.enemyTank);
        this.placeObstacle.enabled = (ScreenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.obstacle);

        this.exitObjectMenu.update();

        this.placePlayer.update();
        this.placeEnemy.update();
        this.placeObstacle.update();

        if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            this.playerSpawnsButton.enabled = editor.movePlayer;
            this.movePlayerButton.enabled = !editor.movePlayer;

            this.playerSpawnsButton.update();
            this.movePlayerButton.update();
        }
        else if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            for (int i = 0; i < this.tankButtons.size(); i++)
            {
                Button b = this.tankButtons.get(i);
                b.enabled = editor.tankNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == editor.tankPage)
                    b.update();
            }

            nextTankPage.enabled = (this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > editor.tankPage;
            previousTankPage.enabled = editor.tankPage > 0;

            if (this.tankButtons.size() > this.objectButtonRows * this.objectButtonCols)
            {
                nextTankPage.update();
                previousTankPage.update();
            }

            sort.update();
        }
        else if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
        {
            for (int i = 0; i < this.obstacleButtons.size(); i++)
            {
                this.obstacleButtons.get(i).enabled = editor.obstacleNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == editor.obstaclePage)
                    this.obstacleButtons.get(i).update();
            }

            if ((this.obstacleButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > editor.obstaclePage)
                nextObstaclePage.update();

            if (editor.obstaclePage > 0)
                previousObstaclePage.update();

            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
                this.editStartingHeight.update();
        }

        if (leftButton != null)
            leftButton.update();

        if (rightButton != null)
            rightButton.update();

        if (editor.tankNum >= Game.registryTank.tankEntries.size())
        {
            Button b = this.tankButtons.get(editor.tankNum);
            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
                this.editTank.image = "icons/delete.png";
            else
                this.editTank.image = "icons/pencil.png";

            this.editTank.posX = b.posX + 35;
            this.editTank.posY = b.posY + 35;
            this.editTank.update();
        }

        (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle ? editor.mouseObstacle : editor.mouseTank)
                .forAllSelectors(s ->
                {
                    if (s.keybind != null && s.keybind.isValid())
                    {
                        s.keybind.invalidate();
                        s.onSelect();
                    }
                });

        super.update();
    }

    public void draw()
    {
        super.draw();

        if (Game.screen != this)
            return;

        Drawing.drawing.setColor(this.editor.fontBrightness, this.editor.fontBrightness, this.editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 240, "Object menu");

        this.placePlayer.draw();
        this.placeEnemy.draw();
        this.placeObstacle.draw();

        this.exitObjectMenu.draw();

        if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            this.playerSpawnsButton.draw();
            this.movePlayerButton.draw();

            if (this.editor.movePlayer)
                this.drawMobileTooltip(this.movePlayerButton.hoverTextRawTranslated);
            else
                this.drawMobileTooltip(this.playerSpawnsButton.hoverTextRawTranslated);

        }
        else if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            if (this.tankButtons.size() > this.objectButtonRows * this.objectButtonCols)
            {
                nextTankPage.draw();
                previousTankPage.draw();
            }

            boolean found = false;

            for (int i = tankButtons.size() - 1; i >= 0; i--)
            {
                if (i / (objectButtonCols * objectButtonRows) == editor.tankPage)
                {
                    Button b = tankButtons.get(i);
                    if (b.isDragging)
                        continue;

                    if (editor.tankNum >= Game.registryTank.tankEntries.size() && !tankButtons.get(i).enabled && tankButtons.get(i) instanceof ButtonObject b1)
                    {
                        if (this.editTank.selected)
                            b1.tempDisableHover = true;

                        b1.drawBeforeTooltip = drawEditTank;
                    }

                    b.draw();

                    if (!found && i < tankButtons.size() - 1)
                    {
                        Button dragged = Panel.draggedButton;
                        if (dragged != null && Game.lessThan(true, b.posY - b.sizeY / 2, dragged.posY, b.posY + b.sizeY / 2))
                        {
                            if (b.posX < dragged.posX)
                            {
                                found = true;
                                draggedIndex = i - Game.registryTank.tankEntries.size() + 1;
                                Drawing.drawing.setColor(255, 128, 50, 128);
                                Drawing.drawing.fillInterfaceRect(b.posX + b.sizeX / 2 + 15.5, b.posY, 6, b.sizeY);
                            }
                            else if (i % objectButtonCols == 0 && b.posX > dragged.posX)
                            {
                                found = true;
                                draggedIndex = i - Game.registryTank.tankEntries.size();
                                Drawing.drawing.setColor(255, 128, 50, 128);
                                Drawing.drawing.fillInterfaceRect(b.posX - b.sizeX / 2 - 15.5, b.posY, 6, b.sizeY);
                            }
                        }
                    }
                }
            }

            if (Panel.draggedButton != null)
                Panel.draggedButton.draw();

            sort.draw();
            this.drawMobileTooltip(this.tankButtons.get(editor.tankNum).hoverTextRawTranslated);
        }
        else if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
        {
            if ((this.obstacleButtons.size() - 1) / (objectButtonRows * objectButtonCols) > editor.obstaclePage)
                nextObstaclePage.draw();

            if (editor.obstaclePage > 0)
                previousObstaclePage.draw();

            for (int i = this.obstacleButtons.size() - 1; i >= 0; i--)
            {
                if (i / (this.objectButtonCols * this.objectButtonRows) == editor.obstaclePage)
                    this.obstacleButtons.get(i).draw();
            }

            if (!editor.mouseObstacle.isSurfaceTile && Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
            {
                this.editStartingHeight.setText("Starting height: %.1f", editor.mouseObstacleStartHeight);
                this.editStartingHeight.draw();
            }

            this.drawMobileTooltip(this.obstacleButtons.get(this.editor.obstacleNum).hoverTextRawTranslated);
        }

        if (leftButton != null)
            leftButton.draw();

        if (rightButton != null)
            rightButton.draw();
    }

    public static void saveSelectors(ScreenLevelEditor e)
    {
        GameObject o = ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle ? e.mouseObstacle : e.mouseTank;
        o.forAllSelectors(s -> ScreenLevelEditor.selectors.put(s.id, s));
    }

    public void drawMobileTooltip(String text)
    {
        if (!Game.game.window.touchscreen)
            return;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY - 300, 1120, 60);

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 300, text.replace("---", " "));
    }

    @Override
    public void load()
    {
        if (ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
            editor.mouseObstacle.forAllSelectors(LevelEditorSelector::load);
        else
            editor.mouseTank.forAllSelectors(LevelEditorSelector::load);
    }

    @Override
    public void addTank(TankAIControlled t)
    {
        this.editor.level.customTanks.add(t);
        this.editor.tankNum = this.editor.level.customTanks.size() + Game.registryTank.tankEntries.size() - 1;
        this.editor.refreshMouseTank();
    }

    @Override
    public void removeTank(TankAIControlled t)
    {
        this.editor.level.customTanks.remove(t);
        ArrayList<EditorAction> actions = new ArrayList<>();

        for (int i = 0; i < Game.movables.size(); i++)
        {
            Movable m = Game.movables.get(i);
            if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(t.name))
            {
                actions.add(new EditorAction.ActionTank((Tank) m, false));
                Game.movables.remove(i);
                i--;
            }
        }

        this.editor.undoActions.add(new EditorAction.ActionDeleteCustomTank(this.editor, actions, t));
    }

    @Override
    public void refreshTanks(TankAIControlled t)
    {
        Game.screen = new OverlayObjectMenu(this.previous, this.editor);

        String name = this.editor.mouseTank.name;

        if (this.editor.mouseTank instanceof TankAIControlled)
        {
            for (Movable m : Game.movables)
            {
                if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(name))
                    t.cloneProperties((TankAIControlled) m);
            }
        }

        if (this.editor.tankNum >= this.editor.level.customTanks.size() + Game.registryTank.tankEntries.size())
            this.editor.tankNum--;

        this.editor.refreshMouseTank();
    }

    public int compareTo(TankAIControlled t1, TankAIControlled t2)
    {
        int i = Double.compare(t1.size, t2.size);
        if (i != 0)
            return i;
        if (t1.name == null || t2.name == null)
            return 0;

        return t1.name.compareTo(t2.name);
    }

    public void finishDrag(int i, boolean canceled)
    {
        if (draggedIndex == i || canceled)
            return;

        if (i < draggedIndex)
            draggedIndex--;

        draggedIndex = Math.max(0, draggedIndex);

        TankAIControlled t = this.editor.level.customTanks.remove(i);
        this.editor.level.customTanks.add(draggedIndex, t);
        refreshTanks(t);
    }
}
