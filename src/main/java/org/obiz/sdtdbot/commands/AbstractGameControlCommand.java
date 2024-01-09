package org.obiz.sdtdbot.commands;

import org.obiz.sdtdbot.ServerGameShell;
import org.obiz.sdtdbot.ServerHostShell;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractGameControlCommand extends Command {
    protected ServerGameShell gameShell;
    protected ServerHostShell shell;

    public AbstractGameControlCommand(String command, String description, String roleName) {
        super(command, description, roleName);
    }

    protected CompletableFuture<Boolean> checkServerProcessStatus() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        shell.executeCommand("ps -ef | grep --color=never 7DaysToDieServer", false).thenAccept(commandResult -> {
            String lines = commandResult.mergedLines();
            if(lines.contains("7DaysToDieServer.x86_64")) {
                result.complete(true);
            } else {
                result.complete(false);
            }
        });
        return result;
    }
}
