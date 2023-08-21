package tanks.editorselector;

import tanks.Consumer;
import tanks.GameObject;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.leveleditor.OverlayObjectMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;

/**
 * A selector that is added to <code>GameObject</code>s.
 *
 * @param <T> the type of <code>GameObject</code> (<code>Tank</code>, <code>Obstacle</code>, etc.)
 *            that the selector is applied to.
 */
public abstract class LevelEditorSelector<T extends GameObject> implements Cloneable
{
    public static ArrayList<Consumer<GameObject>> addSelFuncRegistry = new ArrayList<>();
    public boolean init = false;
    /**
     * Whether the selector has been modified.
     */
    public boolean modified = false;
    public Position position;
    public Position shortcutPos = Position.editor_bottom_right;
    public GameObject gameObject;
    public ScreenLevelEditor editor;
    public OverlayObjectMenu objectMenu;
    public String id = "";
    public String title = "";
    public String description = null;
    /**
     * The result of {@link #getButton()} is stored in this variable.
     */
    public Button button;
    public String buttonText = "";
    public String image = null;
    /**
     * The result of {@link #addShortcutButton()} is stored in this variable.
     */
    public ScreenLevelEditor.EditorButton shortcutButton;
    public InputBindingGroup keybind = null;

    /**
     * Registers the function in the <code>func</code> parameter to be called
     * whenever a <code>GameObject</code> is instantiated and its selectors are added.
     */
    @SuppressWarnings("unused")
    public static void onAddSelector(Consumer<GameObject> func)
    {
        addSelFuncRegistry.add(func);
    }

    public void init()
    {

    }

    public abstract void onSelect();

    /**
     * The button to display in the selector's specified position.
     */
    public Button getButton()
    {
        Button b = new Button(0, 0, editor.objWidth, editor.objHeight, "", this::onSelect);

        b.imageXOffset = -155;
        b.imageSizeX = 30;
        b.imageSizeY = 30;
        b.image = image;

        return b;
    }

    public void addShortcutButton()
    {
        shortcutButton = new ScreenLevelEditor.EditorButton(getLocation(shortcutPos), image.replace("icons/", ""),
                50, 50, this::onShortcut, () -> false, this::gameObjectSelected, description, keybind);

        editor.buttons.refreshButtons();

        if (position != Position.object_menu_left && position != Position.object_menu_right)
            button = shortcutButton;
    }

    public void onShortcut()
    {
        editor.paused = true;
        onSelect();
    }

    /**
     * Syncs the changes between the selector and the selector's game object.
     */
    public void syncProperties()
    {
        //noinspection unchecked
        syncProperties((T) this.gameObject);
    }

    /**
     * Syncs the changes between the selector and the game object.
     */
    public void syncProperties(T o)
    {
        setProperty(o);
        o.onPropertySet(this);
    }

    public abstract void setProperty(T o);

    public abstract String getMetadata();

    public abstract void setMetadata(String data);

    /**
     * Don't forget to call {@link #syncProperties(GameObject) setPropertyWithUpdate}.<br>
     * The <code>add</code> parameter takes two values:<br>
     * -1: If the editor's prev. meta keybind was pressed or if Shift+RMB was pressed.<br>
     * 1: If the editor's next meta keybind was pressed or if RMB was pressed.
     */
    public abstract void changeMetadata(int add);

    public void load()
    {
    }

    public void cloneProperties(LevelEditorSelector<T> s)
    {
        this.editor = s.editor;
        this.objectMenu = s.objectMenu;
        this.button = s.button;
        this.shortcutButton = s.shortcutButton;
        this.modified = true;

        this.setMetadata(s.getMetadata());
        this.syncProperties();

        if (this.editor.addedShortcutButtons.add(this.id))
            this.addShortcutButton();
    }

    public void baseInit()
    {
        this.syncProperties();

        if (this.init)
            return;

        this.init = true;

        this.init();

        if (!this.image.startsWith("icons/"))
            this.image = "icons/" + this.image;

        if (description == null)
            description = title + " (" + keybind.getInputs() + ")";
    }

    public boolean gameObjectSelected()
    {
        if (gameObject instanceof Obstacle)
            return ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle && editor.mouseObstacle.getSelector(this.id) != null;
        else
            return ScreenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.obstacle && editor.mouseTank.getSelector(this.id) != null;
    }

    public ArrayList<ScreenLevelEditor.EditorButton> getLocation(Position p)
    {
        if (p == Position.editor_top_left)
            return editor.buttons.topLeft;

        if (p == Position.editor_top_right)
            return editor.buttons.topRight;

        if (p == Position.editor_bottom_left)
            return editor.buttons.bottomLeft;

        if (p == Position.editor_bottom_right)
            return editor.buttons.bottomRight;

        return null;
    }

    @Override
    public LevelEditorSelector<T> clone()
    {
        try
        {
            return (LevelEditorSelector<T>) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }

    public enum Position
    {editor_top_left, editor_top_right, editor_bottom_left, editor_bottom_right, object_menu_left, object_menu_right}
}
