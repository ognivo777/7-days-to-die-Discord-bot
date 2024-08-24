package org.obiz.sdtdbot.bus;

import org.obiz.sdtdbot.ServerGameShell;
import org.obiz.sdtdbot.ServerHostShell;

public class Events {

    public static class ServerStarted {}

    public record BotStartPhaseOne(
            ServerHostShell hostShell,
            ServerGameShell gameShell
    ) {}

    public static class ServerStopped {}

    public record PlayerJoined(
            String playerName
    ) {}

    public record PlayerLeft(
            String playerName
    ) {}

    public record StopBot (
            String reason
    ) {}

    public record DiscordMessage(
            String message
    ) {}

    public static class ServerNotStarted {}
}
