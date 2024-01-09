package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.ServerGameShell;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListPlayersCommand extends Command {
    private static final Logger log = LogManager.getLogger(ListPlayersCommand.class);
    private ServerGameShell shell;

    public ListPlayersCommand(ServerGameShell shell) {
        super("list", "Show players in game");
        this.shell = shell;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        shell.executeCommand("lpi").thenAccept(commandResult -> {
            String gtResult = commandResult.lastLine().split("\n")[0].trim();
            log.info("lpiResult: " + gtResult);
            consumer.accept(gtResult);
        }).exceptionally(throwable -> {
            log.error("Error on GT command: " +throwable.getMessage(), throwable);
            return null;
        });
    }
}
