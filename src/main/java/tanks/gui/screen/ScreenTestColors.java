package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;

public class ScreenTestColors extends Screen
{
    public int colors = 50;
    public ColorTile[] tiles;
    public ColorTile selectedTile = null;

    public double sizeX;

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

        if (Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1))
        {
            Game.game.window.pressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);

            if (validTile)
                selectedTile = tiles[tileIndex];
            else
                selectedTile = null;
        }

        if (selectedTile != null)
            selectedTile.hoverDim = 100;

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(32);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 300, "Rainbow Color Generator");
        Game.game.window.shapeRenderer.drawRect(tiles[0].posX - sizeX / 2, this.centerY - this.objYSpace * 3 - 25, tiles[tiles.length-1].posX - tiles[0].posX + sizeX, 50);

        colorCount.draw();
        back.draw();

        Drawing.drawing.drawPopup(this.centerX, this.centerY, 500, 200);
    }

    public void setColorTiles()
    {
        tiles = new ColorTile[colors];

        sizeX = 800. / colors;
        for (int i = 0; i < colors; i++)
        {
            double frac = 1. / colors * i;
            tiles[i] = new ColorTile(frac, i, sizeX, colors / 2.);
        }
    }

    public static class ColorTile
    {
        public double colorR;
        public double colorG;
        public double colorB;
        public String fracString;

        public double posX;
        public double sizeX;

        public double hoverDim = 0;

        public ColorTile(double frac, double i, double sizeX, double halfNum)
        {
            double[] col = Game.getRainbowColor(frac);
            this.colorR = col[0];
            this.colorG = col[1];
            this.colorB = col[2];

            this.fracString = String.format("%.3f", frac);

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
