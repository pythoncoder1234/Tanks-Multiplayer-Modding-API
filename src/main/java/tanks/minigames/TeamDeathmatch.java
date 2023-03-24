package tanks.minigames;

import basewindow.InputCodes;
import tanks.Game;
import tanks.Minigame;
import tanks.ModAPI;
import tanks.gui.menus.FixedText;
import tanks.gui.menus.Scoreboard;
import tanks.hotbar.item.Item;
import tanks.tank.TankNPC;

public class TeamDeathmatch extends Minigame
{
    public static final String levelString = "{85,30,0,175,0,0,40,0,0,100,50|2-3-hard-5.5,2-4-hard-6.0,2-25-hard-5.5,2-26-hard-6.0,3-2-hard-5.5,3-5-hard-6.0,3-24-hard-5.5,3-27-hard-6.0,4-1-hard-5.5,4-3-teleporter-1,4-6-hard-6.0,4-23-hard-5.5,4-26-teleporter-1,4-28-hard-6.0,5-1-hard-6.0,5-6-hard-5.5,5-7-hard-3.0,5-8-hard-2.5,5-9-hard-3.0,5-10-hard-2.5,5-12-hard-2.5,5-13-hard-3.0,5-14-hard-2.5,5-15-hard-3.0,5-16-hard-2.5,5-17-hard-3.0,5-18-hard-2.5,5-19-hard-3.0,5-20-hard-2.5,5-21-hard-3.0,5-22-hard-3.5,5-23-hard-6.0,5-28-hard-5.5,6-2-hard-6.0,6-11...11-hole,6-11-hard-0.5-2.0,6-18...18-hole,6-18-hard-1.0-2.0,6-27-hard-5.5,7-3-hard-3.0,7-11-hole,7-11-hard-1.0-2.0,7-18-hole,7-18-hard-0.5-2.0,7-26-hard-2.5,8-3-hard-2.5,8-11-hard-2.5,8-18-hard-3.0,8-26-hard-3.0,9-3-hard-3.0,9-11-hard-3.0,9-18-hard-2.5,9-26-hard-2.5,10-3-hard-2.5,10-11-hard-2.5,10-18-hard-3.0,10-26-hard-3.0,11-3-hard-3.0,11...12-11-hole,11-11-hard-1.0-2.0,11-18...18-hole,11-18-hard-0.5-2.0,11-26-hard-2.5,12-3-hard-2.5,12-11-hard-0.5-2.0,12-18-hole,12-18-hard-1.0-2.0,12-26-hard-3.0,13-3-hard-3.0,13-12-hole,13-12-hard-1.0-1.5,13-17-hole,13-17-hard-1.0-1.5,13-26-hard-2.5,14-3-hard-2.5,14-13-hard-2.5,14-14-hard-3.0,14-15-hard-2.5,14-16-hard-3.0,14-26-hard-3.0,15-3-hard-3.0,15-26-hard-2.5,16-3-hard-2.5,16-26-hard-3.0,17-3-hard-3.0,17-26-hard-2.5,18-3-hard-2.5,18-25-hard-5.5,18-26-hard-6.0,19-3-hard-3.0,19-27-hard-6.0,20-2-hard-6.0,20-28-hard-6.0,21-1-hard-6.0,21-3-teleporter-1,21-6-hard-5.5,21-23-hard-6.0,21-26-teleporter-1,21-28-hard-5.5,22-1-hard-5.5,22-6-hard-6.0,22-7-hard-3.0,22-8-hard-2.5,22-9-hard-3.0,22-10-hard-2.5,22-11-hard-3.0,22-12-hard-2.5,22-13-hard-3.0,22...22-14-hard-1.0-2.0,22-15-hard-1.0-2.0,22-16-hard-3.0,22-17-hard-2.5,22-18-hard-3.0,22-19-hard-2.5,22-20-hard-3.0,22-21-hard-2.5,22-22-hard-3.0,22-23-hard-5.5,22-28-hard-6.0,23-2-hard-5.5,23-5-hard-6.0,23-24-hard-5.5,23-27-hard-6.0,31...31-3-normal-0.5,31...31-4-normal-0.5,31...31-5-normal-0.5,31...31-6-normal-0.5,31...31-7-normal-0.5,31...31-8-normal-0.5,31-9-normal-0.5,31...31-19-normal-0.5,31...31-20-normal-0.5,31...31-21-normal-0.5,31...31-22-normal-0.5,31...31-23-normal-0.5,31...31-24-normal-0.5,31...31-25-normal-0.5,31-26-normal-0.5,36-7-sand,36...49-11-normal-1.0,36...49-15-normal-1.0,36-16-sand,36-17-sand,37-19-sand,37...37-22-sand,37...40-23-sand,37...37-24-sand,37...37-25-sand,37-26-sand,37-28-sand,38...38-1-sand,38-2-sand,38...40-3-sand,38...38-4-sand,38...40-5-sand,38...40-9-sand,38...38-12-breakable-0.5,38...38-13-breakable-0.5,38-14-breakable-0.5,38...40-18-sand,38...40-20-sand,38-28-sand,39...43-0-sand,39...42-1-sand,39...41-2-sand,39-4-sand,39...40-6-sand,39-7-sand,39...40-8-sand,39...40-10-sand,39...40-16-sand,39...40-17-sand,39...40-19-sand,39...40-21-sand,39...40-22-sand,39...40-24-sand,39-25-sand,39...40-26-sand,39...39-27-sand,39...40-28-sand,39...40-29-sand,40...40-4-sand,40...40-7-sand,40...40-25-sand,40-27-sand,41...42-3-water-1.0,41...41-4-water-1.0,41...41-5-water-1.0,41...41-6-water-1.0,41...41-7-water-1.0,41...41-8-water-1.0,41...41-9-water-1.0,41-10-water-1.0,41...41-16-water-1.0,41...41-17-water-1.0,41...41-18-water-1.0,41...41-19-water-1.0,41...41-20-water-1.0,41...41-21-water-1.0,41...41-22-water-1.0,41...41-23-water-1.0,41...41-24-water-1.0,41...41-25-water-1.0,41...41-26-water-1.0,41...41-27-water-1.0,41...41-28-water-1.0,41-29-water-1.0,42...43-2-water-1.0,42...44-4-water-2.5,42...44-5-water-2.5,42...44-6-water-2.5,42...44-7-water-2.5,42...44-8-water-2.5,42...44-9-water-2.5,42...44-10-water-2.5,42...44-16-water-2.5,42...44-17-water-2.5,42...44-18-water-2.5,42...44-19-water-2.5,42...44-20-water-2.5,42...44-21-water-2.5,42...44-22-water-2.5,42...44-23-water-2.5,42...44-24-water-2.5,42...44-25-water-2.5,42...44-26-water-2.5,42...44-27-water-2.5,42...44-28-water-2.5,42...44-29-water-2.5,43...44-1-water-1.0,43...45-3-water-2.5,44...45-0-water-1.0,44...46-2-water-2.5,45...47-1-water-2.5,45...46-4-water-1.0,45...46-5-water-1.0,45...46-6-water-1.0,45...45-7-water-1.0,45...45-8-water-1.0,45...45-9-water-1.0,45-10-water-1.0,45...45-16-water-1.0,45...45-17-water-1.0,45...45-18-water-1.0,45...45-19-water-1.0,45...45-20-water-1.0,45...45-21-water-1.0,45...45-22-water-1.0,45...45-23-water-1.0,45...45-24-water-1.0,45...45-25-water-1.0,45...45-26-water-1.0,45...45-27-water-1.0,45...45-28-water-1.0,45-29-water-1.0,46...47-0-water-2.5,46...47-3-water-1.0,46...47-7-sand,46...47-8-sand,46...47-9-sand,46...47-10-sand,46-16-sand,46...47-17-sand,46...48-18-sand,46...47-19-sand,46...47-20-sand,46...47-21-sand,46...47-22-sand,46...49-23-sand,46...46-24-sand,46...47-25-sand,46...47-26-sand,46...47-27-sand,46...46-28-sand,46...48-29-sand,47...48-2-water-1.0,47...48-4-sand,47...49-5-sand,47-6-sand,47...47-12-breakable-0.5,47...47-13-breakable-0.5,47-14-breakable-0.5,47...47-16-sand,47-24-sand,47-28-sand,48...48-0-water-1.0,48...48-1-water-1.0,48...50-3-sand,49-0-sand,49...51-1-sand,49...51-2-sand,49-4-sand,49...49-8-sand,49-9-sand,49...50-16-sand,49-24-sand,50...51-0-sand,50-4-sand,50-7-sand,50...50-17-sand,50...50-18-sand,50...50-19-sand,50...50-20-sand,50...50-21-sand,50-22-sand,52...53-5-sand,53-1-sand,54-0-sand,54...54-3-normal-0.5,54...54-4-normal-0.5,54...54-5-normal-0.5,54...54-6-normal-0.5,54...54-7-normal-0.5,54...54-8-normal-0.5,54-9-normal-0.5,54...54-19-normal-0.5,54...54-20-normal-0.5,54...54-21-normal-0.5,54...54-22-normal-0.5,54...54-23-normal-0.5,54...54-24-normal-0.5,54...54-25-normal-0.5,54-26-normal-0.5,55...55-0-sand,55-1-sand,58-25-hard-5.5,58-26-hard-6.0,58-27-hard-5.5,59-3-hard-6.0,59-4-hard-5.5,59-5-hard-6.0,59-28-hard-5.5,60-2-hard-6.0,60-23-hard-5.5,60-29-hard-5.5,61-1-hard-6.0,61-7-hard-6.0,61-23-hard-6.0,61-26-teleporter-0,61-29-hard-6.0,62-1-hard-5.5,62-4-teleporter-0,62-7-hard-5.5,62-8-hard-2.5,62-9-hard-3.0,62-10-hard-2.5,62-11-hard-3.0,62-12-hard-2.5,62-13-hard-3.0,62...62-14-hard-1.0-2.0,62-15-hard-1.0-2.0,62-16-hard-3.0,62-17-hard-2.5,62-18-hard-3.0,62-19-hard-2.5,62-20-hard-3.0,62-21-hard-2.5,62-22-hard-3.0,62-23-hard-5.5,62-29-hard-5.5,63-1-hard-6.0,63-7-hard-6.0,63-28-hard-5.5,64-2-hard-6.0,64-26-hard-2.5,64-27-hard-5.5,65-3-hard-6.0,65-4-hard-5.5,65-5-hard-6.0,65-26-hard-3.0,66-3-hard-3.0,66-26-hard-2.5,67-3-hard-2.5,67-26-hard-3.0,68-3-hard-3.0,68-26-hard-2.5,69-3-hard-2.5,69-26-hard-3.0,70-3-hard-3.0,70-26-hard-2.5,71-3-hard-2.5,71...71-13-hard-2.5,71-14-hard-2.5,71-15-hard-3.0,71-16-hard-2.5,71-26-hard-3.0,72-3-hard-3.0,72-12-hole,72-12-hard-1.0-1.5,72-17-hole,72-17-hard-1.0-1.5,72-26-hard-2.5,73-3-hard-2.5,73...74-11-hole,73...74-11-hard-1.0-1.5,73...74-18-hole,73...74-18-hard-1.0-1.5,73-26-hard-3.0,74-3-hard-3.0,74-26-hard-2.5,75-3-hard-2.5,75-11-hard-2.5,75-18-hard-2.5,75-25-hard-6.0,75-26-hard-5.5,75-27-hard-6.0,76-2-hard-6.0,76-3-hard-5.5,76-4-hard-6.0,76-11-hard-3.0,76-18-hard-3.0,76-28-hard-6.0,77-1-hard-6.0,77...78-11-hole,77...78-11-hard-1.0-1.5,77...78-18-hole,77...78-18-hard-1.0-1.5,77-23-hard-6.0,77-29-hard-6.0,78-0-hard-6.0,78-6-hard-6.0,78-23-hard-5.5,78-26-teleporter-0,78-29-hard-5.5,79-0-hard-5.5,79-3-teleporter-0,79-6-hard-5.5,79-7-hard-2.5,79-8-hard-3.0,79-9-hard-2.5,79-10-hard-3.0,79-11-hard-2.5,79-12-hard-3.0,79-13-hard-2.5,79-14-hard-3.0,79-15-hard-2.5,79-16-hard-3.0,79-17-hard-2.5,79-18-hard-3.0,79-19-hard-2.5,79-20-hard-3.0,79-21-hard-2.5,79-22-hard-3.0,79-23-hard-6.0,79-29-hard-6.0,80-0-hard-6.0,80-6-hard-6.0,80-24-hard-6.0,80-28-hard-6.0,81-1-hard-6.0,81-5-hard-6.0,81-25-hard-6.0,81-26-hard-5.5,81-27-hard-6.0,82-2-hard-6.0,82-3-hard-5.5,82-4-hard-6.0|6-14-medic-0-Team Red,6-15-medic-0-Team Red,78-14-medic-2-Team Blue,78-15-medic-2-Team Blue,18-13-black-0-Team Red,68-13-black-2-Team Blue,67-8-darkgreen-2-Team Blue,67-20-darkgreen-2-Team Blue,16-8-darkgreen-0-Team Red,16-20-darkgreen-0-Team Red,7-8-player-0-Team Red,11-8-player-0-Team Red,7-20-player-0-Team Red,11-20-player-0-Team Red,16-11-player-0-Team Red,16-17-player-0-Team Red,67-12-player-2-Team Blue,67-16-player-2-Team Blue,76-20-player-2-Team Blue,71-20-player-2-Team Blue,71-8-player-2-Team Blue,76-8-player-2-Team Blue|Team Red-false-255.0-0.0-0.0,Team Blue-false-0.0-0.0-255.0}\ncoins\n15\nshop\n[Rocket,bullet_fire.png,3,0,5,100,bullet,normal,fire,8.0,0,1.0,5,100.0,10.0,5.0,false,0.0,1,30.0]\n[Machine Gun,bullet_mini.png,3,0,100,1000,bullet,normal,none,5.0,0,0.1,12,3.0,5.0,0.1,false,0.0,1,30.0]";
    public static final String[] items = new String[] {
        "Sniper Rifle,bullet_fire_trail.png,5,0,1,100,bullet,normal,fire_and_smoke,12.0,1,1.0,3,50.0,10.0,1.0,false,0.0,1,30.0",
        "Shield,shield.png,20,0,1,100,shield,1.0,5.0,100.0",
        "Mega bullet,bullet_large.png,3,0,2,20,bullet,normal,trail,4.25,3,1.0,5,100.0,25.0,4.0,true,0.0,1,30.0"
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
        super.start();

        ModAPI.loadLevel(levelString);

        scoreboard = new Scoreboard("Team Deathmatch", Scoreboard.objectiveTypes.kills, level.teamsList).add();
        scoreboard.title.fontSize = 22;
        shopNPC = new TankNPC("npc", 42, 13, Math.PI / 2, new TankNPC.MessageList("Use this shop at your own risk!", TankNPC.shopCommand), "Shop", 0, 255, 0);
        shopNPC.shopItems.addAll(Game.currentLevel.shop);

        for (String s : items)
            shopNPC.shopItems.add(Item.parseItem(null, s));

        Game.addTank(true, shopNPC);

        ModAPI.displayText(new FixedText(FixedText.types.title, "Team Deathmatch", 500, 255, 0, 0));
        ModAPI.displayText(new FixedText(FixedText.types.subtitle, "Kill all your enemies", 500, 255, 255, 255));
    }

    @Override
    public void update()
    {
        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_B))
        {
            Game.game.window.pressedKeys.remove(((Integer) InputCodes.KEY_B));
            scoreboard.title.text += " :D";
        }
    }
}