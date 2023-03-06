package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.ServerGameShell;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetTimeCommand extends Command {
    private static final Logger log = LogManager.getLogger(GetTimeCommand.class);
    private static Pattern gt_parse = Pattern.compile(".*Day (\\d+), (\\d\\d?:\\d\\d?).*");
    private ServerGameShell shell;

    public GetTimeCommand(ServerGameShell shell) {
        super("gt", "Show in game time");
        this.shell = shell;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        shell.executeCommand("gt").thenAccept(commandResult -> {
            String gtResult = commandResult.lastLine().split("\n")[0].trim();
            log.info("gtResult: " + gtResult);
            Matcher m = gt_parse.matcher(gtResult);
            if (m.matches()) {
                log.info("GT Matches");
                int day = Integer.parseInt(m.group(1));
                //TODO get red night period from 'ggs'
                int daysLeft = 7 - day % 7;
                if (daysLeft == 7) {
                    gtResult += ". Tonight they coming.";
                } else {
                    gtResult += ". " + daysLeft + " days for red night.";
                }
            }
            consumer.accept(gtResult);
        }).exceptionally(throwable -> {
            log.error("Error on GT command: " +throwable.getMessage(), throwable);
            return null;
        });
    }
}
