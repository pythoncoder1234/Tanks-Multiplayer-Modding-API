package com.orangishcat.modapi;

import com.orangishcat.modapi.editor.selector.LevelEditorSelector;
import com.orangishcat.modapi.editor.selector.LevelEditorSelector.Position;
import tanks.Consumer;
import tanks.Game;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GameObject implements Cloneable
{
    public ArrayList<LevelEditorSelector<? extends GameObject>> selectors;

    public double posX, posY, posZ;

    Position[] positions = {Position.object_menu_left, Position.object_menu_right};
    Position extraSelPos /* Position of extra selectors */ = Position.editor_bottom_right;
    int currentPos = 0;

    /** Registers the specified selector. <br>
     * Examples: {@link Tank#registerSelectors()}, {@link Obstacle#registerSelectors()}
     * @see #postInitSelectors() */
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
        if (!this.hasCustomSelectors())
            return null;

        for (LevelEditorSelector<?> s : this.selectors)
        {
            if (id.equals(s.id))
                return s;
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

    public void cloneAllSelectors(GameObject cloneFrom)
    {
        if (!cloneFrom.hasCustomSelectors())
            return;

        AtomicInteger i = new AtomicInteger();
        forAllSelectors(c -> c.cloneProperties(cloneFrom.selectors.get(i.getAndIncrement())));
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

    /**
     * This function is called after the selectors have been registered and initialized.<br>
     * Override to modify selector properties.<br>
     * Examples: {@link TankPlayer#postInitSelectors()}, {@link ObstacleConveyor#postInitSelectors()}
     * */
    public void postInitSelectors()
    {

    }

    public void initSelectors(ScreenLevelEditor editor)
    {
        if (editor == null)
        {
            if (tanks.Game.screen instanceof ScreenLevelEditor)
                editor = (ScreenLevelEditor) tanks.Game.screen;

            else if (tanks.Game.screen instanceof ScreenLevelEditorOverlay)
                editor = ((ScreenLevelEditorOverlay) Game.screen).screenLevelEditor;
        }

        for (Consumer<GameObject> s : LevelEditorSelector.addSelFuncRegistry)
            s.accept(this);

        ScreenLevelEditor editor1 = editor;

        if (!hasCustomSelectors())
            registerSelectors();

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

    /** The save order of selectors; the selector that appears at index {@code saveOrder(i)} is saved at the {@code i}th position.
     *  (0 <= {@code i} < total selector count) */
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