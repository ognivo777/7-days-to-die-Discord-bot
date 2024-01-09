package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.Bot;
import org.obiz.sdtdbot.bus.Events;
import org.obiz.sdtdbot.ServerHostShell;

import java.util.function.Consumer;

public class RunGameServerCommand extends Command {
    private static final Logger log = LogManager.getLogger(RunGameServerCommand.class);
    private ServerHostShell shell;

    public RunGameServerCommand(ServerHostShell shell) {
        super("run", "Start game server", Bot.getConfigInstance().getOpDiscordRole());
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        //todo проверить что сервер лежит
        //todo отдавать сразу ответ "Выполняем", а потом обновлять его на актуальный результат.
        //todo добавить шину, кидать туда евент "restart telnet" по окончании старта сервера (отловлена строчка "Done!" не ранее чем ерез N секнуд - замерить). Подписать на него ServerGameShell
        //todo подписаться на логи и отслеживать определённые строки для фиксации успешного запуска.
        consumer.accept("Try to do that..");
        shell.executeCommand(Bot.getConfigInstance().getRunServerCmd(), false).thenAccept(s -> {
            log.info("Start server logs:\n" + s);
            boolean isDone = s.isSuccess() && s.toString().contains("Done");
            consumer.accept(isDone ?"Done":s.toString());
            if(isDone)
                Bot.getEventBusInstance().post(new Events.ServerStarted()); //todo отправлять по событию появления нужной строки в логах старта сервера
            else
                Bot.getEventBusInstance().post(new Events.ServerNotStarted()); //todo отправлять по событию появления нужной строки в логах старта сервера

        });
    }

}
