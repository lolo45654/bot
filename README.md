# Bot

A project creating minecraft bots for 1.20.6.

## Usage

Note that the project is licensed under AGPL 3.0!

### Paper

Under paper, you have the option to run the following commands:

- /bot (`bot.use`)<br>
  Shows a menu with options to modify simple bots.
- /bot totem (`bot.totem`)<br>
  Spawns a simple bot using the default totem settings.
- /bot shield (`bot.shield`)<br>
  Spawns a simple bot using the default shield settings.
- /bot control (`bot.control`)<br>
  This will control the player and do actions on their behalf using the advanced bot.<br>
  **NOTE: Currently there's no way of resetting that effect.**
- /bot spawn (`bot.spawn`)<br>
  Similar to the simple bot, it spawns a fake player, now with advanced capabilities though.
- /bot removeall (`bot.removeall`)<br>
  Clears all bots, useful as bots load chunks.
- /bot count (`bot.count`)<br>
  Show how many bots there are!

### Fabric

As of right now, you can't use the bot on a fabric *server*.
You may however, use it on the client.

These commands are available on the client:

- /cbot disable<br>
  Clears the bot.
- /cbot enable \<goal><br>
  Starts the bot.

In addition to that, there's a button that leads you to an extra menu with some more controls.
*Only appears while running the bot!*

## Contributing

Go ahead! I'm going to write a guide later.