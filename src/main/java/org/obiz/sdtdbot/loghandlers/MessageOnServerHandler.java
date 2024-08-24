package org.obiz.sdtdbot.loghandlers;

import com.google.common.collect.BiMap;
import com.google.common.eventbus.EventBus;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.bus.Events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageOnServerHandler extends AbstractLogHandler{
    private static Pattern PARSE_MESSAGE_PATTERN = Pattern.compile(".+INF Chat \\(from '([^']+)', entity id '([^']+)', to '([^']+)'\\): (.+)");
    public MessageOnServerHandler() {
        super(line-> {
            return line.contains("INF Chat (from")
                    //&& line.contains("to 'Global'")
                    ;
        }, line-> {
            EventBus eventBusInstance = Context.getContext().getEventBusInstance();
            BiMap<String, String> playersOnline = Context.getContext().getServerState().getPlayersOnline();
            Matcher matcher = PARSE_MESSAGE_PATTERN.matcher(line.trim());
            if(matcher.matches()) {
                String from = matcher.group(1);
                String entityId = matcher.group(2);
                String to = matcher.group(3);
                String message = matcher.group(4);
                String playerName = playersOnline.get(entityId);
                eventBusInstance.post(new Events.DiscordMessage("New message from: **%s** to %s:\n`%s`".formatted(
                        playerName!=null?playerName:"<>",
                        to,
                        message
                )));
            }
        });
    }
}
