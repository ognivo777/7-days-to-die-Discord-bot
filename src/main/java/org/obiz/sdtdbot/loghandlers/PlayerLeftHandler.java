package org.obiz.sdtdbot.loghandlers;

import com.google.common.eventbus.EventBus;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.bus.Events;
import org.obiz.sdtdbot.entity.PlayerInfo;

import java.util.Comparator;
import java.util.List;
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
                        EventBus eventBus = Context.getContext().getEventBusInstance();
                        //Похоже надо вынести это в отдельные классы
                        eventBus.post(new Events.DiscordMessage("**%s** left the game after %s minutes.".formatted(playerName, minutesInGame)));
                        eventBus.post(new Events.PlayerLeft(playerName));


                        //todo may be move to new PlayerRepository or else
                        List<PlayerInfo> allPlayersHistory = Context.getContext().getPlayersHistory();
                        List<PlayerInfo> playerHistory = allPlayersHistory.stream()
                                .filter(playerInfo -> playerInfo.getName().equalsIgnoreCase(playerName))
                                .sorted(Comparator.comparing(PlayerInfo::getServerLastSeen))
                                .toList();
                        allPlayersHistory.removeAll(playerHistory);
                        if(playerHistory.size()>1){
                            PlayerInfo started = playerHistory.get(0);
                            PlayerInfo finished = playerHistory.get(playerHistory.size()-1);
                            int levels = finished.getLevel() - started.getLevel();
                            int kills = finished.getZombies() - started.getZombies();
                            int score = finished.getScore() - started.getScore();

                            double distance = 0;
                            for (int i = 1; i < playerHistory.size(); i++) {
                                PlayerInfo current = playerHistory.get(i);
                                PlayerInfo previous = playerHistory.get(i-1);
                                double step = Math.sqrt(
                                    Math.pow(current.getPosX()-previous.getPosX(), 2) +
                                    Math.pow(current.getPosZ()-previous.getPosZ(), 2)
                                );
                                distance+=step;
                            }

                            eventBus.post(new Events.DiscordMessage(
                                    ("**%s** progress: %d levels, %d kills, %d score. And moved to %.2fm").formatted(
                                            playerName,
                                            levels,
                                            kills,
                                            score,
                                            distance
                                    )
                            ));


                        }
                    }
                });
    }
}
