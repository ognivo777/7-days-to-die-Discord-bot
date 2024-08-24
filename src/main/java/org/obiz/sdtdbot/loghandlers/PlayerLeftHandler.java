package org.obiz.sdtdbot.loghandlers;

import com.google.common.eventbus.EventBus;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.bus.Events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerLeftHandler extends AbstractLogHandler {

    private static Pattern playerNameFromDisconnectedString = Pattern.compile(".+ Player (.+) disconnected after (.+) minutes");

    public PlayerLeftHandler() {
        super(line ->
                        line.contains("disconnected after")
                , line -> {
                    Matcher matcher = playerNameFromDisconnectedString.matcher(line.strip());
                    if(matcher.matches()) {
                        String playerName = matcher.group(1);
                        String minutesInGame = matcher.group(2);
                        EventBus eventBusInstance = Context.getContext().getEventBusInstance();
                        //Похоже надо вынести это в отдельные классы
                        eventBusInstance.post(new Events.DiscordMessage("**%s** left the game after %s minutes.".formatted(playerName, minutesInGame)));
                        eventBusInstance.post(new Events.PlayerLeft(playerName));
                    }
                });
    }
}
