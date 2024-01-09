package org.obiz.sdtdbot.loghandlers;

import org.obiz.sdtdbot.Bot;
import org.obiz.sdtdbot.ServerGameShell;
import org.obiz.sdtdbot.bus.Events;
import org.obiz.sdtdbot.commands.GetTimeCommand;

public class PlayerJoinHandler extends AbstractLogHandler {
    public PlayerJoinHandler(ServerGameShell gameShell) {
        super(line ->
                line.contains("joined the game")||
                line.contains("INF PlayerLogin:")||
                line.contains("disconnected after")||
                        line.contains("left the game")
                , line -> {
            Bot.getEventBusInstance().post(new Events.DiscordMessage(line));
            GetTimeCommand.runAndParse_GT_Command(gameShell).thenAccept(s -> Bot.getEventBusInstance().post(new Events.DiscordMessage(s)));
        });
    }
}
