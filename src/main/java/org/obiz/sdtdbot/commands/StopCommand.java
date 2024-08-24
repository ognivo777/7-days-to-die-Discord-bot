package org.obiz.sdtdbot.commands;

import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.Bot;
import org.obiz.sdtdbot.Context;
import org.obiz.sdtdbot.bus.Events;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class StopCommand extends Command {
    private Bot bot;

    public StopCommand(Bot bot, String userId) {
        super("stop", "stop the bot", interaction -> interaction.getUser().getIdAsString().equals(userId));
        this.bot = bot;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        consumer.accept("Ok. :,(");
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(3000);
                Context.getContext().getEventBusInstance().post(new Events.StopBot("By user command"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
