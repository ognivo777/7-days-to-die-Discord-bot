package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.ServerGameShell;

import java.util.function.Consumer;

public class KickAllCommand extends Command {
    private static final Logger log = LogManager.getLogger(KickAllCommand.class);
    private ServerGameShell shell;

    public KickAllCommand(ServerGameShell shell) {
        super("kickall", "Kick all players", Context.getContext().getConfigInstance().getOpDiscordRole());
        this.shell = shell;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        shell.executeCommand("kickall \"Server maintenance. Restart needed for some reason.\"").thenAccept(kickAllResult -> {
            log.info("KickAll Done.");
            consumer.accept("KickAll Done.");
        }).exceptionally(throwable -> {
            log.error("Error on kickAll command: " +throwable.getMessage(), throwable);
            return null;
        });
    }
}
