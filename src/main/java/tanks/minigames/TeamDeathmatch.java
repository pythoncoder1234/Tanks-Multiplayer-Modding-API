package tanks.minigames;

import basewindow.InputCodes;
import tanks.*;
import tanks.gui.menus.FixedText;
import tanks.gui.menus.Scoreboard;
import tanks.hotbar.item.Item;
import tanks.obstacle.ObstacleTeleporter;
import tanks.tank.Tank;
import tanks.tank.TankMedic;
import tanks.tank.TankModels;
import tanks.tank.TankNPC;

import java.util.ArrayList;

/**
 * This is the minigame I wrote to test out features of the Mod API.<br><br>
 * <p>
 * So if things change from time to time, or I forget to remove a
 * piece of code I was testing, that's why.<br><br>
 * <p>
 * Want to make your own minigame? See {@link Minigame}
 */
public class TeamDeathmatch extends Minigame
{
    public static final String levelString = "{85,30,0,175,0,0,40,0,0,100,50|2-2-hard-6.0,2-3-hard-5.5,2-4-hard-6.0,2-25-hard-6.0,2-26-hard-5.5,2-27-hard-6.0,3-1-hard-6.0,3-5-hard-6.0,3-24-hard-6.0,3-28-hard-6.0,4-0-hard-6.0,4-6-hard-6.0,4-23-hard-6.0,4-29-hard-6.0,5-0-hard-5.5,5-3-teleporter-1,5-6-hard-5.5,5-7-hard-3.0,5-8-hard-2.5,5-9-hard-3.0,5-10-hard-2.5,5-11-hard-3.0,5-12-hard-2.5,5-13-hard-3.0,5-14-hard-2.5,5-15-hard-3.0,5-16-hard-2.5,5-17-hard-3.0,5-18-hard-2.5,5-19-hard-3.0,5-20-hard-2.5,5-21-hard-3.0,5-22-hard-2.5,5-23-hard-5.5,5-26-teleporter-1,5-29-hard-5.5,6-0-hard-6.0,6-6-hard-6.0,6-11...11-hole-,6-11...11-hard-1.0-1.5,6-18...18-hole-,6-18...18-hard-1.0-1.5,6-23-hard-6.0,6-29-hard-6.0,7-1-hard-6.0,7-11-hole-,7-11-hard-1.0-1.5,7-18-hole-,7-18-hard-1.0-1.5,7-28-hard-6.0,8-2-hard-6.0,8-3-hard-5.5,8-4-hard-6.0,8-11-hard-3.0,8-18-hard-3.0,8-25-hard-6.0,8-26-hard-5.5,8-27-hard-6.0,9-3-hard-2.5,9-11-hard-2.5,9-18-hard-2.5,9-26-hard-2.5,10-3-hard-3.0,10...11-11-hole-,10...11-11-hard-1.0-1.5,10-18...18-hole-,10-18...18-hard-1.0-1.5,10-26-hard-3.0,11-3-hard-2.5,11-18-hole-,11-18-hard-1.0-1.5,11-26-hard-2.5,12-3-hard-3.0,12-12-hole-,12-12-hard-1.0-1.5,12-17-hole-,12-17-hard-1.0-1.5,12-26-hard-3.0,13-3-hard-2.5,13-13-hard-2.5,13-14-hard-3.0,13-15...16-hard-2.5,13-26-hard-2.5,14-3-hard-3.0,14-26-hard-3.0,15-3-hard-2.5,15-26-hard-2.5,16-3-hard-3.0,16-26-hard-3.0,17-3-hard-2.5,17-26-hard-2.5,18-3-hard-3.0,18-26-hard-3.0,19-2-hard-5.5,19-3-hard-2.5,19-25-hard-6.0,19-26-hard-5.5,19-27-hard-6.0,20-1-hard-5.5,20-28-hard-6.0,21-0-hard-5.5,21-6-hard-5.5,21-23-hard-6.0,21-29-hard-6.0,22-0-hard-6.0,22-3-teleporter-1,22-6-hard-6.0,22-7-hard-3.0,22-8-hard-2.5,22-9-hard-3.0,22-10-hard-2.5,22-11-hard-3.0,22-12-hard-2.5,22-13-hard-3.0,22...22-14-hard-1.0-2.0,22-15-hard-1.0-2.0,22-16-hard-3.0,22-17-hard-2.5,22-18-hard-3.0,22-19-hard-2.5,22-20-hard-3.0,22-21-hard-2.5,22-22-hard-2.5,22-23-hard-5.5,22-26-teleporter-1,22-29-hard-5.5,23-0-hard-5.5,23-6-hard-5.5,23-23-hard-6.0,23-29-hard-6.0,24-1-hard-5.5,24-28-hard-6.0,25-2-hard-5.5,25-3-hard-6.0,25-4-hard-5.5,25-25-hard-6.0,25-26-hard-5.5,25-27-hard-6.0,31...31-3-normal-0.5,31...31-4-normal-0.5,31...31-5-normal-0.5,31...31-6-normal-0.5,31...31-7-normal-0.5,31...31-8-normal-0.5,31-9-normal-0.5,31...31-19-normal-0.5,31...31-20-normal-0.5,31...31-21-normal-0.5,31...31-22-normal-0.5,31...31-23-normal-0.5,31...31-24-normal-0.5,31...31-25-normal-0.5,31-26-normal-0.5,36-7-sand-1.0,36...49-11-normal-1.0,36...49-15-normal-1.0,36...36-16-sand-1.0,36-17-sand-1.0,37-19-sand-1.0,37...37-22-sand-1.0,37...40-23-sand-1.0,37...37-24-sand-1.0,37...37-25-sand-1.0,37-26-sand-1.0,37...40-28-sand-1.0,38...42-1-sand-1.0,38...41-2-sand-1.0,38...40-3-sand-1.0,38...40-4-sand-1.0,38...40-5-sand-1.0,38...40-9-sand-1.0,38...38-12-breakable-0.5,38...38-13-breakable-0.5,38-14-breakable-0.5,38...40-18-sand-1.0,38...40-20-sand-1.0,39...43-0-sand-1.0,39...40-6-sand-1.0,39...40-7-sand-1.0,39...40-8-sand-1.0,39...40-10-sand-1.0,39...40-16-sand-1.0,39...40-17-sand-1.0,39...40-19-sand-1.0,39...40-21-sand-1.0,39...40-22-sand-1.0,39...40-24-sand-1.0,39...40-25-sand-1.0,39...40-26-sand-1.0,39...40-27-sand-1.0,39...40-29-sand-1.0,41...42-3-water-1.0,41...41-4-water-1.0,41...41-5-water-1.0,41...41-6-water-1.0,41...41-7-water-1.0,41...41-8-water-1.0,41...41-9-water-1.0,41-10-water-1.0,41...41-16-water-1.0,41...41-17-water-1.0,41...41-18-water-1.0,41...41-19-water-1.0,41...41-20-water-1.0,41...41-21-water-1.0,41...41-22-water-1.0,41...41-23-water-1.0,41...41-24-water-1.0,41...41-25-water-1.0,41...41-26-water-1.0,41...41-27-water-1.0,41...41-28-water-1.0,41-29-water-1.0,42...43-2-water-1.0,42...44-4-water-2.5,42...44-5-water-2.5,42...44-6-water-2.5,42...44-7-water-2.5,42...44-8-water-2.5,42...44-9-water-2.5,42...44-10-water-2.5,42...44-16-water-2.5,42...44-17-water-2.5,42...44-18-water-2.5,42...44-19-water-2.5,42...44-20-water-2.5,42...44-21-water-2.5,42...44-22-water-2.5,42...44-23-water-2.5,42...44-24-water-2.5,42...44-25-water-2.5,42...44-26-water-2.5,42...44-27-water-2.5,42...44-28-water-2.5,42...44-29-water-2.5,43...44-1-water-1.0,43...45-3-water-2.5,44...45-0-water-1.0,44...46-2-water-2.5,45...47-1-water-2.5,45...46-4-water-1.0,45...46-5-water-1.0,45...46-6-water-1.0,45...45-7-water-1.0,45...45-8-water-1.0,45...45-9-water-1.0,45-10-water-1.0,45...45-16-water-1.0,45...45-17-water-1.0,45...45-18-water-1.0,45...45-19-water-1.0,45...45-20-water-1.0,45...45-21-water-1.0,45...45-22-water-1.0,45...45-23-water-1.0,45...45-24-water-1.0,45...45-25-water-1.0,45...45-26-water-1.0,45...45-27-water-1.0,45...45-28-water-1.0,45-29-water-1.0,46...47-0-water-2.5,46...47-3-water-1.0,46...47-7-sand-1.0,46...47-8-sand-1.0,46...47-9-sand-1.0,46...47-10-sand-1.0,46...47-16-sand-1.0,46...47-17-sand-1.0,46...48-18-sand-1.0,46...47-19-sand-1.0,46...47-20-sand-1.0,46...47-21-sand-1.0,46...47-22-sand-1.0,46...49-23-sand-1.0,46...47-24-sand-1.0,46...47-25-sand-1.0,46...47-26-sand-1.0,46...47-27-sand-1.0,46...47-28-sand-1.0,46...48-29-sand-1.0,47...48-2-water-1.0,47...50-4-sand-1.0,47...49-5-sand-1.0,47...47-6-sand-1.0,47...47-12-breakable-0.5,47...47-13-breakable-0.5,47-14-breakable-0.5,48...48-0-water-1.0,48...48-1-water-1.0,48...50-3-sand-1.0,49...51-0-sand-1.0,49...51-1-sand-1.0,49...51-2-sand-1.0,49...49-8-sand-1.0,49-9-sand-1.0,49...50-16-sand-1.0,49-24-sand-1.0,50-7-sand-1.0,50...50-17-sand-1.0,50...50-18-sand-1.0,50...50-19-sand-1.0,50...50-20-sand-1.0,50...50-21-sand-1.0,50-22-sand-1.0,52...53-5-sand-1.0,53-1-sand-1.0,54...55-0-sand-1.0,54...54-3-normal-0.5,54...54-4-normal-0.5,54...54-5-normal-0.5,54...54-6-normal-0.5,54...54-7-normal-0.5,54...54-8-normal-0.5,54-9-normal-0.5,54...54-19-normal-0.5,54...54-20-normal-0.5,54...54-21-normal-0.5,54...54-22-normal-0.5,54...54-23-normal-0.5,54...54-24-normal-0.5,54...54-25-normal-0.5,54-26-normal-0.5,55-1-sand-1.0,59-2-hard-6.0,59-3-hard-5.5,59-4-hard-6.0,59-25-hard-5.5,59-26-hard-6.0,59-27-hard-5.5,60-1-hard-6.0,60-28-hard-5.5,61-0-hard-6.0,61-6-hard-6.0,61-23-hard-5.5,61-29-hard-5.5,62-0-hard-5.5,62-3-teleporter-0,62-6-hard-5.5,62-7-hard-3.0,62-8-hard-2.5,62-9-hard-3.0,62-10-hard-2.5,62-11-hard-3.0,62-12-hard-2.5,62-13-hard-3.0,62...62-14-hard-1.0-2.0,62-15-hard-1.0-2.0,62-16-hard-3.0,62-17-hard-2.5,62-18-hard-3.0,62-19-hard-2.5,62-20-hard-3.0,62-21-hard-2.5,62-22-hard-3.0,62-23-hard-6.0,62-26-teleporter-0,62-29-hard-6.0,63-0-hard-6.0,63-6-hard-6.0,63-23-hard-5.5,63-29-hard-5.5,64-1-hard-6.0,64-28-hard-5.5,65-2-hard-6.0,65-3-hard-5.5,65-4-hard-6.0,65-26-hard-2.5,65-27-hard-5.5,66-3-hard-3.0,66-26-hard-3.0,67-3-hard-2.5,67-26-hard-2.5,68-3-hard-3.0,68-26-hard-3.0,69-3-hard-2.5,69-26-hard-2.5,70-3-hard-3.0,70-26-hard-3.0,71-3-hard-2.5,71...71-13-hard-2.5,71-14-hard-2.5,71-15-hard-3.0,71-16-hard-2.5,71-26-hard-2.5,72-3-hard-3.0,72-12-hole-,72-12-hard-1.0-1.5,72-17-hole-,72-17-hard-1.0-1.5,72-26-hard-3.0,73-3-hard-2.5,73...74-11-hole-,73...74-11-hard-1.0-1.5,73...74-18-hole-,73...74-18-hard-1.0-1.5,73-26-hard-2.5,74-3-hard-3.0,74-26-hard-3.0,75-3-hard-2.5,75-11-hard-2.5,75-18-hard-2.5,75-26-hard-2.5,76-2-hard-6.0,76-3-hard-5.5,76-4-hard-6.0,76-11-hard-3.0,76-18-hard-3.0,76-25-hard-6.0,76-26-hard-5.5,76-27-hard-6.0,77-1-hard-6.0,77...78-11-hole-,77...78-11-hard-1.0-1.5,77...78-18-hole-,77...78-18-hard-1.0-1.5,77-28-hard-6.0,78-0-hard-6.0,78-6-hard-6.0,78-23-hard-6.0,78-29-hard-6.0,79-0-hard-5.5,79-3-teleporter-0,79-6-hard-5.5,79-7-hard-2.5,79-8-hard-3.0,79-9-hard-2.5,79-10-hard-3.0,79-11-hard-2.5,79-12-hard-3.0,79-13-hard-2.5,79-14-hard-3.0,79-15-hard-2.5,79-16-hard-3.0,79-17-hard-2.5,79-18-hard-3.0,79-19-hard-2.5,79-20-hard-3.0,79-21-hard-2.5,79-22-hard-3.0,79-23-hard-5.5,79-26-teleporter-0,79-29-hard-5.5,80-0-hard-6.0,80-6-hard-6.0,80-23-hard-6.0,80-29-hard-6.0,81-1-hard-6.0,81-5-hard-6.0,81-24-hard-6.0,81-28-hard-6.0,82-2-hard-6.0,82-3-hard-5.5,82-4-hard-6.0,82-25-hard-6.0,82-26-hard-5.5,82-27-hard-6.0|78-14-medic-2-Team Blue,78-15-medic-2-Team Blue,67-8-darkgreen-2-Team Blue,67-20-darkgreen-2-Team Blue,67-14-black-2-Team Blue,17-9-darkgreen-0-Team Red,17-21-darkgreen-0-Team Red,17-15-black-0-Team Red,6-14-medic-0-Team Red,6-15-medic-0-Team Red,13-9-player-0-Team Red,8-9-player-0-Team Red,17-13-player-0-Team Red,17-17-player-0-Team Red,13-21-player-0-Team Red,8-21-player-0-Team Red,67-12-player-0-Team Blue,67-16-player-0-Team Blue,71-20-player-0-Team Blue,76-20-player-0-Team Blue,76-8-player-0-Team Blue,71-8-player-0-Team Blue|Team Red-false-255.0-0.0-0.0,Team Blue-false-0.0-0.0-255.0}\n" +
            "coins\n" +
            "15\n" +
            "shop\n" +
            "[Rocket,bullet_fire.png,3,0,5,100,bullet,normal,fire,8.0,0,1.0,5,100.0,10.0,5.0,false,0.0,1,30.0]\n" +
            "[Machine Gun,bullet_mini.png,3,0,100,1000,bullet,normal,none,5.0,0,0.1,12,3.0,5.0,0.1,false,0.0,1,30.0]";
    public static final String[] items = new String[]{
            "Sniper Rifle,bullet_fire_trail.png,5,0,1,100,bullet,normal,fire_and_smoke,12.0,1,1.0,3,50.0,10.0,1.0,false,0.0,1,30.0",
            "Shield,shield.png,20,0,1,100,shield,1.0,5.0,100.0",
            "Mega bullet,bullet_large.png,3,0,2,20,bullet,normal,trail,3.25,3,1.0,5,100.0,25.0,4.0,true,0.0,1,30.0"
    };

    public static final String[] coolItems = new String[]{
            "[Blaster,bullet_laser.png,5,0,2000,2000,bullet,laser,none,1.0,0,0.1,1,100.0,16.0,0.0,false,0.0,1,30.0]\n",
            "[Grenade,mine.png,1,0,1,100,mine,300.0,300.0,200.0,2.0,2,75.0,30.0,true,20.0,0.02]\n"
    };

    public Scoreboard scoreboard;
    public TankNPC shopNPC;

    public TeamDeathmatch()
    {
        this.name = "Team Deathmatch";
        this.description = "Kill all your enemies!";
        this.enableKillMessages = true;
        this.playerKillCoins = 10;
    }

    @Override
    public void start()
    {
        ModAPI.loadLevel(levelString);

        ArrayList<Tank> arr = new ArrayList<>();
        for (Player p : Game.players)
            arr.add(p.tank);

        scoreboard = new Scoreboard("Team Deathmatch", Scoreboard.objectiveTypes.kills, arr, true).add();
        scoreboard.title.fontSize = 22;
        scoreboard.subtitle.text = "Leaderboard";
        shopNPC = new TankNPC("npc", 42, 13, Math.PI / 2, new TankNPC.MessageList("Use this shop at your own risk!", TankNPC.shopCommand), "Shop", 0, 255, 0);
        shopNPC.shopItems.addAll(Game.currentLevel.shop);

        ObstacleTeleporter.exitCooldown = 1000;

        for (Movable m : Game.movables)
        {
            if (m instanceof TankMedic)
            {
                TankMedic t = (TankMedic) m;
                t.health = 2;
                t.baseHealth = 2;
                t.baseModel = TankModels.fixed.base;
                t.colorModel = TankModels.fixed.color;
            }
        }

        for (String s : items)
            shopNPC.shopItems.add(Item.parseItem(null, s));

        if ((Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) && Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_CONTROL) || Math.random() < 0.1))
        {
            shopNPC.posX -= 50;

            TankNPC aCoolTank = new TankNPC("npc", 44, 13, Math.PI / 2, new TankNPC.MessageList("Hello! I'm just a passerby who sells toys.", "I sell very cool toys for very cheap prices.", "Why don't you give my toys a try?", TankNPC.shopCommand), "", 50, 200, 255);

            for (String s : coolItems)
                aCoolTank.shopItems.add(Item.parseItem(null, s));

            Game.addTank(aCoolTank);
            ModAPI.displayText(new FixedText(FixedText.types.actionbar, "A stranger has appeared on the bridge. And he sells some cool stuff...", 500));
        }
        Game.addTank(shopNPC);

        ModAPI.displayText(new FixedText(FixedText.types.title, "Team Deathmatch", 500, 255, 0, 0));
        ModAPI.displayText(new FixedText(FixedText.types.subtitle, "Kill all your enemies", 500, 255, 255, 255));
    }

    @Override
    public void update()
    {
        if (Game.game.window.shift)
        {
            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_B))
            {
                Game.game.window.pressedKeys.remove(((Integer) InputCodes.KEY_B));
                scoreboard.title.text += " :D";
            }
        }
    }
}