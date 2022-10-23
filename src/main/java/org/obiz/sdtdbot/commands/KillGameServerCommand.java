package org.obiz.sdtdbot.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.obiz.sdtdbot.Config;
import org.obiz.sdtdbot.ServerHostShell;

import java.util.function.Consumer;

public class KillGameServerCommand extends Command {
    private static final Logger log = LogManager.getLogger(KillGameServerCommand.class);
    private ServerHostShell shell;
    private Config config;

    public KillGameServerCommand(ServerHostShell shell, Config config) {
        super("kill", "Kill game server", config.getOpDiscordRole());
        this.shell = shell;
        this.config = config;
    }

    @Override
    void createResponseContent(SlashCommandInteraction interaction, Consumer<String> consumer) {
        //todo проверить что сервер лежит
        shell.executeCommandWithSimpleResults(config.getKillServerCmd(), true).thenAccept(s -> {
            log.info("Kill server logs:\n" + s);
        });
    }
}
