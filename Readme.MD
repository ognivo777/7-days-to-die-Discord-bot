
# Discord Bot for 7 Days to Die
<!-- TOC -->
* [Discord Bot for 7 Days to Die](#discord-bot-for-7-days-to-die)
  * [Overview](#overview)
  * [Features](#features)
    * [Notifications:](#notifications)
    * [Statistics:](#statistics)
    * [Server Time:](#server-time)
    * [In-Game Chat:](#in-game-chat)
    * [Server Management:](#server-management)
  * [Requirements](#requirements)
  * [Installation and Setup](#installation-and-setup)
  * [Usage](#usage)
    * [Commands](#commands)
  * [Contributing](#contributing)
<!-- TOC -->
## Overview
This Discord bot is designed to enhance your 7 Days to Die experience by providing a range of interactive features. It integrates seamlessly with your Discord server, offering notifications, statistics, and server management capabilities.

## Features

### Notifications:
* Receive notifications when players join or leave the game.
* Get updates on server status and events.
### Statistics:
* View detailed statistics about your gameplay sessions, including playtime, kills, deaths, and more.
### Server Time:
Easily check the current time on the game server. And update it, if you are op-role.
### In-Game Chat:
* See the in-game chat in real-time.
* Send messages directly to the game chat.
### Server Management:
* Start and stop the game server remotely.

## Requirements
* **SSH Access:** You need SSH access to the server where the game is running, using a user with permissions to access the log file.
* **Telnet Access:** The bot must be able to connect to the game server's Telnet port (it do it from remote host over ssh).

## Installation and Setup
1. Create discord bot as described [here](https://javacord.org/wiki/getting-started/creating-a-bot-account.html#use-the-invite-link) and save token.
2. Rename  `sample-config.properties` into `config.properties` and specify your settings
3. Run application using `screen` or *docker*.


## Usage
* **Invite the Bot:** Add the bot to your Discord server using link from bot stdout or log files.
### Commands
Use discord command prefix `/` to view and choose command.

**List of bot commands:**
* `/gt`: Get server time.
* `/say <text>`: Say something to in-game chat.
* `/run`: Start the game server.
* `/kill` (with game op role): Stop the game server.
* `/list` (with game op role): List players online.
* `/st` (with game op role): Set in-game day and time.
* `/kickall` (with game op role): Kick all from server.
* `/info`: Show bot version.

## Contributing
Contributions are welcome! Feel free to fork the repository, make changes, and submit a pull request.
