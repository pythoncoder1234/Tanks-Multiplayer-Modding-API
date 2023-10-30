package tanks;

import tanks.editorselector.LevelEditorSelector;
import tanks.editorselector.LevelEditorSelector.Position;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;

import java.util.ArrayList;

public abstract class GameObject implements Cloneable
{
    public ArrayList<LevelEditorSelector<? extends GameObject>> selectors;

    Position[] positions = {Position.object_menu_left, Position.object_menu_right};
    Position extraSelPos /* Position of extra selectors */ = Position.editor_bottom_right;
    int currentPos = 0;

    public void registerSelector(LevelEditorSelector<?>... s)
    {
        if (selectors == null)
            selectors = new ArrayList<>();

        for (LevelEditorSelector<?> s1 : s)
        {
            if (currentPos < positions.length)
            {
                s1.position = positions[currentPos];
                currentPos++;
            }
            else
                s1.position = extraSelPos;

            s1.gameObject = this;
            selectors.add(s1);
        }
    }

    public LevelEditorSelector<?> getSelector(String id)
    {
        if (this.hasCustomSelectors())
        {
            for (LevelEditorSelector<?> s : this.selectors)
            {
                if (id.equals(s.id))
                    return s;
            }
        }

        return null;
    }

    public boolean hasCustomSelectors()
    {
        return selectors != null && !selectors.isEmpty();
    }

    public int selectorCount()
    {
        return selectors != null ? selectors.size() : 0;
    }

    public void forAllSelectors(Consumer<LevelEditorSelector> c)
    {
        if (this.hasCustomSelectors())
        {
            for (LevelEditorSelector<?> s : this.selectors)
                c.accept(s);
        }
    }

    public void registerSelectors()
    {

    }

    public void onPropertySet(LevelEditorSelector<?> s)
    {

    }

    /** Warning: Shallow copy. */
    @Override
    public GameObject clone()
    {
        try
        {
            GameObject clone = (GameObject) super.clone();

            if (clone.hasCustomSelectors())
                clone.selectors = (ArrayList<LevelEditorSelector<? extends GameObject>>) selectors.clone();

            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError();
        }
    }

    public void postInitSelectors()
    {

    }

    public void initSelectors(ScreenLevelEditor editor)
    {
        if (editor == null)
        {
            if (Game.screen instanceof ScreenLevelEditor)
                editor = (ScreenLevelEditor) Game.screen;

            else if (Game.screen instanceof ScreenLevelEditorOverlay)
                editor = ((ScreenLevelEditorOverlay) Game.screen).editor;
        }

        for (Consumer<GameObject> s : LevelEditorSelector.addSelFuncRegistry)
            s.accept(this);

        ScreenLevelEditor editor1 = editor;

        this.forAllSelectors(s ->
        {
            s.gameObject = this;
            s.editor = editor1;

            if (!s.init)
                s.baseInit();

            if (editor1 != null)
                s.button = s.getButton();
        });

        postInitSelectors();
    }

    public void refreshSelectorValue()
    {
        this.forAllSelectors(s -> s.prevObject = s.getPropertyBase());
    }

    public int saveOrder(int index)
    {
        return index;
    }

    public void setMetadata(String s) {}
    public String getMetadata() { return null; }

    public void updateSelectors()
    {
        this.forAllSelectors(LevelEditorSelector::update);
    }
}