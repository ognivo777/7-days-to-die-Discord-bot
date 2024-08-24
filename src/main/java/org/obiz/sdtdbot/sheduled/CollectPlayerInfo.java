package org.obiz.sdtdbot.sheduled;

import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.ServerGameShell;
import org.obiz.sdtdbot.entity.PlayerInfo;

import java.util.List;

public class CollectPlayerInfo extends AbstractScheduledTask {

    private ServerGameShell shell;

    public CollectPlayerInfo(ServerGameShell shell, int maxSleepCount) {
        super(skippedCount -> Context.getContext().getServerState().hasPlayersOnline() || skippedCount >= maxSleepCount);
        this.shell = shell;
    }

    protected void tick() {
        shell.executeCommand("lp").thenAccept(shellCommandResult -> {
            List<String> lines = shellCommandResult.getResult();
            lines.stream()
                    .filter(s -> s.matches("^\\d+\\. id=.+"))
//                    .map(s -> s.replaceAll("(\\d), ([-\\d])", "$1 $2"))
//                    .map(s -> s.replaceAll("^\\d+\\. +", ""))
                    .map(PlayerInfo::new).forEach(playerInfo -> {
                        System.out.println("playerInfo = " + playerInfo);
                    });
        });
    }
}
