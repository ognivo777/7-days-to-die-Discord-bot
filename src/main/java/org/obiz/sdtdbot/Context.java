package org.obiz.sdtdbot;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.obiz.sdtdbot.entity.PlayerInfo;
import org.obiz.sdtdbot.exceptions.ExceptionContextNotInitialazed;

import java.util.ArrayList;
import java.util.List;

public class Context {
    private static Context contextInstanse;


    private final Config config;
    private final Discord discord;
    private final Bot bot;
    private final AsyncEventBus eventBus;
    private Server server;

    private List<PlayerInfo> playerInfoHistory = new ArrayList<>();

    private Context(AsyncEventBus eventBus, Config config, Discord discord, Bot bot, Server server) {
        this.eventBus = eventBus;
        this.config = config;
        this.discord = discord;
        this.bot = bot;
        this.server = server;
    }

    public static Context getContext() {
        if(contextInstanse !=null)
            return contextInstanse;
        else
            throw new ExceptionContextNotInitialazed();
    }

    public static void initWith(Config config, AsyncEventBus eventBus, Discord discord, Server server, Bot bot) {
        contextInstanse = new Context(eventBus, config, discord, bot, server);
    }

    public Config getConfigInstance() {
        return config;
    }

    public EventBus getEventBusInstance() {
        return eventBus;
    }

    public Server getServerState() {
        return server;
    }

    public List<PlayerInfo> getPlayersHistory() {
        return playerInfoHistory;
    }
}
