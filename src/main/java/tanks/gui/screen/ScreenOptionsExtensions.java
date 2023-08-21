package tanks.gui.screen;

import basewindow.BaseFile;
import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.util.ArrayList;

public class ScreenOptionsExtensions extends ScreenOptionsOverlay
{
    public static boolean modified = false;

    public static ArrayList<String> extensionNames;
    public static boolean[] selectedExtensions;
    public Button description = new Button(this.centerX + 140, this.centerY - this.objYSpace * 5, 40, 40, "",
            "Extension names in §000220000255green §255255255255are saved in the---extension registry, and will be loaded---on the next game launch." +
                    "------If auto loading extensions is enabled, all---extensions in this list will be loaded.");    public Button enabled = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            modified = true;
            Game.enableExtensions = !Game.enableExtensions;
            enabled.setText("Enabled: ", Game.enableExtensions ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });
    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 4.5, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.screen = new ScreenOptionsMisc();

        if (modified)
        {
            ScreenOptions.saveExtensions();
            Game.extensionRegistry.loadRegistry();
        }
    });    public Button autoLoad = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            modified = true;
            Game.autoLoadExtensions = !Game.autoLoadExtensions;
            autoLoad.setText("Auto Load: ", Game.autoLoadExtensions ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    public ScreenOptionsExtensions()
    {
        try
        {
            extensionNames = Game.game.fileManager.getFile(Game.homedir + Game.extensionDir).getSubfiles();
            extensionNames.removeIf(s -> !s.endsWith(".jar"));

            for (int i = 0; i < extensionNames.size(); i++)
            {
                String[] sp = extensionNames.get(i).split("/|\\\\");
                extensionNames.set(i, sp[sp.length - 1]);
            }

            selectedExtensions = new boolean[extensionNames.size()];

            try
            {
                BaseFile in = Game.game.fileManager.getFile(Game.homedir + Game.extensionRegistryPath);
                in.startReading();

                while (in.hasNextLine())
                {
                    String line = in.nextLine();

                    if (line == null || line.length() == 0 || line.startsWith("#"))
                        continue;

                    String[] extensionLine = line.split(",");
                    int i = 0;

                    for (String s : extensionNames)
                    {
                        if (s.equals(extensionLine[0]))
                        {
                            selectedExtensions[i] = true;
                            break;
                        }
                        i++;
                    }
                }

                in.stopReading();
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        enabled.setText("Enabled: ", Game.enableExtensions ? ScreenOptions.onText : ScreenOptions.offText);
        autoLoad.setText("Auto Load: ", Game.autoLoadExtensions ? ScreenOptions.onText : ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        super.update();

        description.update();
        enabled.update();
        autoLoad.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "Extension list");

        Drawing.drawing.setFontSize(this.textSize);

        double mx = Drawing.drawing.getInterfaceMouseX();
        double my = Drawing.drawing.getInterfaceMouseY();
        int i = 0;

        for (String s : extensionNames)
        {
            double x = this.centerX + this.objXSpace / 2 * (i % 2 == 0 ? -1 : 1);
            double y = this.centerY - this.objYSpace * 3 + (i / 2) * 50;
            double sX = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s);

            if (selectedExtensions[i])
                Drawing.drawing.setColor(0, 200, 0);
            else
                Drawing.drawing.setColor(0, 0, 0);

            if (Game.lessThan(x - sX / 2, mx, x + sX / 2) && Game.lessThan(y - 25, my, y + 25))
            {
                Drawing.drawing.setColor(100, Math.min(230, Drawing.drawing.currentColorG + 100), 100);

                if (Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1))
                {
                    selectedExtensions[i] = !selectedExtensions[i];
                    modified = true;
                    Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
                }
            }
            Drawing.drawing.drawInterfaceText(x, y, s);
            i++;
        }

        enabled.draw();
        autoLoad.draw();
        back.draw();
        description.draw();
    }




}
