package org.obiz.sdtdbot.commands;

import com.google.common.eventbus.AsyncEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.Config;
import org.obiz.sdtdbot.Events;
import org.obiz.sdtdbot.ServerHostShell;

import java.util.function.Consumer;

public class RunGameServerCommand extends Command {
    private static final Logger log = LogManager.getLogger(RunGameServerCommand.class);
    private ServerHostShell shell;
    private Config config;
    private AsyncEventBus eventBus;

    public RunGameServerCommand(ServerHostShell shell, Config config, AsyncEventBus eventBus) {
        super("run", "Start game server", config.getOpDiscordRole());
        this.shell = shell;
        this.config = config;
        this.eventBus = eventBus;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        //todo проверить что сервер лежит
        //todo отдавать сразу ответ "Выполняем", а потом обновлять его на актуальный результат.
        //todo добавить шину, кидать туда евент "restart telnet" по окончании старта сервера (отловлена строчка "Done!" не ранее чем ерез N секнуд - замерить). Подписать на него ServerGameShell
        //todo подписаться на логи и отслеживать определённые строки для фиксации успешного запуска.
        consumer.accept("Try to do that..");
        shell.executeCommand(config.getRunServerCmd(), true).thenAccept(s -> {
            log.info("Start server logs:\n" + s);
            consumer.accept(s.isSuccess()?"Done":s.toString());
            eventBus.post(new Events.ServerStarted()); //todo отправлять по событию появления нужной строки в логах старта сервера
        });
    }

}
