package org.obiz.sdtdbot.loghandlers;

import org.obiz.sdtdbot.Bot;

public class PlayerJoinHandler extends LogHandler{
    public PlayerJoinHandler() {
        super(line ->
                line.contains("joined the game")||
                line.contains("INF PlayerLogin:")||
                line.contains("disconnected after")||
                        line.contains("left the game")
                , line -> {
            Bot.botInsance.sendMessage(line);
        });
    }
}
