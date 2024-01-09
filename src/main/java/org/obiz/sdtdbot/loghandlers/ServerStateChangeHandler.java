package org.obiz.sdtdbot.loghandlers;

import com.google.common.eventbus.AsyncEventBus;
import org.obiz.sdtdbot.Bot;
import org.obiz.sdtdbot.bus.Events;

public class ServerStateChangeHandler extends AbstractLogHandler {
    private AsyncEventBus eventBus;
    public ServerStateChangeHandler(AsyncEventBus eventBus) {
        super(line ->
                line.contains("INF Starting dedicated server") ||
                line.contains("INF [NET] ServerShutdown") ||
                line.contains("Shutdown handler: cleanup.") ||
                line.contains("[Steamworks.NET] Making server public")
                , line -> {
            Bot.getEventBusInstance().post(new Events.DiscordMessage(line));
            if (line.contains("[Steamworks.NET] Making server public")) {
                eventBus.post(new Events.ServerStarted());
            }
        });
        this.eventBus = eventBus;
    }
}
