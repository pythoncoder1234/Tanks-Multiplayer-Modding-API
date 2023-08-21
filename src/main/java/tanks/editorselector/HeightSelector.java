package tanks.editorselector;

import tanks.obstacle.Obstacle;

public class HeightSelector extends NumberSelector<Obstacle>
{
    public HeightSelector()
    {
        this.min = 0;
        this.max = Obstacle.default_max_height;
        this.step = 0.5;
        this.image = "obstacle_height.png";
    }

    @Override
    public void setProperty(Obstacle o)
    {
        o.stackHeight = this.number;
    }
}
