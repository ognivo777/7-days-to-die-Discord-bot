package org.obiz.sdtdbot.loghandlers;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.obiz.sdtdbot.Bot;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.bus.Events;

public class ServerStateChangeHandler extends AbstractLogHandler {
    public ServerStateChangeHandler() {
        super(line ->
                line.contains("INF Starting dedicated server") ||
                line.contains("INF [NET] ServerShutdown") ||
                line.contains("Shutdown handler: cleanup.") ||
                line.contains("[Steamworks.NET] Making server public")
                , line -> {
                    EventBus eventBus = Context.getContext().getEventBusInstance();
                    eventBus.post(new Events.DiscordMessage(line));
            if (line.contains("[Steamworks.NET] Making server public")) {
                eventBus.post(new Events.ServerStarted());
            } else if (line.contains("[Steamworks.NET] Server shutdown")) {
                eventBus.post(new Events.ServerStopped());
            }
        });
    }
}
