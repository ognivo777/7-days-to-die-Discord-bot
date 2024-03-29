package org.obiz.sdtdbot.bus;

public class Events {

    public static class ServerStarted {}

    public static class ServerStopped {}

    public static class StopBot {
        private final String reason;

        public StopBot(String reason) {

            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

    public static class DiscordMessage {
        private String message;

        public DiscordMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ServerNotStarted {
    }
}
