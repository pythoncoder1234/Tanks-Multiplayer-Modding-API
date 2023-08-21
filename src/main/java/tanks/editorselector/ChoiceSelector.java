package tanks.editorselector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.ButtonList;
import tanks.gui.screen.leveleditor.OverlayChoiceSelector;

import java.util.ArrayList;

public abstract class ChoiceSelector<T extends GameObject, V> extends LevelEditorSelector<T>
{
    public ArrayList<V> choices = new ArrayList<>();
    public V selectedChoice;
    public ButtonList buttonList;
    public int selectedIndex = 0;
    public boolean addNoneChoice = false;

    @Override
    public void onSelect()
    {
        Game.screen = new OverlayChoiceSelector(Game.screen, editor, this);
    }

    @Override
    public String getMetadata()
    {
        return choiceToString(selectedChoice);
    }

    @Override
    public void setMetadata(String data)
    {
        int i = 0;
        for (V choice : choices)
        {
            if (data.equals(choiceToString(choice)))
                setChoice(i);
            i++;
        }
    }

    @Override
    public void changeMetadata(int add)
    {
        selectedIndex += add;
        setChoice(selectedIndex);
        syncProperties();
    }

    @Override
    public void load()
    {
        this.button.setText(this.buttonText, choiceToString(selectedChoice));
    }

    public String choiceToString(V choice)
    {
        return choice != null ? choice.toString() : "";
    }

    public void setChoice(int index)
    {
        setChoice(index, true);
    }

    public void setChoice(int index, boolean modify)
    {
        if (modify && selectedIndex != index)
            modified = true;

        index = (index + (!addNoneChoice ? choices.size() : 0)) % choices.size();

        if (addNoneChoice && index < -1)
            index = choices.size() - 1;

        selectedIndex = index;

        if (index > -1)
            selectedChoice = choices.get(index);
        else
            selectedChoice = null;

        syncProperties();
    }
}
