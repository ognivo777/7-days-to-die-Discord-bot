package org.obiz.sdtdbot.loghandlers;

import com.google.common.eventbus.EventBus;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.bus.Events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerJoinHandler extends AbstractLogHandler {

    private static Pattern playerNameFromJionString = Pattern.compile(".+ INF GMSG: Player '([^']+)' joined the game");

    public PlayerJoinHandler() {
        super(line ->
                line.contains("joined the game")
                , line -> {
                    Matcher matcher = playerNameFromJionString.matcher(line);
                    if(matcher.matches()) {
                        String playerName = matcher.group(1);
                        EventBus eventBusInstance = Context.getContext().getEventBusInstance();
                        //Похоже надо вынести это в отдельные классы
                        eventBusInstance.post(new Events.DiscordMessage("**%s** join the game. Good luck!".formatted(playerName)));
                        eventBusInstance.post(new Events.PlayerJoined(playerName));
//                        GetTimeCommand.runAndParse_GT_Command(gameShell)
//                                .thenAccept(s -> eventBusInstance.post(new Events.DiscordMessage(s)));
                    }
        });
    }
}
