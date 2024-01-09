package org.obiz.sdtdbot;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obiz.sdtdbot.bus.Events;
import org.obiz.sdtdbot.commands.*;
import org.obiz.sdtdbot.loghandlers.PlayerJoinHandler;
import org.obiz.sdtdbot.loghandlers.ServerStateChangeHandler;

import java.time.Instant;
import java.util.concurrent.Executors;

public class Bot {
    public static final String BOT_VERSION = "2022.10";
    private static final Logger log = LogManager.getLogger(Bot.class);

    private static Bot botInsance;
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
            hostShell = new ServerHostShell("pure host"); //host SSH shell. Used for game start and stop commands
            hostShellForGame = new ServerHostShell("host for telnet"); //base for game shell ( run telnet localhost only)
            gameShell = new ServerGameShell(hostShellForGame);
            hostShellForLog = new ServerHostShell("host for tail"); //base for game shell ( run telnet localhost only)

            //it's important to run after init all shells!
            new Discord(config).init()
                    .thenAccept(d -> {
                        discord = d;
                        //add commands.. -убрать в класс Discord? или и тут норм?
                        discord.addCommand(new InfoCommand(this));
                        discord.addCommand(new StopCommand(this, config.getOwnerDiscordID()));
                        discord.addCommand(new GetTimeCommand(gameShell));
                        discord.addCommand(new ListPlayersCommand(gameShell));
                        discord.addCommand(new KickAllCommand(gameShell));
                        discord.addCommand(new RunGameServerCommand(hostShell));
                        discord.addCommand(new KillGameServerCommand(gameShell, hostShell));
                        eventBus.register(discord);
                    })
                    .exceptionally(throwable -> {
                        log.error("Error: ", throwable);
                        System.exit(0); //todo fix this dirty hack
                        return null;
                    });

            logProcessor = new ServerFileTailer(config, hostShellForLog, eventBus);
            logProcessor.addHandler(new PlayerJoinHandler(gameShell));
            logProcessor.addHandler(new ServerStateChangeHandler(eventBus));

//            connect each other with event bus
            eventBus.register(hostShell);
            eventBus.register(hostShellForGame);
            eventBus.register(hostShellForLog);
            eventBus.register(gameShell);

            logProcessor.start();

        } catch (Exception e) {
            log.error("Error!", e);
            stop();
        }
        return this;
    }

    @Subscribe
    public void onEventBStopBot(Events.StopBot event) {
        log.info("StopBot event: " + event.getReason());
        stop();
    }

    private void stop() {
        //todo use events to stop shells
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

    public Discord getDiscord() {
        return discord;
    }

    public boolean isStopped() {
        return isStopped;
    }

    public static Bot getInstance() {
        return botInsance;
    }

    public AsyncEventBus getEventBus() {
        return eventBus;
    }

    public Config getConfig() {
        return config;
    }

    public static AsyncEventBus getEventBusInstance(){
        return botInsance.getEventBus();
    }

    public static Config getConfigInstance() {
        return botInsance.getConfig();
    }
}
