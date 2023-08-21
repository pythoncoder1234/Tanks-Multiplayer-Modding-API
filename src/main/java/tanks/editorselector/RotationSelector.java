package tanks.editorselector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlayRotationSelector;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;

public class RotationSelector<T extends GameObject> extends NumberSelector<T>
{
    @Override
    public void init()
    {
        this.id = "rotation";
        this.keybind = Game.game.input.editorRotate;

        if (gameObject instanceof Tank)
        {
            this.image = "rotate_tank.png";
            this.title = "Select tank orientation";
            this.buttonText = "Tank orientation";
        }
        else
        {
            this.image = "rotate_obstacle.png";
            this.title = "Select obstacle orientation";
            this.buttonText = "Obstacle rotation";
        }
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlayRotationSelector(Game.screen, editor, this);
    }

    @Override
    public void setProperty(T o)
    {
        if (o instanceof Obstacle)
            ((Obstacle) o).rotation = this.number * Math.PI / 2;
        else if (o instanceof Tank)
            ((Tank) o).angle = ((Tank) o).orientation = this.number * Math.PI / 2;
    }
}
