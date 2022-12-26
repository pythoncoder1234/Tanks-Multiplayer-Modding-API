package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Colors;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;

public class ScreenTestColors extends Screen
{
    public ColorTile selectedTile = null;
    public static int colors = 50;
    public static ColorTile[] tiles;
    public static double sizeX;

    TextBoxSlider colorCount = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Colors Shown", new Runnable()
    {
        @Override
        public void run()
        {
            colors = (int) colorCount.value;
            setColorTiles();
        }
    }, 50, 10, 200, 10);

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 4.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenDebug());

    public ScreenTestColors()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (tiles == null)
            setColorTiles();
    }

    @Override
    public void update()
    {
        colorCount.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        for (ColorTile t : tiles)
            t.draw();

        double mx = Drawing.drawing.getMouseX();
        double my = Drawing.drawing.getMouseY();

        int tileIndex = (int) ((mx - tiles[0].posX + sizeX / 2) / sizeX);
        boolean validTile = Game.lessThan(true, this.centerY - this.objYSpace * 3 - 25, my, this.centerY - this.objYSpace * 3 + 25) && Game.lessThan(true, 0, tileIndex, colors-1);

        if (validTile)
            tiles[tileIndex].hoverDim = 50;

        if (Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1) && validTile)
            selectedTile = tiles[tileIndex];

        if (selectedTile != null)
            selectedTile.hoverDim = 100;

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(32);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 300, "Rainbow Color Generator");
        Game.game.window.shapeRenderer.drawRect(tiles[0].posX - sizeX / 2, this.centerY - this.objYSpace * 3 - 25, tiles[tiles.length-1].posX - tiles[0].posX + sizeX, 50);

        colorCount.draw();
        back.draw();

        drawColorModal();
    }

    public void drawColorModal()
    {
        Drawing.drawing.drawPopup(this.centerX, this.centerY, this.objXSpace * 1.25, this.objYSpace * 3.25, 20, 10);

        if (selectedTile == null)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace / 3, "Click on a tile to display");
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace / 3, "information about it!");
        }
        else
        {
            Drawing.drawing.setColor(200, 200, 200);
            Drawing.drawing.displayUncenteredInterfaceText(this.centerX - this.objXSpace / 2, this.centerY - 37.5,
                    "Fraction:" + Colors.white + " %s", selectedTile.fracString);
            Drawing.drawing.displayUncenteredInterfaceText(this.centerX - this.objXSpace / 2, this.centerY + 10,
                    "RGB:" + Colors.white + " %s", selectedTile.colorString);

            Drawing.drawing.setColor(selectedTile.colorR, selectedTile.colorG, selectedTile.colorB);
            Drawing.drawing.fillRect(this.centerX + this.objXSpace / 2.5, this.centerY, 75, 75);
            Drawing.drawing.setColor(50, 50, 50);
            Drawing.drawing.drawRect(this.centerX + this.objXSpace / 2.5, this.centerY, 75, 75, 2, 2);
        }
    }

    public void setColorTiles()
    {
        ColorTile closestTile = null;
        tiles = new ColorTile[colors];

        sizeX = 800. / colors;
        for (int i = 0; i < colors; i++)
        {
            double frac = 1. / colors * i;
            tiles[i] = new ColorTile(frac, i, sizeX, colors / 2.);

            if (selectedTile != null)
            {
                if (closestTile == null || Math.abs(tiles[i].frac - selectedTile.frac) < Math.abs(closestTile.frac - selectedTile.frac))
                    closestTile = tiles[i];
            }
        }

        if (closestTile != null)
            selectedTile = closestTile;
    }

    public static class ColorTile
    {
        public double colorR;
        public double colorG;
        public double colorB;
        public double frac;
        public String fracString;
        public String colorString;

        public double posX;
        public double sizeX;

        public double hoverDim = 0;

        public ColorTile(double frac, double i, double sizeX, double halfNum)
        {
            double[] col = Game.getRainbowColor(frac);
            this.colorR = col[0];
            this.colorG = col[1];
            this.colorB = col[2];

            this.frac = frac;
            this.fracString = String.format("%.2f", frac);
            this.colorString = String.format("%.0f", this.colorR) + ", " + String.format("%.0f", this.colorG) + ", " + String.format("%.0f", this.colorB);

            this.posX = i * sizeX + (Game.screen.centerX - halfNum * sizeX);
            this.sizeX = sizeX;
        }

        public void draw()
        {
            Drawing.drawing.setColor(this.colorR - hoverDim, this.colorG - hoverDim, this.colorB - hoverDim);
            Drawing.drawing.fillRect(this.posX, Game.screen.centerY - Game.screen.objYSpace * 3, this.sizeX, 50);

            hoverDim = 0;
        }
    }
}
