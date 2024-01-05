package org.obiz.sdtdbot;

import com.google.common.eventbus.AsyncEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obiz.sdtdbot.commands.*;
import org.obiz.sdtdbot.loghandlers.PlayerJoinHandler;
import org.obiz.sdtdbot.loghandlers.ServerStateChangeHandler;

import java.time.Instant;
import java.util.concurrent.Executors;

public class Bot {
    public static final String BOT_VERSION = "2022.10";
    private static final Logger log = LogManager.getLogger(Bot.class);

    public static Bot botInsance;
    private final AsyncEventBus eventBus;

    private Instant started;
    private Config config;
    private ServerHostShell hostShell;
    private ServerHostShell hostShellForGame;
    private ServerHostShell hostShellForLog;
    private ServerGameShell gameShell;
    private Discord discord;
    private boolean isStopped = false;
    private ServerFileTailer logProcessor;

    public static void main(String[] args) {
        //todo добавить DI через guice - конфиги, шину
        log.info("Hello!");
        Config config = new Config(args);
        new Bot(config).start();
    }

    public Bot(Config config) {
        Bot.botInsance = this;
        this.started = Instant.now();
        this.config = config;
        eventBus = new AsyncEventBus(Executors.newCachedThreadPool());
    }

    private Bot start() {
        log.info("Version " + BOT_VERSION);
        try {
            //init main mechanics

            hostShell = new ServerHostShell(config); //host SSH shell. Used for game start and stop commands
            hostShellForGame = new ServerHostShell(config); //base for game shell ( run telnet localhost only)
            hostShellForLog = new ServerHostShell(config); //base for game shell ( run telnet localhost only)
            gameShell = new ServerGameShell(config, hostShellForGame, eventBus);

            //it's important to run after init all shells!
            new Discord(config).init()
                    .thenAccept(d -> {
                        discord = d;
                        //add commands.. -убрать в класс Discord? или и тут норм?
                        discord.addCommand(new InfoCommand(this));
                        discord.addCommand(new StopCommand(this, config.getOwnerDiscordID()));
                        discord.addCommand(new GetTimeCommand(gameShell));
                        discord.addCommand(new KickAllCommand(gameShell, config));
                        discord.addCommand(new RunGameServerCommand(hostShell, config, eventBus));
                        discord.addCommand(new KillGameServerCommand(gameShell, hostShell, config));
                        eventBus.register(discord);
                    })
                    .exceptionally(throwable -> {
                        log.error("Error: ", throwable);
                        System.exit(0); //todo fix this dirty hack
                        return null;
                    });

            logProcessor = new ServerFileTailer(config, hostShellForLog, eventBus);
            logProcessor.addHandler(new PlayerJoinHandler());
            logProcessor.addHandler(new ServerStateChangeHandler());

//            connect each other with event bus
            eventBus.register(hostShell);
            eventBus.register(hostShellForGame);
            eventBus.register(gameShell);

            logProcessor.start();

        } catch (Exception e) {
            log.error("Error!", e);
            stop();
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

    public void sendMessage(String text) {
        discord.sendMessageToChannel(text);
    }
}
