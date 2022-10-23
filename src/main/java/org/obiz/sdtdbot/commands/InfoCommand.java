package org.obiz.sdtdbot.commands;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.Bot;

import java.util.function.Consumer;

public class InfoCommand extends Command {
    private Bot bot;

    public InfoCommand(Bot bot) {
        super("info", "Shows bot info!");
        this.bot = bot;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        consumer.accept("Version:\t" + Bot.BOT_VERSION + "Started:\t" + bot.getStarted());
    }
}
