package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.Config;
import org.obiz.sdtdbot.ServerGameShell;
import org.obiz.sdtdbot.ServerHostShell;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class KillGameServerCommand extends AbstractGameControlCommand {
    private static final Logger log = LogManager.getLogger(KillGameServerCommand.class);
    private Config config;
    private AtomicBoolean serverIsEmpty = new AtomicBoolean(false);

    public KillGameServerCommand(ServerGameShell gameShell, ServerHostShell shell, Config config) {
        super("kill", "Kill game server", config.getOpDiscordRole());
        this.gameShell = gameShell;
        this.shell = shell;
        this.config = config;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        //todo проверить что сервер НЕ лежит
        //todo отдавать сразу ответ "Выполняем", а потом обновлять его на актуальный результат,
        // для этого вернуть отсюда какой-то объект, через который можно выполнить обновление.
        consumer.accept("Try to do that..");

        gameShell.executeCommand("gt").thenAccept(gtResult -> {
            log.info("KILL: server seems alive by server console.");
            gameShell.executeCommand("lpi").thenAccept(lpiResult -> {
                if(lpiResult.mergedLines().contains("Total of 0 in the game")) {
                    serverIsEmpty.set(true);
                    interaction.createFollowupMessageBuilder().setContent("Server is empty. Shutdown sequence will initiated by game server terminal.")
                            .setFlags(MessageFlag.EPHEMERAL).send();
                    gameShell.executeCommand("shutdown").thenAccept(sdCommandResult -> {
                    });
                } else {
                    serverIsEmpty.set(false);
                    interaction.createFollowupMessageBuilder().setContent("Server not empty. Try use 'kickall' before.").setFlags(MessageFlag.EPHEMERAL).send();
                }
            });
        }).thenRun(() -> {
            checkServerProcessAlive().thenAccept(isAlive -> {
                if(isAlive) {
                    log.info("KILL: server process found. Try to kill using kill script.");
                    if(serverIsEmpty.get()) {
                        shell.executeCommand(config.getKillServerCmd(), true).thenAccept(s -> {
                            log.info("Kill server logs:\n" + s.mergedLines());
                            consumer.accept("Done.");
                        });
                    } else {
                        consumer.accept("Server alive an not empty");
                    }
                } else {
                    log.info("KILL: server process seems are NOT alive.");
                }
            });
        });

    }

}
