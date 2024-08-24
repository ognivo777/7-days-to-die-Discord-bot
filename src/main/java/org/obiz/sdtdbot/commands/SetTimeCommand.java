package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.ServerGameShell;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.obiz.sdtdbot.commands.GetTimeCommand.runAndParse_GT_Command;

public class SetTimeCommand extends Command {
    private static final Logger log = LogManager.getLogger(SetTimeCommand.class);
    public static final String DAY = "day";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    private ServerGameShell shell;

    public SetTimeCommand(ServerGameShell shell) {
        super("st", "Set game time", Context.getContext().getConfigInstance().getOpDiscordRole());
        addOption(SlashCommandOption.createLongOption(DAY, "Day", true, 1, 1000));
        addOption(SlashCommandOption.createLongOption(HOUR, "Hour", true, 0, 23));
        addOption(SlashCommandOption.createLongOption(MINUTE, "Minute", true, 0, 59));
        this.shell = shell;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        try {
            long day  = interaction.getOptionByName(DAY).get().getLongValue().get();
            long hour  = interaction.getOptionByName(HOUR).get().getLongValue().get();
            long minute  = interaction.getOptionByName(MINUTE).get().getLongValue().get();
            runAndParse_ST_Command(shell, day, hour, minute)
                    .thenRun(() -> runAndParse_GT_Command(shell).thenAccept(consumer));

        } catch (NoSuchElementException ok) {
            consumer.accept("Not enough arguments. Format: `/gt 7 21 59`");
        }
    }

    public static CompletableFuture<String> runAndParse_ST_Command(ServerGameShell shell, long day, long hour, long minute) {
        return shell.executeCommand("st %d %d %d".formatted(day, hour, minute)).thenApply(commandResult -> {
            String stResult = commandResult.lastLine().split("\n")[0].trim();
            log.info("st command result: " + stResult);
            return stResult;
        }).exceptionally(throwable -> {
            log.error("Error on GT command: " + throwable.getMessage(), throwable);
            return null;
        });
    }
}
