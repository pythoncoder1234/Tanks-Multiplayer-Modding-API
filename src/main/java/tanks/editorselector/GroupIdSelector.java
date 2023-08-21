package tanks.editorselector;

import tanks.Game;
import tanks.obstacle.Obstacle;

public class GroupIdSelector extends NumberSelector<Obstacle>
{
    public void init()
    {
        this.keybind = Game.game.input.editorGroupID;
        this.id = "group_id";
        this.title = "Group ID";
        this.format = "%.0f";
        this.buttonText = "Group ID: %.0f";
        this.image = "id.png";
        this.min = 0;
    }

    @Override
    public void setProperty(Obstacle o)
    {
        o.groupID = (int) number;
    }
}