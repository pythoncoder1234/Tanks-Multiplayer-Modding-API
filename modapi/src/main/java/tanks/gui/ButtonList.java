package tanks.gui;

import basewindow.InputCodes;
import tanks.*;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ButtonList
{
    public ArrayList<Button> buttons;

    public ArrayList<Button> upButtons = new ArrayList<>();
    public ArrayList<Button> downButtons = new ArrayList<>();

    public boolean arrowsEnabled = false;
    public boolean reorder = false;
    public boolean centerAlign = true;

    public int page;

    public int rows = 6;
    public int columns = 3;

    public int dragIndex;
    public BiConsumer<Integer, Integer> reorderBehavior;

    public double xOffset;
    public double yOffset;

    public double controlsYOffset;

    public boolean indexPrefix = false;

    public double objWidth = 350;
    public double objHeight = 40;
    public double objXSpace = 380;
    public double objYSpace = 60;

    public double imageR = 255;
    public double imageG = 255;
    public double imageB = 255;

    public boolean translate = false;

    public boolean hideText = false;

    Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + Drawing.drawing.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Next page", () -> page++);

    Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - Drawing.drawing.objXSpace / 2, 0, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Previous page", () -> page--);

    Button first = new Button(Drawing.drawing.interfaceSizeX / 2 - Drawing.drawing.objXSpace - Drawing.drawing.objHeight * 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.objHeight, Drawing.drawing.objHeight, "", () -> page = 0);

    Button last = new Button(Drawing.drawing.interfaceSizeX / 2 + Drawing.drawing.objXSpace + Drawing.drawing.objHeight * 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.objHeight, this.objHeight, "", () -> page = (buttons.size() - 1) / rows / columns);

    public ButtonList(ArrayList<Button> buttons, int page, double xOffset, double yOffset)
    {
        this.buttons = buttons;
        this.page = page;

        this.xOffset = xOffset;
        this.yOffset = yOffset;

        this.sortButtons();
    }

    public ButtonList(ArrayList<Button> buttons, int page, double xOffset, double yOffset, double controlsYOffset)
    {
        this.buttons = buttons;
        this.page = page;

        this.xOffset = xOffset;
        this.yOffset = yOffset;

        this.controlsYOffset = controlsYOffset;

        this.sortButtons();
    }

    protected ButtonList()
    {

    }

    public void sortButtons()
    {
        this.next.posX = Drawing.drawing.interfaceSizeX / 2 + Drawing.drawing.objXSpace / 2 + xOffset;
        this.next.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * Drawing.drawing.objYSpace + yOffset + controlsYOffset;

        this.previous.posX = Drawing.drawing.interfaceSizeX / 2 - Drawing.drawing.objXSpace / 2 + xOffset;
        this.previous.posY = Drawing.drawing.interfaceSizeY / 2 + ((rows + 3) / 2.0) * Drawing.drawing.objYSpace + yOffset + controlsYOffset;

        this.last.posX = this.next.posX + Drawing.drawing.objXSpace / 2 + Drawing.drawing.objHeight / 2;
        this.last.posY = this.next.posY;

        this.first.posX = this.previous.posX - Drawing.drawing.objXSpace / 2 - Drawing.drawing.objHeight / 2;
        this.first.posY = this.previous.posY;

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;

        this.last.image = "icons/last.png";
        this.last.imageSizeX = 20;
        this.last.imageSizeY = 20;
        this.last.imageXOffset = 0;

        this.first.image = "icons/first.png";
        this.first.imageSizeX = 20;
        this.first.imageSizeY = 20;
        this.first.imageXOffset = 0;

        for (int i = 0; i < buttons.size(); i++)
        {
            int page = i / (rows * columns);

            int entries = rows * columns + Math.min(0, buttons.size() - (page + 1) * rows * columns);
            int cols = entries / rows + Math.min(1, entries % rows);

            double offset = -this.objXSpace / 2 * (cols - 1);

            Button b = buttons.get(i);

            if (centerAlign)
            {
                b.posX = Drawing.drawing.interfaceSizeX / 2 + offset + ((i / rows) % columns) * this.objXSpace + xOffset;
                b.posY = Drawing.drawing.interfaceSizeY / 2 + yOffset + (i % rows - (rows - 1) / 2.0) * this.objYSpace;
            }
            else
            {
                b.posX = Drawing.drawing.interfaceSizeX / 2 + xOffset + this.objXSpace * (i % columns - columns / 2. + 0.5);
                b.posY = Drawing.drawing.interfaceSizeY / 2 + yOffset + this.objYSpace * ((i % (rows * columns)) / columns - rows / 2. + 0.5);
            }

            b.sizeX = this.objWidth;
            b.sizeY = this.objHeight;
            b.translated = this.translate;
            b.imageR = this.imageR;
            b.imageG = this.imageG;
            b.imageB = this.imageB;

            if (hideText)
                b.text = "";
        }

        if (this.arrowsEnabled)
            this.setupArrows();
    }

    public void setupArrows()
    {
        this.arrowsEnabled = true;

        this.upButtons.clear();
        this.downButtons.clear();

        for (int i = 0; i < this.buttons.size(); i++)
        {
            Button b = this.buttons.get(i);

            int finalI = i;

            Button up = new Button(b.posX + b.sizeX / 2 - b.sizeY / 2 - b.sizeY, b.posY, b.sizeY * 0.8, b.sizeY * 0.8, "", () -> reorderBehavior.accept(finalI - 1, finalI));

            up.image = "icons/arrow_up.png";
            up.imageSizeX = 15;
            up.imageSizeY = 15;
            this.upButtons.add(up);

            Button down = new Button(b.posX + b.sizeX / 2 - b.sizeY / 2, b.posY, b.sizeY * 0.8, b.sizeY * 0.8, "", () -> reorderBehavior.accept(finalI + 1, finalI));

            down.image = "icons/arrow_down.png";
            down.imageSizeX = 15;
            down.imageSizeY = 15;
            this.downButtons.add(down);
        }
    }

    public void update()
    {
        while (page * rows * columns >= buttons.size() && page > 0)
            page--;

        if (previous.enabled && Game.game.window.validPressedKeys.contains(InputCodes.KEY_LEFT))
        {
            Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_LEFT);
            Game.game.input.moveLeft.invalidate();

            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
                first.function.run();
            else
                previous.function.run();
        }

        if (next.enabled && Game.game.window.validPressedKeys.contains(InputCodes.KEY_RIGHT))
        {
            Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_RIGHT);
            Game.game.input.moveRight.invalidate();

            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
                last.function.run();
            else
                next.function.run();
        }

        if (this.arrowsEnabled && !this.buttons.isEmpty())
        {
            upButtons.get(0).enabled = false;
            downButtons.get(downButtons.size() - 1).enabled = false;
        }

        dragIndex = Integer.MAX_VALUE;

        for (int i = page * rows * columns; i < Math.min((page + 1) * rows * columns, buttons.size()); i++)
        {
            Button b = buttons.get(i);

            if (this.arrowsEnabled)
                b.enabled = !this.reorder;

            if (this.reorder)
            {
                upButtons.get(i).update();
                downButtons.get(i).update();

                if (Panel.draggedButton != null && Game.lessThan(b.posX - b.sizeX / 2, Panel.draggedButton.posX, b.posX + b.sizeX / 2) && Panel.draggedButton.posY < b.posY)
                    dragIndex = Math.min(dragIndex, i);
            }

            b.update();
        }

        previous.enabled = page > 0;
        next.enabled = buttons.size() > (1 + page) * rows * columns;

        if (rows * columns < buttons.size())
        {
            previous.update();
            next.update();

            if ((buttons.size() - 1) / rows / columns >= 2)
            {
                last.update();
                first.update();
            }
        }
    }

    public void draw()
    {
        previous.enabled = page > 0;
        next.enabled = buttons.size() > (1 + page) * rows * columns;

        first.enabled = previous.enabled;
        last.enabled = next.enabled;

        if (this.arrowsEnabled && !this.buttons.isEmpty())
        {
            upButtons.get(0).enabled = false;
            downButtons.get(downButtons.size() - 1).enabled = false;
        }

        if (rows * columns < buttons.size())
        {
            Drawing.drawing.setInterfaceFontSize(Drawing.drawing.objHeight * 0.6);

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + xOffset, previous.posY - Drawing.drawing.objYSpace * 0.75,
                    Translation.translate("Page %d of %d", (page + 1), (buttons.size() / (rows * columns) + Math.min(1, buttons.size() % (rows * columns)))));

            previous.draw();
            next.draw();

            if ((buttons.size() - 1) / rows / columns >= 2)
            {
                last.draw();
                first.draw();
            }
        }

        for (int i = Math.min(page * rows * columns + rows * columns, buttons.size()) - 1; i >= page * rows * columns; i--)
        {
            Button b = buttons.get(i);
            String n = b.text;

            if (this.arrowsEnabled)
                buttons.get(i).enabled = !this.reorder;

            if (indexPrefix)
                b.text = (i + 1) + ". " + n;

            b.draw();

            if (indexPrefix)
                b.text = n;

            if (this.reorder)
            {
                upButtons.get(i).draw();
                downButtons.get(i).draw();
            }
        }
    }

    public SavedFilesList clone()
    {
        SavedFilesList s = new SavedFilesList();
        s.page = this.page;
        s.xOffset = this.xOffset;
        s.yOffset = this.yOffset;
        s.buttons = new ArrayList<>();
        s.buttons.addAll(this.buttons);

        return s;
    }

    public void filter(String s)
    {
        if (s.isEmpty())
            return;

        for (int i = 0; i < this.buttons.size(); i++)
        {
            if (!buttons.get(i).text.toLowerCase().contains(s.toLowerCase()))
            {
                buttons.remove(i);
                i--;
            }
        }
    }
}
