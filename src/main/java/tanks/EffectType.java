package tanks;

import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.minigames.Arcade;
import tanks.obstacle.Obstacle;
import tanks.rendering.TrackRenderer;
import tanks.tank.Turret;

public enum EffectType
{
    fire(e -> e.maxAge = 20, e ->
    {
        double size = e.age * 3 + 10;
        double rawOpacity = 1.0 - e.age / 20.0;
        rawOpacity *= rawOpacity * rawOpacity;
        double opacity = rawOpacity * 255 / 4;

        double green = Math.min(255, 255 - 255.0 * (e.age / 20.0));
        Drawing.drawing.setColor(255, green, 0, Math.min(255, Math.max(0, opacity * ScreenGame.finishTimer / ScreenGame.finishTimerMax)));

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    smokeTrail(e -> e.maxAge = 200, e ->
    {
        double opacityModifier = Math.max(0, Math.min(1, e.age / 40.0 - 0.25));
        int size = 20;
        double rawOpacity = 1.0 - e.age / 200.0;
        rawOpacity *= rawOpacity * rawOpacity;
        double opacity = rawOpacity * 100 / 2;

        Drawing.drawing.setColor(0, 0, 0, Math.min(255, Math.max(0, opacity * ScreenGame.finishTimer / ScreenGame.finishTimerMax * opacityModifier)));

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    trail(e -> e.maxAge = 50, e ->
    {
        double size = Math.min(20, e.age / 20.0 + 10);
        double rawOpacity = 1.0 - e.age / 50.0;
        rawOpacity *= rawOpacity * rawOpacity;
        double opacity = rawOpacity * 50;
        Drawing.drawing.setColor(127, 127, 127, Math.min(255, Math.max(0, opacity * ScreenGame.finishTimer / ScreenGame.finishTimerMax)));

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    ray(e -> e.maxAge = 20, e ->
    {
        int size = 6;

        if (Level.isDark()) Drawing.drawing.setColor(255, 255, 255, 50);
        else Drawing.drawing.setColor(0, 0, 0, 50);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    explosion(e ->
    {
        e.maxAge = 20;
        e.force = true;
    }, e ->
    {
        double size = e.radius * 2;
        double opacity = 100 - e.age * 5;

        if (Game.vanillaMode) size += Game.tile_size;

        Drawing.drawing.setColor(255, 0, 0, opacity, 1);
        Drawing.drawing.fillForcedOval(e.posX, e.posY, size, size);
        Drawing.drawing.setColor(255, 255, 255);
    }),

    laser(e -> e.maxAge = 21, e ->
    {
        double size = Bullet.bullet_size - e.age / 2;
        Drawing.drawing.setColor(255, 0, 0);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    piece(e -> e.maxAge = Math.random() * 100 + 50, e ->
    {
        double size = 1 + Bullet.bullet_size * (1 - e.age / e.maxAge);
        Drawing.drawing.setColor(e.colR, e.colG, e.colB, 255, 0.5);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    obstaclePiece(e -> e.maxAge = Math.random() * 100 + 50, e ->
    {
        double size = 1 + Bullet.bullet_size * (1 - e.age / e.maxAge);
        Drawing.drawing.setColor(e.colR, e.colG, e.colB);

        Drawing.drawing.fillRect(e.posX, e.posY, size, size);
    }),

    obstaclePiece3d(e ->
    {
        e.size = Math.random() * 20;
        e.maxAge = Math.random() * 150 + 75;
        e.force = true;
    }, e ->
    {
        double size = 1 + e.size * (1 - e.age / e.maxAge);
        Drawing.drawing.setColor(e.colR, e.colG, e.colB);

        Drawing.drawing.fillBox(e.posX, e.posY, e.posZ, size, size, size);
    }),

    charge(e ->
    {
        if (Game.enable3d)
            e.add3dPolarMotion(Math.random() * Math.PI * 2, -Math.atan(Math.random()), Math.random() * 3 + 3);
        else e.addPolarMotion(Math.random() * Math.PI * 2, Math.random() * 3 + 3);

        e.posX -= e.vX * 25;
        e.posY -= e.vY * 25;
        e.posZ -= e.vZ * 25;
        e.maxAge = 25;
    }, e ->
    {
        double size = 1 + Bullet.bullet_size * (e.age / e.maxAge);
        Drawing.drawing.setColor(e.colR, e.colG, e.colB, 255, 0.5);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    tread(e -> e.maxAge = TrackRenderer.getMaxTrackAge(), e ->
    {
    }),

    darkFire(e -> e.maxAge = 20, e ->
    {
        double size = e.age * 3 + 10;
        double rawOpacity = 1.0 - e.age / 20.0;
        rawOpacity *= rawOpacity * rawOpacity;
        double opacity = rawOpacity * 255 / 4;

        double red = Math.min(255, 128 - 128.0 * (e.age / 20.0));
        Drawing.drawing.setColor(red / 2, 0, red, Math.min(255, Math.max(0, opacity * ScreenGame.finishTimer / ScreenGame.finishTimerMax)));

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    electric(e -> e.maxAge = 20, e ->
    {
        double size = Math.max(0, Bullet.bullet_size - e.age / 2);
        Drawing.drawing.setColor(0, 255, 255);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    healing(e -> e.maxAge = 21, e ->
    {
        double size = Bullet.bullet_size - e.age / 2;
        Drawing.drawing.setColor(0, 255, 0);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    stun(e ->
    {
        e.angle += Math.PI * 2 * Math.random();
        e.maxAge = 80 + Math.random() * 40;
        e.size = Math.random() * 5 + 5;
        e.distance = Math.random() * 50 + 25;
    }, e ->
    {
        double size = 1 + e.size * Math.min(Math.min(1, (e.maxAge - e.age) * 3 / e.maxAge), Math.min(1, e.age * 3 / e.maxAge));
        double angle = e.angle + e.age / 20;
        double distance = 1 + e.distance * Math.min(Math.min(1, (e.maxAge - e.age) * 3 / e.maxAge), Math.min(1, e.age * 3 / e.maxAge));

        Drawing.drawing.setColor(e.colR, e.colG, e.colB, 255, 0.5);
        double[] o = Movable.getLocationInDirection(angle, distance);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX + o[0], e.posY + o[1], e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX + o[0], e.posY + o[1], size, size);
    }),

    bushBurn(e -> e.maxAge = e.posZ * 2, e ->
    {
        if (Game.enable3d)
        {
            Drawing.drawing.setColor(e.colR, e.colG, e.colB);
            Drawing.drawing.fillBox(e.posX, e.posY, 0, Obstacle.draw_size, Obstacle.draw_size, e.posZ);
        }
        else
        {
            Drawing.drawing.setColor(e.colR, e.colG, e.colB, e.posZ);
            Drawing.drawing.fillRect(e.posX, e.posY, Obstacle.draw_size, Obstacle.draw_size);
        }

        if (!Game.game.window.drawingShadow) e.posZ -= Panel.frameFrequency / 2;

        e.colR = Math.max(e.colR - Panel.frameFrequency, 0);
        e.colG = Math.max(e.colG - Panel.frameFrequency, 0);
        e.colB = Math.max(e.colB - Panel.frameFrequency, 0);
    }),

    glow(e -> e.maxAge = 100, e ->
    {
        double size = 1 + 40 * (1 - e.age / e.maxAge);
        Drawing.drawing.setColor(255, 255, 255, 40);

        if (Game.enable3d)
        {
            Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size, false, true);
            Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size / 2, size / 2, false, true);
        }
        else
        {
            Drawing.drawing.fillOval(e.posX, e.posY, size, size);
            Drawing.drawing.fillOval(e.posX, e.posY, size / 2, size / 2);
        }
    }),

    teleporterLight(e -> e.maxAge = 0, e ->
    {
        for (double i = 0; i < 1 - e.size; i += 0.025)
        {
            Drawing.drawing.setColor(255, 255, 255, (1 - e.size - i) * 25, 1);
            Drawing.drawing.fillOval(e.posX, e.posY, e.posZ + 7 + i * 50, Obstacle.draw_size / 2, Obstacle.draw_size / 2, true, false);
        }
    }),

    teleporterPiece(e -> e.maxAge = Math.random() * 100 + 50, e ->
    {
        double size = 1 + Bullet.bullet_size * (1 - e.age / e.maxAge);
        Drawing.drawing.setColor(e.colR, e.colG, e.colB, 255, 0.5);

        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size, size);
        else Drawing.drawing.fillOval(e.posX, e.posY, size, size);
    }),

    interfacePiece(e ->
    {
        e.maxAge = Math.random() * 100 + 50;
        e.force = true;
    }, e ->
    {
        double size = 1 + Bullet.bullet_size * (1 - e.age / e.maxAge);

        if (e.size > 0) size *= e.size;

        Drawing.drawing.setColor(e.colR, e.colG, e.colB, 255, 0.5);
        Drawing.drawing.fillInterfaceOval(e.posX, e.posY, size, size);
    }),

    interfacePieceSparkle(e ->
    {
        e.maxAge = Math.random() * 100 + 50;
        e.force = true;
    }, e ->
    {
        double size = 1 + Bullet.bullet_size * (1 - e.age / e.maxAge);
        if (e.size > 0) size *= e.size;
        Drawing.drawing.setColor(e.colR, e.colG, e.colB, 255, 0.5);
        Drawing.drawing.fillInterfaceOval(e.posX, e.posY, size, size);
    }),

    snow(e ->
    {
        e.maxAge = Math.random() * 100 + 50;
        e.size = (Math.random() * 4 + 2) * Bullet.bullet_size;
    }, e ->
    {
        double size2 = 1 + 1.5 * (Bullet.bullet_size * (1 - e.age / e.maxAge));
        Drawing.drawing.setColor(e.colR, e.colG, e.colB);
        if (Game.enable3d) Drawing.drawing.fillOval(e.posX, e.posY, e.posZ, size2, size2);
        else Drawing.drawing.fillOval(e.posX, e.posY, size2, size2);
    }),

    splash(e ->
    {
        e.maxAge = 75;
        e.size = 75;
    }, e ->
    {
        double s = e.age / e.maxAge * e.size;
        Drawing.drawing.setColor(240, 240, 240, (1 - e.age / e.maxAge) * 128);
        Drawing.drawing.fillOval(e.posX, e.posY, s, s);
    }),

    shield(e -> e.maxAge = 50, e ->
    {
        double a = Math.min(25, 50 - e.age) * 2.55 * 4;
        Drawing.drawing.setColor(255, 255, 255, a);
        if (Game.enable3d)
        {
            Drawing.drawing.drawImage("shield.png", e.posX, e.posY, e.posZ + e.age, e.size * 1.25, e.size * 1.25);
            Drawing.drawing.setFontSize(24 * e.size / Game.tile_size);
            Drawing.drawing.setColor(0, 0, 0, a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20, e.posZ + e.age + 1, "" + (int) e.radius);
        }
        else
        {
            Drawing.drawing.drawImage("shield.png", e.posX, e.posY, e.size * 1.25, e.size * 1.25);
            Drawing.drawing.setFontSize(24 * e.size / Game.tile_size);
            Drawing.drawing.setColor(0, 0, 0, a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20, "" + (int) e.radius);
        }
    }),

    boostLight(e -> e.maxAge = 0, e ->
    {
        if (Game.game.window.drawingShadow) return;

        Drawing.drawing.setColor(255, 255, 255, 255, 1);
        Game.game.window.shapeRenderer.setBatchMode(true, true, true, true, false);

        double max = e.size;
        for (int i = 0; i < max; i++)
        {
            double a = (max - i) / 400;
            Drawing.drawing.setColor(255 * a, 255 * a, 200 * a, 255, 1.0);
            Drawing.drawing.fillBox(e.posX, e.posY, i, Game.tile_size, Game.tile_size, 0, (byte) 62);
        }

        Game.game.window.shapeRenderer.setBatchMode(false, true, true, true, false);
    }),

    exclamation(e -> e.maxAge = 50, e ->
    {
        double a = Math.min(25, 50 - e.age) * 2.55 * 4;

        double r2 = Turret.calculateSecondaryColor(e.colR);
        double g2 = Turret.calculateSecondaryColor(e.colG);
        double b2 = Turret.calculateSecondaryColor(e.colB);

        Drawing.drawing.setColor(r2, g2, b2, a, 0.5);

        if (Game.enable3d)
        {
            Drawing.drawing.fillOval(e.posX, e.posY, e.posZ + e.age, e.size, e.size);
            Drawing.drawing.setColor(e.colR, e.colG, e.colB, a, 0);
            Drawing.drawing.fillOval(e.posX, e.posY, e.posZ + e.age, e.size * 0.8, e.size * 0.8);
            Drawing.drawing.setFontSize(32 * e.size / Game.tile_size);

            Drawing.drawing.setColor(r2, g2, b2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, 3 + e.posY - e.size / 20, e.posZ + e.age + 1, "!");
            Drawing.drawing.setColor(e.glowR, e.glowG, e.glowB, a, 1);
            Drawing.drawing.drawText(e.posX + 0, 1 + e.posY - e.size / 20, e.posZ + e.age + 2, "!");
        }
        else
        {
            Drawing.drawing.fillOval(e.posX, e.posY, e.size, e.size);
            Drawing.drawing.setColor(e.colR, e.colG, e.colB, a, 0);
            Drawing.drawing.fillOval(e.posX, e.posY, e.posZ + e.age, e.size * 0.8, e.size * 0.8);
            Drawing.drawing.setFontSize(32 * e.size / Game.tile_size);
            Drawing.drawing.setColor(r2, g2, b2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, 3 + e.posY - e.size / 20, "!");
            Drawing.drawing.setColor(e.glowR, e.glowG, e.glowB, a, 1);
            Drawing.drawing.drawText(e.posX + 0, 1 + e.posY - e.size / 20, "!");
        }
    }),

    chain(e ->
    {
        e.drawLayer = 9;
        e.maxAge = 100;
        e.size = Game.tile_size * 2;
    }, e ->
    {
        double a = Math.min(50, e.maxAge - e.age) / 50 * 255;
        Drawing.drawing.setColor(255, 255, 255, a);

        double c = 0.5 - Math.min(Arcade.max_power * 3, e.radius) / 30;
        if (c < 0) c += (int) -c + 1;

        double[] col = Game.getRainbowColor(c);

        if (Game.enable3d)
        {
            Drawing.drawing.setFontSize(24 * e.size / Game.tile_size);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, e.posY - e.size / 20 - 5, e.posZ + e.age, "" + (int) e.radius);
            Drawing.drawing.setColor(col[0], col[1], col[2], a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20 - 7, e.posZ + e.age + 1, "" + (int) e.radius);

            Drawing.drawing.setFontSize(8 * e.size / Game.tile_size);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, e.posY - e.size / 20 + 22, e.posZ + e.age, "chain!");
            Drawing.drawing.setColor(col[0], col[1], col[2], a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20 + 20, e.posZ + e.age + 1, "chain!");
        }
        else
        {
            Drawing.drawing.setFontSize(24 * e.size / Game.tile_size);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, e.posY - e.size / 20 - 5, "" + (int) e.radius);
            Drawing.drawing.setColor(col[0], col[1], col[2], a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20 - 7, "" + (int) e.radius);

            Drawing.drawing.setFontSize(8 * e.size / Game.tile_size);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, e.posY - e.size / 20 + 22, "chain!");
            Drawing.drawing.setColor(col[0], col[1], col[2], a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20 + 20, "chain!");
        }
    }),

    tutorialProgress(e ->
    {
        e.drawLayer = 9;
        e.maxAge = 100;
        e.size = Game.tile_size * 2;
    }, e ->
    {
        double a = Math.min(50, e.maxAge - e.age) / 50 * 255;
        Drawing.drawing.setColor(255, 255, 255, a);

        double c = 0.5 - Math.min(Arcade.max_power * 3, e.radius * 4) / 30;
        if (c < 0) c += (int) -c + 1;

        double[] col = Game.getRainbowColor(c);

        String text = (int) e.radius + "/4";

        if (e.radius == 5) text = "Nice shot!";

        if (e.radius == 6) text = "Great!";

        if (e.radius == 7) text = "Got it!";

        if (Game.enable3d)
        {
            Drawing.drawing.setFontSize(24 * e.size / Game.tile_size);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, e.posY - e.size / 20 - 5, e.posZ + e.age, text);
            Drawing.drawing.setColor(col[0], col[1], col[2], a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20 - 7, e.posZ + e.age + 1, text);
        }
        else
        {
            Drawing.drawing.setFontSize(24 * e.size / Game.tile_size);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, a, 0.5);
            Drawing.drawing.drawText(e.posX + 2, e.posY - e.size / 20 - 5, text);
            Drawing.drawing.setColor(col[0], col[1], col[2], a, 0.5);
            Drawing.drawing.drawText(e.posX, e.posY - e.size / 20 - 7, text);
        }
    });

    private final Consumer<Effect> initialize, draw;

    EffectType(Consumer<Effect> initialize, Consumer<Effect> draw)
    {
        this.initialize = initialize;
        this.draw = draw;
    }

    public void initialize(Effect e)
    {
        initialize.accept(e);
    }

    public void draw(Effect e)
    {
        draw.accept(e);
    }
}