package org.obiz.sdtdbot;

import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Permissions;
import org.obiz.sdtdbot.bus.DiscordMessageListener;
import org.obiz.sdtdbot.bus.Events;
import org.obiz.sdtdbot.bus.ServerStartedListener;
import org.obiz.sdtdbot.bus.ServerStoppedListener;
import org.obiz.sdtdbot.commands.Command;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Discord implements ServerStartedListener, ServerStoppedListener, DiscordMessageListener {
    private static final Logger log = LogManager.getLogger(Discord.class);
    private static final long RIGTHS = 2147601472l;
    private Config config;
    private DiscordApi api;
    private TextChannel textChannel;
    private long serversCount;

    public Discord(Config config) {
        this.config = config;
    }

    public CompletableFuture<Discord> init() {
        CompletableFuture<Discord> futureResult = new CompletableFuture<>();
        new DiscordApiBuilder().setToken(config.getToken()).login().thenAccept(discordApi -> {
            api = discordApi;
            log.info("Discord connected.");
            log.info("Invite url: " + api.createBotInvite(Permissions.fromBitmask(RIGTHS)));

            api.getServers().stream().peek(server -> {
                log.info("I'm on server: " + server.getName() + " (" + server.getDescription().orElse("-") + "). id: " + server.getIdAsString());
            }).findFirst().ifPresentOrElse(server -> {
                //here if server found
                api.getTextChannelById(config.getBotChannelId()).ifPresentOrElse(tc -> {
                    //text chan found
                    //todo ??
                    textChannel = tc;
                    //textChannel.sendMessage("Hi! I'm here!");
                    futureResult.complete(Discord.this);
                }, () -> {
                    //text chan NOT found
                    log.error("Can't open discord channel!");
                    shutdown();
                    futureResult.completeExceptionally(new Exception("Can't open discord channel!"));
                });
            }, () -> {
                //here if no server found
                log.error("No one server i know.. Use invite link for invite me.");
                shutdown();
                futureResult.completeExceptionally(new Exception("No one server i know.. Use invite link for invite me."));
            });
        }).exceptionally(throwable -> {
            //can't login to discord
            log.error("Discord login error:" + throwable.getMessage(), throwable);
            futureResult.completeExceptionally(throwable);
            return null;
        });
        return futureResult;
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
    public void onServerStarted(Events.ServerStarted event) {
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
        textChannel.sendMessage(event.message());
    }

    public void sendMessageToChannel(String message) {
        textChannel.sendMessage(message);
    }

//    public DiscordApi getApi() {
//        return api;
//    }
}
