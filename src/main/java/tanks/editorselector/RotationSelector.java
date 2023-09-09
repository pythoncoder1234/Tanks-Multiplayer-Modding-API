package tanks.editorselector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlayRotationSelector;
import tanks.tank.Tank;

public class RotationSelector<T extends GameObject> extends NumberSelector<T>
{
    @Override
    public void init()
    {
        this.id = "rotation";
        this.keybind = Game.game.input.editorRotate;
        this.loop = true;
        this.format = "%.0f";
        this.min = 0;
        this.max = 4;

        if (gameObject instanceof Tank)
        {
            this.objectProperty = "angle";
            this.image = "rotate_tank.png";
            this.title = "Select tank orientation";
            this.buttonText = "Tank orientation";
        }
        else
        {
            this.objectProperty = "rotation";
            this.image = "rotate_obstacle.png";
            this.title = "Select obstacle orientation";
            this.buttonText = "Obstacle rotation";
        }
    }

    @Override
    public void updateAndDraw()
    {

    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlayRotationSelector(Game.screen, editor, this);
    }
}
