package org.obiz.sdtdbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obiz.sdtdbot.commands.InfoCommand;
import org.obiz.sdtdbot.commands.StopCommand;

import java.time.Instant;

public class Bot {
    public static final String BOT_VERSION = "2022.10";
    private static final Logger log = LogManager.getLogger(Bot.class);

    private Instant started;
    private Config config;
    private ServerHostShell hostShell;
    private ServerHostShell hostShellForGame;
    private ServerGameShell gameShell;
    private Discord discord;
    private boolean isStopped = false;

    public static void main(String[] args) {
        log.info("Hello!");
        Config config = new Config(args);
        new Bot(config).start();
    }

    public Bot(Config config) {
        this.started = Instant.now();
        this.config = config;
    }

    private Bot start() {
        log.info("Version " + BOT_VERSION);
        try {
            hostShell = new ServerHostShell(config);
            hostShellForGame = new ServerHostShell(config);
            gameShell = new ServerGameShell(config, hostShellForGame);
            discord = new Discord(config).init();
            discord.addCommand(new InfoCommand(this));
            discord.addCommand(new StopCommand(this, config.getOwnerDiscordID()));

        } catch (Exception e) {
            log.error(e);
            stop();
//            throw new RuntimeException(e);
        }
        return this;
    }

    public void stop() {
        if(!isStopped) {
            isStopped = true;
            if (gameShell != null) {
                log.debug("gameShell.close()");
                gameShell.close();
            }
            if (hostShellForGame != null) {
                log.debug("hostShellForGame.close()");
                hostShellForGame.close();
            }
            if (hostShell != null) {
                log.debug("hostShell.close()");
                hostShell.close();
            }
            if (discord != null) {
                log.debug("discord.shutdown()");
                discord.shutdown();
                log.debug("discord.shutdown()  complete");
            }
            System.exit(0);
        }
    }

    public String getStarted() {
        return started.toString();
    }
}
