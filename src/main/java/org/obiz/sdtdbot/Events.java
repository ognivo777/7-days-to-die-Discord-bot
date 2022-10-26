package org.obiz.sdtdbot;

public class Events {

    public static class ServerStarted{}

    public static class ServerStopped{}
    public static class DiscordMessage{
        String message;

        public DiscordMessage(String message) {
            this.message = message;
        }
    }

}
