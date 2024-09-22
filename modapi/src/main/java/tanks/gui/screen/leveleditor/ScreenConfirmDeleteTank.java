package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.TankReferenceSolver;
import tanks.gui.Button;
import tanks.gui.screen.IConditionalOverlayScreen;
import tanks.gui.screen.Screen;
import tanks.tank.TankAIControlled;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScreenConfirmDeleteTank extends Screen implements IConditionalOverlayScreen
{
    public Screen previous = Game.screen;
    public ArrayList<String> textLines;
    public ArrayList<TankAIControlled> referredBy;
    public List<Double> widths;
    public double sumWidths = 0;

    public TankAIControlled tank;
    public Runnable onDelete;

    public Button delete, cancel;

    public ScreenConfirmDeleteTank(TankAIControlled tank, Runnable onDelete)
    {
        this.music = previous.music;
        this.musicID = previous.musicID;

        this.tank = tank;
        this.onDelete = onDelete;

        this.allowClose = previous.allowClose;

        referredBy = TankReferenceSolver.getReferences(tank);
        if (!referredBy.isEmpty())
        {
            int maxSize = 5;
            int extra = referredBy.size() - maxSize;
            String s = referredBy.size() != 1 ? "s" : "", s2 = extra != 1 ? "s" : "";
            Supplier<Stream<String>> supplier = () -> referredBy.stream().limit(maxSize).map(t -> t.name);
            widths = supplier.get().map(str -> Game.game.window.fontRenderer.getStringSizeX(this.textSize / 36, str)).toList();
            for (double width : widths)
                sumWidths += width + 15;

            String text = "Tank \"" + tank.name + "\" is used in " + referredBy.size() + " other tank" + s + ": \n " +
                    supplier.get().collect(Collectors.joining(", ")) +
                    (extra > 0 ? " \n and " + extra + " more tank" + s2 : "");

            textLines = Drawing.drawing.wrapText(text, Drawing.drawing.interfaceSizeX * 0.9, this.titleSize);
        }
        else
        {
            textLines = new ArrayList<>();
            textLines.add("No references to tank \"" + tank.name + "\" were found.");
        }

        this.delete = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Delete", onDelete);
        this.cancel = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Cancel", () -> Game.screen = previous);
    }

    /** Displays a "confirm deletion" popup if references to <code>tank</code>
     * are found, running <code>onDelete</code> if the delete button is clicked.
     * Otherwise, it runs <code>onDelete</code> immediately. */
    public static void confirmDelete(TankAIControlled tank, Runnable onDelete)
    {
        if (TankReferenceSolver.getReferences(tank).isEmpty())
            onDelete.run();
        else
            Game.screen = new ScreenConfirmDeleteTank(tank, onDelete);
    }

    @Override
    public void update()
    {
        delete.update();
        cancel.update();
    }

    @Override
    public void draw()
    {
        if (previous instanceof ScreenLevelEditorOverlay e)
            e.editor.draw();
        else
            this.drawDefaultBackground();

        boolean p = previous instanceof ScreenLevelEditorOverlay;
        int brightness = Level.isDark(true) || !p ? 255 : 0;
        if (!p)
        {
            Drawing.drawing.setColor(0, 0, 0, 128);
            Drawing.drawing.drawPopup(this.centerX, this.centerY, Drawing.drawing.interfaceSizeX * 0.75, Drawing.drawing.interfaceSizeY * 0.6, 10, 5);
        }

        Drawing.drawing.setColor(brightness, brightness, brightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Confirm deletion");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        for (int i = 0; i < textLines.size(); i++)
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 100 + i * (this.textSize + 20) + (i >= 1 ? 90 : 0), textLines.get(i));

        double sum = widths.get(0) / 2 + 15;
        for (int i = 0; i < Math.min(referredBy.size(), 5); i++)
        {
            if (i > 0) sum += (widths.get(i - 1) + widths.get(i)) / 2 + 15;
            referredBy.get(i).drawForInterface(this.centerX - sumWidths / 2 + sum, this.centerY - 25);
        }

        delete.draw();
        cancel.draw();
    }

    @Override
    public void onAttemptClose()
    {
        previous.onAttemptClose();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        return true;
    }
}
