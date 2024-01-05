package org.obiz.sdtdbot.loghandlers;

import org.obiz.sdtdbot.Bot;

public class ServerStateChangeHandler extends LogHandler {
    public ServerStateChangeHandler() {
        super(line ->
                line.contains("INF Starting dedicated server") ||
                line.contains("INF [NET] ServerShutdown") ||
                line.contains("Shutdown handler: cleanup.") ||
                line.contains("[Steamworks.NET] Making server public")
                , line -> {
            Bot.botInsance.sendMessage(line);
        });

    }
}
