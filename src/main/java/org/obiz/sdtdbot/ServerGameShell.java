package org.obiz.sdtdbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ServerGameShell {
    private static final Logger log = LogManager.getLogger(ServerGameShell.class);
    private ServerHostShell shell;
    private boolean isClosed = false;

    public ServerGameShell(Config config, ServerHostShell shell) {
        try {
            this.shell = shell;
            String s1 = shell.executeCommandWithSimpleResults("telnet 127.0.0.1 " + config.getTelnetPort(), false).get();
            log.info("telnetWelcomeMessages: " + s1);
            String s2 =  shell.executeCommandWithSimpleResults(config.getTelnetPasswd(), false).get();
            log.info("telnetWelcomeMessages: " + s2);

            //todo add handler for string "Connection closed by foreign host." - this means disconnects from telnet
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<String> executeCommandWithSimpleResults(String cmd) {
        return shell.executeCommandWithSimpleResults(cmd, false);
    }

    public void close() {
        if(!isClosed) {
            isClosed = true;
            try {
                shell.executeCommandWithSimpleResults("exit", false).thenAccept(s -> {
                    log.info("Game shell exit message: " + s);
                    shell.signal("2");
                }).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Can't close telnet: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

    }
}
