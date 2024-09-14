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

Only the JAR is needed if playing with others or making an extension.

Installation
---

You can download the JAR file from GitHub releases.

To create a project with the source code... well, there's been some changes
since v1.2.8+. Ask the Discord for help for now, I'll put some instructions here later.

New Features:
---
- Mixins

[Older Changelogs](changelog.md)