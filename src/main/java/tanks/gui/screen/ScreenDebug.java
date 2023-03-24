package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenDebug extends Screen
{
    public String traceText = "Trace rays: ";
    public String firstPersonText = "First person: ";
    public String followingCamText = "Immersive camera: ";
    public String tankIDsText = "Show tank IDs: ";
    public String invulnerableText = "Invulnerable: ";

    public ScreenDebug()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

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

        tankHitboxes.setText("Tank Hitboxes: ", Game.showTankHitboxes ? ScreenOptions.onText : ScreenOptions.offText);
        obstacleHitboxes.setText("Obstacle Hitboxes: ", Game.showObstacleHitboxes ? ScreenOptions.onText : ScreenOptions.offText);
        pathfinding.setText("Show Pathfinding: ", Game.showPathfinding ? ScreenOptions.onText : ScreenOptions.offText);
        allNums.setText("All Numbers: ", Game.allowAllNumbers ? ScreenOptions.onText : ScreenOptions.offText);
    }

    Button back = new Button(this.centerX, this.centerY + 210, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle()
    );

    Button keyboardTest = new Button(this.centerX - this.objXSpace, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Test keyboard", () -> Game.screen = new ScreenTestKeyboard()
    );

    Button textboxTest = new Button(this.centerX - this.objXSpace, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Test text boxes", () -> Game.screen = new ScreenTestTextbox()
    );

    Button modelTest = new Button(this.centerX - this.objXSpace, this.centerY, this.objWidth, this.objHeight, "Test models", () -> Game.screen = new ScreenTestModel(Drawing.drawing.createModel("/models/tankcamoflauge/base/"))
    );

    Button fontTest = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Test fonts", () -> Game.screen = new ScreenTestFonts()
    );

    Button shapeTest = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Test shapes", () -> Game.screen = new ScreenTestShapes());

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

    Button invulnerable = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.invulnerable = !Game.invulnerable;

            if (Game.invulnerable)
                invulnerable.setText(invulnerableText, ScreenOptions.onText);
            else
                invulnerable.setText(invulnerableText, ScreenOptions.offText);
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

    @Override
    public void update()
    {
        keyboardTest.update();
        textboxTest.update();
        modelTest.update();
        fontTest.update();
        shapeTest.update();
        traceAllRays.update();
        followingCam.update();
        firstPerson.update();
        invulnerable.update();
        tankIDs.update();
        tankHitboxes.update();
        obstacleHitboxes.update();
        pathfinding.update();
        allNums.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210, "Debug menu");

        firstPerson.draw();
        followingCam.draw();
        modelTest.draw();
        keyboardTest.draw();
        textboxTest.draw();
        shapeTest.draw();
        traceAllRays.draw();
        allNums.draw();
        obstacleHitboxes.draw();
        tankHitboxes.draw();
        pathfinding.draw();
        tankIDs.draw();
        invulnerable.draw();
        fontTest.draw();
        back.draw();
    }
}
