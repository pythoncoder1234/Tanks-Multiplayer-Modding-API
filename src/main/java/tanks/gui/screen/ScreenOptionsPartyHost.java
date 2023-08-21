package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;

public class ScreenOptionsPartyHost extends ScreenOptionsOverlay
{
    public static final String anticheatText = "Anticheat: ";
    public static final String disableFriendlyFireText = "Friendly fire: ";

    public static final String weakText = "\u00A7200100000255weak";
    public static final String strongText = "\u00A7000200000255strong";

    public static final String defaultText = "\u00A7000200000255default";
    public static final String disabledText = "\u00A7200000000255off";

    TextBox timer = new TextBox(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Countdown time", new Runnable()
    {
        @Override
        public void run()
        {
            if (timer.inputText.length() == 0)
                timer.inputText = Game.partyStartTime / 100.0 + "";
            else
                Game.partyStartTime = Double.parseDouble(timer.inputText) * 100;
        }
    }, Game.partyStartTime / 100.0 + "", "The wait time in seconds after---all players are ready before---the battle begins.");;

    TextBox tps = new TextBox(this.centerX, this.centerY, this.objWidth, this.objHeight, "Updates per second", new Runnable()
    {
        @Override
        public void run()
        {
            if (tps.inputText.length() != 0)
                Tank.updatesPerSecond = Integer.parseInt(tps.inputText);
            else
                tps.inputText = tps.previousInputText;
        }
    }, Tank.updatesPerSecond + "", "The number of update events---tanks send each second.------Smaller values work better with---less stable connections.");

    Button anticheat = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (!TankPlayerRemote.checkMotion)
            {
                TankPlayerRemote.checkMotion = true;
                TankPlayerRemote.weakTimeCheck = false;
                TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatStrongTimeOffset;
            }
            else if (!TankPlayerRemote.weakTimeCheck)
            {
                TankPlayerRemote.weakTimeCheck = true;
                TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatWeakTimeOffset;
            }
            else
                TankPlayerRemote.checkMotion = false;

            if (!TankPlayerRemote.checkMotion)
                anticheat.setText(anticheatText, ScreenOptions.offText);
            else if (!TankPlayerRemote.weakTimeCheck)
                anticheat.setText(anticheatText, strongText);
            else
                anticheat.setText(anticheatText, weakText);
        }
    },
            "When this option is enabled---while hosting a party,---other players' positions and---velocities will be checked---and corrected if invalid.------Weaker settings work better---with less stable connections.");

    Button disableFriendlyFire = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.disablePartyFriendlyFire = !Game.disablePartyFriendlyFire;

            if (Game.disablePartyFriendlyFire)
                disableFriendlyFire.setText(disableFriendlyFireText, disabledText);
            else
                disableFriendlyFire.setText(disableFriendlyFireText, defaultText);
        }
    },
            "Disables all friendly fire in the party.---Useful for co-op in bigger parties.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer)
        {
            Game.screen = ScreenPartyHost.activeScreen;
            ScreenOptions.saveOptions(Game.homedir);
        }
        else
            Game.screen = new ScreenOptionsMultiplayer();
    }
    );


    public ScreenOptionsPartyHost()
    {

        if (!TankPlayerRemote.checkMotion)
            anticheat.setText(anticheatText, ScreenOptions.offText);
        else if (!TankPlayerRemote.weakTimeCheck)
            anticheat.setText(anticheatText, strongText);
        else
            anticheat.setText(anticheatText, weakText);

        if (Game.disablePartyFriendlyFire)
            disableFriendlyFire.setText(disableFriendlyFireText, disabledText);
        else
            disableFriendlyFire.setText(disableFriendlyFireText, defaultText);

        timer.maxValue = 60;
        timer.maxChars = 4;
        timer.checkMaxValue = true;
        timer.allowDoubles = true;
        timer.allowLetters = false;
        timer.allowSpaces = false;

        tps.maxValue = 60;
        tps.maxChars = 4;
        tps.checkMaxValue = true;
        tps.allowLetters = false;
        tps.allowSpaces = false;
    }

    @Override
    public void update()
    {
        super.update();

        back.update();
        timer.update();
        tps.update();
        anticheat.update();
        disableFriendlyFire.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        back.draw();
        anticheat.draw();
        disableFriendlyFire.draw();
        tps.draw();
        timer.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Party host options");
    }

}
