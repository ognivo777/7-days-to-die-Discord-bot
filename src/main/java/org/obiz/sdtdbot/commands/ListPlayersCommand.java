package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.ServerGameShell;

import java.util.function.Consumer;

public class ListPlayersCommand extends Command {
    private static final Logger log = LogManager.getLogger(ListPlayersCommand.class);
    private ServerGameShell shell;

    public ListPlayersCommand(ServerGameShell shell) {
        super("list", "Show players in game");
        this.shell = shell;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        runListPlayerCommand(consumer, shell);
    }

    public static void runListPlayerCommand(Consumer<String> consumer, ServerGameShell shell) {
        shell.executeCommand("lpi").thenAccept(commandResult -> {
            String lpiResult = commandResult.toString();
            log.debug("lpiResult: \n" + lpiResult);
            consumer.accept(lpiResult);
        }).exceptionally(throwable -> {
            log.error("Error on GT command: " +throwable.getMessage(), throwable);
            return null;
        });
    }
}
