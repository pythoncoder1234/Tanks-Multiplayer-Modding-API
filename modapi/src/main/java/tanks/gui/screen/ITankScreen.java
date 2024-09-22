package tanks.gui.screen;

import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.tank.TankAIControlled;

public interface ITankScreen
{
    ScreenLevelEditor getEditor();

    void addTank(TankAIControlled t);

    void removeTank(TankAIControlled t);

    void refreshTanks(TankAIControlled t);
}
