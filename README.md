# Tanks Multiplayer Modding API

This is a multiplayer modding API for Tanks: The Crusades. I'll call this project Mod API for short.

I've often wanted to use more than the default obstacles and tanks to make my level ideas come true.
I wanted to make modding easier, while also having support in the base game.
So I created this modding API to allow others to create their own custom levels and games more easily.


How it works
---
Many custom modding objects will be added per each version. However, these are only for use when modding, and will not be accessible otherwise.
Once a custom game has been made, if it only uses features from the Mod API, it will be fully functional, even if a server hosts and plays the game with the clients.
This is done by sending the objects through the network.

How to use
---

To play with others as a client, only the JAR file is needed.
To create a custom game or level, the source code is needed.

Installation
---

You can download the JAR file [here](https://onedrive.live.com/download?cid=1E1C6A69D73A57B9&resid=1E1C6A69D73A57B9%21156&authkey=AJK5DlKvt3PfOJc).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

- `LevelEditorSelector`: Customizable and modular editor properties
- `ScreenLevelEditor.buttons`: Made adding buttons to the editor much simpler.
- A variety of selectors already added in
- Registered `ObstacleText`: ability to add text to level editor
- Made some super smooth transitions
- Extensions menu in options!
- Minimap input screen is finally back
  - default panning controls: u, h, j, and k

- Many hotbar improvements:
  - Shows individual item, mine, and shield cooldown
- Mine collision! This means throwable mines, blowable mines, freezable mines, and more!
- Implemented lava obstacle
- Many, many bug fixes


[Older Changelogs](changelog.md)