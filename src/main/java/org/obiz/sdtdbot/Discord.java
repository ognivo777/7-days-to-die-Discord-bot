package org.obiz.sdtdbot;

import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Permissions;
import org.obiz.sdtdbot.commands.Command;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Discord implements ServerStartedListener, ServerStoppedListener, DiscordMessageListener{
    private static final Logger log = LogManager.getLogger(Discord.class);
    private static final long RIGTHS = 2147601472l;
    private Config config;
    private DiscordApi api;
    private TextChannel textChannel;

    public Discord(Config config) {
        this.config = config;
    }

    public Discord init() throws Exception {

        DiscordApiBuilder discordApiBuilder = new DiscordApiBuilder();
        api = discordApiBuilder.setToken(config.getToken()).login().get(5, TimeUnit.SECONDS);
        log.info("Discord connected.");
        log.info("Invite url: " + api.createBotInvite(Permissions.fromBitmask(RIGTHS)));

        AtomicBoolean hasServer = new AtomicBoolean(false); //просто что бы в лямбде засетить
        api.getServers().forEach(server -> {
            log.info("I'm on server: " + server.getName() + " (" + server.getDescription().orElse("-") + "). id: " + server.getIdAsString());
            hasServer.set(true);
        });

        if(hasServer.get()) {
            Optional<TextChannel> channelById = api.getTextChannelById(config.getBotChannelId());
            if (!channelById.isPresent()) {
                log.error("Can't open discord channel!");
                shutdown();
                throw new Exception("Channel not found!");
            } else {
                //todo
                textChannel = channelById.get();
                textChannel.sendMessage("Hi! I'm here!");
            }

            return this;
        } else {
            log.error("No one server i know.. Use invite link for invite me.");
            shutdown();
            throw new Exception("Servers not found!");
        }
    }

    public void shutdown() {
        try {
            log.info("Try to disconnect from discord..");
            api.disconnect().thenAccept(unused -> {
                log.info("Discord disconnected.");
            }).get();
        }
        catch (InterruptedException | ExecutionException e) {
            log.error("Can't stop discord: " + e.getMessage(), e);
        }
        finally {}
    }

    public void addCommand(Command command) {
        command.start(api);
    }

    @Override
    @Subscribe
    public void onServerStart(Events.ServerStarted event) {
        log.info("Event: ServerStarted");
        textChannel.sendMessage("Server about to be started! :)");
    }

    @Override
    @Subscribe
    public void onServerStopped(Events.ServerStopped event) {
        log.info("Event: ServerStopped");
        textChannel.sendMessage("Server about to be stopped! :(");
    }

    @Override
    @Subscribe
    public void onDiscordMessage(Events.DiscordMessage event) {
        log.info("Event: DiscordMessage");
        textChannel.sendMessage(event.message);
    }
}
