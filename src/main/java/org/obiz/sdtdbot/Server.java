package org.obiz.sdtdbot;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.eventbus.Subscribe;
import org.obiz.sdtdbot.bus.*;
import org.obiz.sdtdbot.commands.ListPlayersCommand;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements ServerStartedListener, ServerStoppedListener, PlayerJoinedListener, PlayerLeftListener, BotStartPhaseOneListener {
    private static Pattern playerNameFromLpiCommandOutput = Pattern.compile("\\d+\\. id=(\\d+), (.+)");

    private BiMap<String, String> playersOnline = HashBiMap.create();
    private ServerGameShell gameShell;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private AtomicBoolean serverAlive = new AtomicBoolean(false);

    public Boolean hasPlayersOnline() {
        return initialized.get() && serverAlive.get() && !playersOnline.isEmpty();
    }

    @Override
    @Subscribe
    public void onPlayerJoined(Events.PlayerJoined event) {
        updatePlayersOnlineInfo();
    }

    @Override
    @Subscribe
    public void onPlayerLeft(Events.PlayerLeft event) {
        updatePlayersOnlineInfo();
    }

    @Override
    @Subscribe
    public void onServerStarted(Events.ServerStarted event) {
        serverAlive.set(true);
        updatePlayersOnlineInfo();
    }

    @Override
    @Subscribe
    public void onServerStopped(Events.ServerStopped event) {
        serverAlive.set(false);
    }

    @Override
    @Subscribe
    public void onBotStartPhaseOne(Events.BotStartPhaseOne event) {
        gameShell = event.gameShell();
        initialized.set(true);
        serverAlive.set(true);
        playersOnline.clear();
        updatePlayersOnlineInfo();
    }

    private void updateServerState() {
        serverAlive.set(gameShell.getIsAlive());
    }

    private void updatePlayersOnlineInfo() {
        ListPlayersCommand.runListPlayerCommand(line -> {
            playersOnline.clear();
            Arrays.stream(line.split("\n"))
                    .map(playerNameFromLpiCommandOutput::matcher)
                    .filter(Matcher::matches)
                    .forEach(matcher -> {
                        String playerId = matcher.group(1);
                        String playerName = matcher.group(2);
                        playersOnline.put(playerId, playerName);
                    });
        }, gameShell);
    }

    public BiMap<String, String> getPlayersOnline() {
        return playersOnline;
    }
}
