package tanks.hotbar;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.*;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankModels;
import tanks.translation.Translation;

public class Hotbar
{
	public ItemBar itemBar;
	public int coins;

	public boolean enabledAmmunitionBar = true;
	public boolean enabledItemBar = false;
	public boolean enabledHealthBar = true;
	public boolean enabledCoins = false;
	public boolean enabledRemainingEnemies = true;

	public boolean hidden = true;
	public boolean persistent = false;

	public double percentHidden = 100;
	public double verticalOffset = 0;

	public double hideTimer = 0;

	public double rechargeTimer = 0;

	public static Button toggle;

	public void update()
	{
		if (Game.game.window.touchscreen)
		{
			this.verticalOffset = 20;
			toggle.update();
		}
		else
			this.verticalOffset = 0;

		if (this.persistent)
			this.hidden = false;

		this.hideTimer = Math.max(0, this.hideTimer - Panel.frameFrequency);

		if (this.hideTimer <= 0 && !this.persistent)
			this.hidden = true;

		if (this.hidden)
			this.percentHidden = Math.min(100, this.percentHidden + Panel.frameFrequency);
		else
			this.percentHidden = Math.max(0, this.percentHidden - 4 * Panel.frameFrequency);

		if (Game.game.input.hotbarToggle.isValid())
		{
			Game.game.input.hotbarToggle.invalidate();
			this.persistent = !this.persistent;
		}

		if (this.enabledItemBar)
			this.itemBar.update();
	}

	public void draw()
	{
        if (Game.game.window.touchscreen)
        {
            Drawing.drawing.setColor(255, 255, 255, 64);

            if (!this.persistent)
                Drawing.drawing.drawInterfaceImage("icons/widearrow.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 12, 64, 16);
            else
                Drawing.drawing.drawInterfaceImage("icons/widearrow.png", Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 12, 64, -16);
        }

        double a = (100 - this.percentHidden) * 2.55;

        if (this.enabledItemBar)
            this.itemBar.draw();

        if (this.enabledHealthBar)
        {
            int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
            int y = (int) (Drawing.drawing.interfaceSizeY - 25 + percentHidden - verticalOffset);

            Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.percentHidden) / 100.0);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 128 * (100 - this.percentHidden) / 100.0);

            Drawing.drawing.fillInterfaceRect(x, y, 350, 5);
            Drawing.drawing.setColor(255, 128, 0, a);

			double lives = 0;
			int shields = 0;

			if (Game.playerTank != null)
			{
				lives = Game.playerTank.health % 1.0;
				if (lives == 0 && Game.playerTank.health > 0)
					lives = 1;

				if (Game.playerTank.destroy && Game.playerTank.health < 1)
					lives = 0;

				shields = (int) (Game.playerTank.health - lives);
			}

			Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, lives);

			if (shields > 0)
			{
                Drawing.drawing.setColor(255, 0, 0, a);
                Drawing.drawing.fillInterfaceOval(x - 175, y, 18, 18);
				//Drawing.drawing.drawImage("shield.png", x - 175, y + 1, 14, 14);
                Drawing.drawing.setInterfaceFontSize(12);
                Drawing.drawing.setColor(255, 255, 255, a);
                Drawing.drawing.drawInterfaceText(x - 175, y, shields + "");
			}
		/*	else
			{
				Drawing.drawing.setColor(0, 160, 0);
				Drawing.drawing.drawImage("emblems/medic.png", x - 175, y, 14, 14);
			}*/
		}

		if (this.enabledAmmunitionBar)
		{
			int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.drawing.interfaceSizeY - 10 + percentHidden - verticalOffset);

			Drawing.drawing.setColor(0, 0, 0, 128 * (100 - this.percentHidden) / 100.0);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, 128 * (100 - this.percentHidden) / 100.0);

            Drawing.drawing.fillInterfaceRect(x, y, 350, 5);

            int live = 1;
			int multishot = 1;
            double max = 1;
			double cooldown = 0;
            double cooldownFrac = 0;

            ItemBullet ib = null;
            if (Game.playerTank != null && !Game.playerTank.destroy)
                ib = Game.playerTank.bullet;

            if (this.enabledItemBar && this.itemBar.selected != -1)
            {
                Item i = this.itemBar.slots[this.itemBar.selected];
                if (i instanceof ItemBullet)
                    ib = (ItemBullet) i;
            }

            if (ib != null)
            {
                live = ib.liveBullets;
                max = ib.maxLiveBullets;
				multishot = ib.shotCount;
				cooldown = ib.cooldownBase;
                cooldownFrac = ib.cooldown / ib.cooldownBase;
            }

            int uses = 0;
            int rcMax = 0;
            boolean isMine = true;
            double rcCooldownFrac = 0;

            if (Game.playerTank != null && !Game.playerTank.destroy)
            {
                Item i = null;

                if (this.enabledItemBar && this.itemBar.selected != -1)
                    i = this.itemBar.slots[this.itemBar.selected];

                if (i == null || i instanceof ItemEmpty || i instanceof ItemBullet)
                    i = Game.playerTank.mine;

                isMine = i instanceof ItemMine;

                if (i instanceof ItemMine)
                {
                    ItemMine im = (ItemMine) i;
                    uses = im.maxLiveMines - im.liveMines;
                    rcMax = im.maxLiveMines;
                }
                else if (i instanceof ItemShield)
                {
                    ItemShield is = (ItemShield) i;
                    uses = (int) ((is.max - Game.playerTank.health) / is.amount);
                    rcMax = (int) (is.max / is.amount);
                }

                rcCooldownFrac = Math.max(0, (i.cooldown - 20) / (i.cooldownBase - 20));
            }

			rechargeTimer += Panel.frameFrequency;

			int prevLive = ib != null ? ib.prevLive : live;

            double ammo = 1 - prevLive / max;
            double ammo2 = (prevLive - cooldownFrac) / max;

			if (live < prevLive)
				ammo = Math.min(1, ammo + rechargeTimer / cooldown / max * multishot);

            if (max <= 0)
                ammo = 0;

			if (ib != null && rechargeTimer > (prevLive - live) * cooldown + 10)
			{
				rechargeTimer = 0;
				ib.prevLive = live;
			}

            Drawing.drawing.setColor(0, 255, 255, a);
            Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, Math.min(1, 1 - ammo2));

            Drawing.drawing.setColor(0, 200, 255, a);
            Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, Math.max(0, ammo));

            Drawing.drawing.setColor(0, 255, 255, a);
            Drawing.drawing.fillInterfaceProgressRect(x, y, 350, 5, Math.min(1, Math.max(0, -ammo2 * max)));

            Drawing.drawing.setColor(0, 0, 0, a / 2);

            for (int i = 1; i < Math.min(50, max); i++)
            {
                double frac = i * 1.0 / max;
                Drawing.drawing.fillInterfaceRect(x - 175 + frac * 350, y, 2, 5);
            }

            if (isMine)
            {
                if (uses > 0 || rcMax == 0)
                    Drawing.drawing.setColor(0, 150, 255, a);
                else
                    Drawing.drawing.setColor(255, 255, 0);

                Drawing.drawing.fillInterfaceOval(x + 175, y, 18, 18);

                Drawing.drawing.setColor(255, 255, 0, a);
                Drawing.drawing.fillInterfaceOval(x + 175, y, 14, 14);

                if (rcCooldownFrac > 0)
                {
                    Drawing.drawing.setColor(255, 0, 0);

                    for (double p = -Math.PI; p <= rcCooldownFrac * 2 * Math.PI - Math.PI; p += 0.3)
                        Drawing.drawing.fillInterfaceOval(x + 175 + Math.sin(-p) * 8, y + Math.cos(-p) * 8, 3, 3);
                }
            }
            else
            {
                double frac = 1.5 - rcCooldownFrac / 2;
                Drawing.drawing.setColor(frac * 128 + 127, frac * 255, frac * 255, a);
                Drawing.drawing.drawInterfaceImage("shield.png", x + 175, y, 25, 25);
            }

            if (rcMax > 0)
            {
                Drawing.drawing.setInterfaceFontSize(12);
                Drawing.drawing.setColor(uses > 0 ? 0 : 255, 0, 0, a);
                Drawing.drawing.drawInterfaceText(x + 175, y, Math.max(0, uses) + "");
            }
        }

		if (this.enabledCoins)
		{
            Drawing.drawing.setInterfaceFontSize(18);
            Drawing.drawing.setColor(0, 0, 0, a);

			if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255, a);

			Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 100 + percentHidden - verticalOffset, "Coins: %d", coins);
		}

		if (this.enabledRemainingEnemies)
		{
			int count = 0;

			for (Movable m : Game.movables)
			{
				if (m instanceof Tank && !Team.isAllied(Game.playerTank, m) && !m.destroy && ((Tank)m).mandatoryKill)
					count++;
			}

			int x = (int) ((Drawing.drawing.interfaceSizeX / 2) - 210);
			int y = (int) (Drawing.drawing.interfaceSizeY - 17.5 + percentHidden - verticalOffset);

            Drawing.drawing.setColor(159, 32, 32, a);
            Drawing.drawing.drawInterfaceModel(TankModels.tank.base, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

            Drawing.drawing.setColor(255, 0, 0, a);
            Drawing.drawing.drawInterfaceModel(TankModels.tank.color, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

            Drawing.drawing.setColor(159, 32, 32, a);

			Drawing.drawing.drawInterfaceModel(TankModels.tank.turret, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

            Drawing.drawing.setColor(207, 16, 16, a);
            Drawing.drawing.drawInterfaceModel(TankModels.tank.turretBase, x, y, Game.tile_size / 2, Game.tile_size / 2, 0);

            Drawing.drawing.setColor(255, 0, 0, a);
            Drawing.drawing.setInterfaceFontSize(24);
			Drawing.drawing.drawInterfaceText(x - 20, y, "" + count, true);
		}

		if (Game.currentLevel != null && (Game.currentLevel.timed && Game.screen instanceof ScreenGame))
		{
			int secondsTotal = (int) (((ScreenGame) Game.screen).timeRemaining / 100 + 0.5);
			double secondsFrac = (((ScreenGame) Game.screen).timeRemaining / 100 + 0.5) - secondsTotal;

			int seconds60 = secondsTotal % 60;
			int minutes = secondsTotal / 60;

			double sizeMul = 1;
			double alpha = 127;
			double red = 0;

			if (((ScreenGame) Game.screen).playing)
			{
				if (secondsTotal == 60 || secondsTotal == 30 || secondsTotal <= 10)
				{
					sizeMul = 1.5;

					if (secondsFrac > 0.4 && secondsFrac <= 0.8 && secondsTotal > 9)
						alpha = 0;

					if (secondsTotal <= 9)
						red = Math.max(0, secondsFrac * 2 - 1) * 255;

					if (secondsTotal <= 5 && red == 0)
						red = Math.max(0, secondsFrac * 2) * 255;
				}
				else if (secondsTotal == 59 || secondsTotal == 29)
					sizeMul = 1.0 + Math.max(((((ScreenGame) Game.screen).timeRemaining / 100) - secondsTotal), 0);
			}

			String st = Translation.translate("Time: ");
			String s = st + minutes + ":" + seconds60;
			if (seconds60 < 10)
				s = st + minutes + ":0" + seconds60;

			Drawing.drawing.setInterfaceFontSize(32 * sizeMul);
			Drawing.drawing.setColor(red, 0, 0, (alpha + red / 2) * Obstacle.draw_size / Game.tile_size);

			if (Level.isDark())
				Drawing.drawing.setColor(255, 255 - red, 255 - red, (alpha + red / 2) * Obstacle.draw_size / Game.tile_size);

			double posX = Drawing.drawing.interfaceSizeX / 2;
			double posY = 50;

			if (ScreenGame.finishedQuick)
			{
				Drawing.drawing.setInterfaceFontSize(32);
				Drawing.drawing.setColor(0, 0, 0, 127 * Obstacle.draw_size / Game.tile_size);

				if (Level.isDark())
					Drawing.drawing.setColor(255, 255, 255, 127);
			}

			if (((ScreenGame) Game.screen).timeRemaining <= 0)
			{
				Drawing.drawing.setColor(255, 0, 0, 255 * Obstacle.draw_size / Game.tile_size);

				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Out of time!");
			}
			else
				Drawing.drawing.displayInterfaceText(posX, posY, s);
		}
	}
}
