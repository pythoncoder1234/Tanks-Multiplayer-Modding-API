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

You can download the JAR file [here](https://onedrive.live.com/download?resid=1E1C6A69D73A57B9%21244&authkey=!ACyclUOE9MTa9yo).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

- **Reference spawns** (off by default)
- Getting closer and closer to full 1.5.2d multiplayer compatibility!
- Crusade Import overhaul
- Minimap overhaul
- Fixed multiplayer chat
- F3+D = clear chat (like in minecraft)
- Improved Tank AI
- Did some merging
- Fixed bugs

[Older Changelogs](changelog.md)