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

You can download the JAR file [here](https://github.com/pythoncoder1234/Tanks-Multiplayer-Modding-API/releases/download/v1.2.5/TanksModAPI_v1.2.5.jar).

To create a project with the source code, just set it up like a normal Tanks modding project or extension creation project.

New Features:
---

- Press middle button in editor to pick block
- Tank references have been idiot-proofed
  - A confirm deletion popup appears when attempting to delete a tank, if references to the tank are found
  - Duplicate tank names are no longer allowed
  - All value tanks point to a reference tank with the same name on save
    - If the reference tank is not found, a reference tank using the value tank is created 
- Scrolling up/down or pressing the up/down arrow in any number-based text box will increase/decrease the value
  - Hold shift to change by 0.1
  - Hold alt to change by 10
  - Hold shift+alt to change by 100
  - Some text boxes have multipliers assigned to them - sound options for example will change by 2 times the multiplier
- Transform after timer option
- Fix recursive spawns in tank references
- F3+[ = toggle sprinting the game
- Bug fixes

[Older Changelogs](changelog.md)