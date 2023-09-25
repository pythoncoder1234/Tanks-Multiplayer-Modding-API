package tanks.editorselector;

import tanks.Game;
import tanks.gui.screen.leveleditor.OverlaySelectBlockHeight;
import tanks.obstacle.Obstacle;

public class StackHeightSelector extends NumberSelector<Obstacle>
{
    public void init()
    {
        this.id = "stack_height";
        this.title = "Block height";
        this.objectProperty = "stackHeight";

        this.min = 0.5;
        this.max = Obstacle.default_max_height;
        this.number = 1;
        this.step = 0.5;
        this.image = "obstacle_height.png";
        this.buttonText = "Block height: %.1f";
        this.keybind = Game.game.input.editorHeight;
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectBlockHeight(Game.screen, editor, this);
    }
}
