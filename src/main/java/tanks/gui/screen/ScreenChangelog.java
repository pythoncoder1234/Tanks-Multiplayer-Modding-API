package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.util.ArrayList;
import java.util.Arrays;

public class ScreenChangelog extends Screen
{
    public Screen prev = Game.screen;
    public ArrayList<String> pages = new ArrayList<>();
    public int currentPage;
    public String[] pageContents;

    public ScreenChangelog()
    {
        super(350, 40, 380, 60);

        this.music = "menu_options.ogg";
        this.musicID = "menu";

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;
    }

    public void setup()
    {
        for (Changelog l: Changelog.logs)
        {
            if (l.shouldAdd())
                this.add(l.pages);
        }
    }

    public void add(String[] log)
    {
        pages.addAll(Arrays.asList(log));
    }

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Done", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = prev;
            Game.lastVersion = Game.version;
            ScreenOptions.saveOptions(Game.homedir);
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    Button next = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            currentPage++;
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    Button previous = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            currentPage--;
            pageContents = pages.get(currentPage).split("\n");
        }
    }
    );

    @Override
    public void update()
    {
        if (pageContents == null)
            pageContents = pages.get(currentPage).split("\n");

        next.enabled = currentPage < pages.size() - 1;
        previous.enabled = currentPage > 0;

        next.update();
        previous.update();
        quit.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (pageContents == null)
            pageContents = pages.get(currentPage).split("\n");

        next.enabled = currentPage < pages.size() - 1;
        previous.enabled = currentPage > 0;

        if (next.enabled || previous.enabled)
        {
            next.draw();
            previous.draw();
        }

        quit.draw();

        Drawing.drawing.setColor(0, 0, 0);

        if (pageContents != null)
        {
            for (int i = 0; i < pageContents.length; i++)
            {
                String s = pageContents[i];

                if (s.startsWith("*"))
                {
                    Drawing.drawing.setInterfaceFontSize(this.titleSize);
                    s = s.substring(1);
                }
                else
                    Drawing.drawing.setInterfaceFontSize(this.textSize);

                Drawing.drawing.drawInterfaceText(this.centerX - 400, this.centerY + (-(pageContents.length - 1) / 2.0 + i) * this.objYSpace / 2 - this.objYSpace, s, false);
            }
        }
    }

    public static class Changelog
    {
        public static ArrayList<Changelog> logs = new ArrayList<>();

        public String[] pages;
        public String version;

        public Changelog(String version, String[] log)
        {
            this.version = version;
            this.pages = log;
            logs.add(this);
        }

        public boolean shouldAdd()
        {
            return Game.compareVersions(this.version, Game.lastVersion) > 0;
        }

        public static void setupLogs()
        {
            new Changelog("v1.0.0", new String[]
                    {
                            """
*What's new in Tanks v1.0.0:

*New features:

All-new adventure crusade
New snow obstacle: melts, slows tanks and bullets
Crusades can now be shared in parties
Mines now destroy bullets in range
Freezing bullets make the ground slippery
Mac users can now launch directly from the Jar
Adjusted tank coins and spawn rates
New update changelog screen""",

                            """
*Levels:

Max level size increased
More variation in level generator

*Items:

New mine item
Add items and shops in individual levels
New item templates
New item textures""",

                            """
*User interfaces:

New "restart" button on the pause menu
New "about" screen with links
List reordering for item and crusade level lists
New "test controls" button for mobile input options
New fullscreen mode
White text is now used in dark levels
New chat background
Title text is now larger
Mobile UI scale improved
Pre-game user highlighting in multiplayer
New tank icons in player chat
New color selection sliders
New UI glow effects
Party lobby menu improvements
And many, many more minor improvements""",

                            """
*Graphics:

Updated bullet effects
Updated health indicators
Flashier 3D fireworks

*Audio:

New volume controls
New songs for battle, crusade, and battle victory/defeat
New sound effects


...and countless bug fixes and other minor improvements. Enjoy!"""
                    });

            new Changelog("v1.1.0", new String[]
                    {
                            """
*What's new in Tanks v1.1.0:

*New features:

Added mustard tank which shoots over walls
Added orange-red tank which shoots explosive bullets
Added gold tank which boosts its allies' speed
Added boost panel
Added no bounce block
Added block which breaks when hit by a bullet
Added block which explodes when touched
Added light block

*Levels:

New time limit option for levels
Added lighting options to levels""",

                            """
*Options:

Added shadows and shadow quality option
Added option to show time elapsed
Added keybind to instantly start a level

*More:

Made some tanks smarter
Bullets harmless to you (friendly fire off) now flash
Added a new extension API
...and the usual bug fixes and various minor improvements"""
                    });

            new Changelog("v1.1.1", new String[]
                    {
                            """
*What's new in Tanks v1.1.1:

Split fast/fancy/super graphics setting into multiple options
Reduced certain particle effects to improve performance
New option to change intensity of particle effects
Several bug fixes and minor improvements"""
                    });

            new Changelog("v1.1.2", new String[]
                    {
                            """
*What's new in Tanks v1.1.2:

Added new sound for arc bullets
Added support for custom resources in extensions
Added support for custom tank models"""
                    });

            new Changelog("v1.1.3", new String[]
                    {
                            """
*What's new in Tanks v1.1.3:

Explosive blocks now have a delay if triggered by other explosions
Several bug fixes and other minor improvements"""
                    });

            new Changelog("v1.2.0", new String[]
                    {
                            """
*What's new in Tanks v1.2.0:

*New features:

Added light pink tank which gets angry when it sees you
Added mimic tank which mimics the behavior of a nearby tank
Dummy tanks can be now used in the level editor
Added support for Steam peer-to-peer multiplayer
You can now override the game's default resources

*Balancing:

Tanks no longer take damage after the battle has ended
Blue tank electricity now only arcs between 4 targets (from 6)
Cyan tank freeze duration decreased by 1 second
Large timed levels have longer timers
Orangered tank is now immune to explosion damage

""",

                            """
*More:

Improved the crusade editor level edit menu
Changed light block appearance
Fireworks are more varied and prettier!
Medic tank cross is now green
Performance improvements
...and, of course, bug fixes and other improvements"""
                    });

            new Changelog("v1.2.1", new String[]
                    {
                            """
*What's new in Tanks v1.2.1:

Added party host options menu
Added option to disable all friendly fire in parties
Added option to change the party countdown timer
Added auto ready multiplayer option
Added fullscreen button (in addition to the key)
Arc bullets now have colored shadows
Teleporter orbs are now the color of the teleporting tank
Player spawns are now more spread out in versus mode
Flash from bullets harmless to you (friendly fire off) is bigger
Bug fixes and other minor improvements"""
                    });

            new Changelog("v1.2.2", new String[]
                    {
                            """
*What's new in Tanks v1.2.2:

*New features:

Added crusade statistics screen
Added music for editor and dark levels
Added deterministic mode for speedrunners
Added built-in item templates
Added option to show level names in crusades
Added indicator for where arc bullets will land
Added mini tank to level editor
""",

                            """
*Improvements:

Hovering over the item bar slots shows keybinds
Leaving and rejoining a party crusade before a new level is
    loaded recovers progress
Explosive blocks now award players coins for kills
Level timer now shows in editor after playing from 'My levels'
Reworked the 'Save this level' button
You can now save levels you play in parties
Bug fixes and other minor improvements"""
                    });

            new Changelog("v1.3.0", new String[]
                    {
                            """
*What's new in Tanks v1.3.0:

*New features:

Added all-new castle crusade
Added cut, copy, and paste to level editor
Added multishot and shot spread options to bullets
Added crusade option to disable respawning tanks

*Sounds and music:

Added individual battle music tracks for each tanks
New editor music which changes based on tanks present
New sounds for winning and losing battles""",

                            """
*User interfaces:

Improved lists with search, sort, and jump buttons
New window options menu for resolution and fullscreen
New option to warn before closing unsaved work
New option to prevent mouse leaving the window bounds

*Improvements:

Cyan tanks are now immune to freeze bullets and ice
Improved game title appearance
Huge rendering improvements
Added translation support
Holes are now bigger
More animations on the crusade stats screen
Added native support for ARM64 (M1) architecture on Mac
Bug fixes and other minor improvements"""
                    });

            new Changelog("v1.3.1", new String[]
                    {
                            """
*What's new in Tanks v1.3.1:

Added maximum framerate option
Scroll while holding the zoom key for manual zoom
Added keybinds to manually zoom in and out
Added automatic zoom mode with keybind
Improved keybindings screen
Bug fixes and other minor improvements
"""
                    });

            new Changelog("v1.3.2", new String[]
                    {
                            """
*What's new in Tanks v1.3.2:

Changed appearance of electric bullets
Arc bullets can now be set to bounce
Bug fixes and other minor improvements
"""
                    });

            new Changelog("v1.4.0", new String[]
                    {
                            """
*What's new in Tanks v1.4.0:

*New features:

Added tank editor and custom tanks
Added air bullet which pushes things
Added homing bullet which moves towards targets
Added light blue tank which uses air bullets
Added salmon tank which uses homing bullets
Added beginner crusade

*Tank behavior:

Tanks now try to avoid walls
Tanks now see through destructible blocks
Tanks now avoid explosive blocks""",

                            """
*Balancing:

Boss tank now only spawns 5 tank types
Cooldowns are now per item
Made random level teleporters rarer
Increased player tank acceleration
Nerfed castle crusade level 'Castle artillery'

*Graphics:

New tank textures and models
Updated crusade info and stats screen background
Updated item icons
Made UI icons more colorful
Updated 3D explosion particle effect
Added indicator when new shop items are available
Added bullet cooldown indicator to hotbar""",

                            """
*More:

New editor music
Shop is now available in versus mode
Increased some textbox character limits
IP address can now be hidden in parties

Bug fixes and other minor improvements
"""
                    });

            new Changelog("v1.4.1", new String[]
                    {
                            """
*What's new in Tanks v1.4.1:

Added bandwidth usage to info bar
Fixed a memory leak with custom tank music
Fixed a bug with spawning tanks in multiplayer
"""
                    });

            new Changelog("v1.5.0", new String[]
                    {
                            """
*What's new in Tanks v1.5.0:

*New arcade mode minigame:

Point system based on tank kills
Continuously spawning enemy tank waves
Respawn if you die
Time limit of 2 minutes and 12 seconds
A rampage system for destroying tanks in a row
Tanks drop items that you can use to your advantage
Frenzy mode: destroy all you can when time runs out!
Point bonuses judging your performance at the end

""",
                            """
*Crusades:

Font is now varied across statistics for readability
Added crusade descriptions for built-in crusades
Built-in crusades now track your best completion time,
which you can see directly from the crusade screen
Completed crusade runs can be compared to your best run

*Options:

Reorganized options screens
Added new profile customization section to options
You can now use a custom tank color in singleplayer
Added option to show bullets under terrain
Added 30 FPS deterministic mode

""",
                            """
*More:

Updated menu music
Added item switching indicator on tank
Updated tutorial to be more exciting
Removed laser from versus mode
Tanks will not explode on death if killed right after spawning
Changed fireworks appearance
Bug fixes and other minor improvements
"""
                    }
            );

            new Changelog("v1.5.1", new String[]
                    {
                            """
*What's new in Tanks v1.5.1:

Bug fixes
"""
                    }
            );

            new Changelog("v1.5.2", new String[]
                    {
                            """
*What's new in Tanks v1.5.2:

Improved and adjusted tank AI
Added new 'Bullet avoid type' to tank customization
Added 'Sidewind' and 'Backwind' sight behaviors
Added all the missing features to the mobile version
Bug fixes and other minor improvements
"""
                    }
            );

        }
    }
}