package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenDebug extends ScreenOptionsOverlay
{
    public String traceText = "Trace rays: ";
    public String firstPersonText = "First person: ";
    public String followingCamText = "Immersive camera: ";
    public String tankIDsText = "Show tank IDs: ";
    public String invulnerableText = "Invulnerable: ";
    public String fancyLightsText = "Fancy lighting: ";


    Button back = new Button(this.centerX, this.centerY + 210, this.objWidth, this.objHeight, "Back", () ->
    {
        if (game != null)
        {
            game.screenshotMode = false;
            Game.screen = game;
        }
        else
            Game.screen = new ScreenTitle();
    });

    Button test = new Button(this.centerX - this.objXSpace, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Test stuff", () -> Game.screen = new ScreenTestDebug());

    Button glipping = new Button(this.centerX - this.objXSpace, this.centerY, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.game.window.allowGlipping = !Game.game.window.allowGlipping;
            glipping.setText("Glipping: ", Game.game.window.allowGlipping ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    Button tankHitboxes = new Button(this.centerX + this.objXSpace, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showTankHitboxes = !Game.showTankHitboxes;
            tankHitboxes.setText("Tank Hitboxes: ", Game.showTankHitboxes ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    Button obstacleHitboxes = new Button(this.centerX + this.objXSpace, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showObstacleHitboxes = !Game.showObstacleHitboxes;
            obstacleHitboxes.setText("Obstacle Hitboxes: ", Game.showObstacleHitboxes ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    Button pathfinding = new Button(this.centerX + this.objXSpace, this.centerY, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showPathfinding = !Game.showPathfinding;
            pathfinding.setText("Show Pathfinding: ", Game.showPathfinding ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    Button allNums = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.allowAllNumbers = !Game.allowAllNumbers;

            allNums.setText("All Numbers: ", Game.allowAllNumbers ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    Button autocannon = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.autocannon = !Game.autocannon;
            autocannon.setText("Autocannon: ", Game.autocannon ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    public ScreenDebug()
    {
        if (Game.traceAllRays)
            traceAllRays.setText(traceText, ScreenOptions.onText);
        else
            traceAllRays.setText(traceText, ScreenOptions.offText);

        if (Game.firstPerson)
            firstPerson.setText(firstPersonText, ScreenOptions.onText);
        else
            firstPerson.setText(firstPersonText, ScreenOptions.offText);

        if (Game.followingCam)
            followingCam.setText(followingCamText, ScreenOptions.onText);
        else
            followingCam.setText(followingCamText, ScreenOptions.offText);

        if (Game.showTankIDs)
            tankIDs.setText(tankIDsText, ScreenOptions.onText);
        else
            tankIDs.setText(tankIDsText, ScreenOptions.offText);

        if (Game.invulnerable)
            invulnerable.setText(invulnerableText, ScreenOptions.onText);
        else
            invulnerable.setText(invulnerableText, ScreenOptions.offText);

        grandpaMode.setText("Grandpa mode: ", Game.grandpaMode ? ScreenOptions.onText : ScreenOptions.offText);
        glipping.setText("Glipping: ", Game.game.window.allowGlipping ? ScreenOptions.onText : ScreenOptions.offText);
        tankHitboxes.setText("Tank Hitboxes: ", Game.showTankHitboxes ? ScreenOptions.onText : ScreenOptions.offText);
        obstacleHitboxes.setText("Obstacle Hitboxes: ", Game.showObstacleHitboxes ? ScreenOptions.onText : ScreenOptions.offText);
        autocannon.setText("Autocannon: ", Game.autocannon ? ScreenOptions.onText : ScreenOptions.offText);
        pathfinding.setText("Show Pathfinding: ", Game.showPathfinding ? ScreenOptions.onText : ScreenOptions.offText);
        allNums.setText("All Numbers: ", Game.allowAllNumbers ? ScreenOptions.onText : ScreenOptions.offText);

        if (Game.fancyLights)
            fancyLighting.setText(fancyLightsText, ScreenOptions.onText);
        else
            fancyLighting.setText(fancyLightsText, ScreenOptions.offText);
    }

    Button traceAllRays = new Button(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.traceAllRays = !Game.traceAllRays;

            if (Game.traceAllRays)
                traceAllRays.setText(traceText, ScreenOptions.onText);
            else
                traceAllRays.setText(traceText, ScreenOptions.offText);
        }
    });

    Button firstPerson = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.firstPerson = !Game.firstPerson;

            if (Game.firstPerson)
                firstPerson.setText(firstPersonText, ScreenOptions.onText);
            else
                firstPerson.setText(firstPersonText, ScreenOptions.offText);
        }
    });

    Button followingCam = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.followingCam = !Game.followingCam;

            if (Game.followingCam)
                followingCam.setText(followingCamText, ScreenOptions.onText);
            else
                followingCam.setText(followingCamText, ScreenOptions.offText);
        }
    });

    Button tankIDs = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showTankIDs = !Game.showTankIDs;

            if (Game.showTankIDs)
                tankIDs.setText(tankIDsText, ScreenOptions.onText);
            else
                tankIDs.setText(tankIDsText, ScreenOptions.offText);
        }
    });

    Button grandpaMode = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.grandpaMode = !Game.grandpaMode;
            grandpaMode.setText("Grandpa mode: ", Game.grandpaMode ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    Button fancyLighting = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fancyLights = !Game.fancyLights;

            if (Game.fancyLights)
                fancyLighting.setText(fancyLightsText, ScreenOptions.onText);
            else
                fancyLighting.setText(fancyLightsText, ScreenOptions.offText);
        }
    });

    @Override
    public void update()
    {
        test.update();
        fancyLighting.update();
        traceAllRays.update();
        followingCam.update();
        firstPerson.update();
        invulnerable.update();
        glipping.update();
        grandpaMode.update();
        autocannon.update();
        tankIDs.update();
        tankHitboxes.update();
        obstacleHitboxes.update();
        pathfinding.update();
        allNums.update();
        back.update();

        super.update();
    }

    Button invulnerable = new Button(this.centerX - this.objXSpace, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.invulnerable = !Game.invulnerable;
            if (Game.playerTank != null)
                Game.playerTank.invulnerable = Game.invulnerable;

            if (Game.invulnerable)
                invulnerable.setText(invulnerableText, ScreenOptions.onText);
            else
                invulnerable.setText(invulnerableText, ScreenOptions.offText);
        }
    });

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(brightness, brightness, brightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210, "Debug menu");

        test.draw();
        fancyLighting.draw();
        firstPerson.draw();
        followingCam.draw();
        traceAllRays.draw();
        allNums.draw();
        grandpaMode.draw();
        obstacleHitboxes.draw();
        tankHitboxes.draw();
        pathfinding.draw();
        tankIDs.draw();
        glipping.draw();
        autocannon.draw();
        invulnerable.draw();
        back.draw();
    }
}
