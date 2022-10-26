package org.obiz.sdtdbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerGameShell {
    private static final Logger log = LogManager.getLogger(ServerGameShell.class);
    private ServerHostShell shell;
    private boolean isClosed = false;
    private AtomicBoolean isAlive = new AtomicBoolean();

    public ServerGameShell(Config config, ServerHostShell shell) {
        try {
            this.shell = shell;
            String s1 = shell.executeCommandWithSimpleResults("telnet 127.0.0.1 " + config.getTelnetPort(), false).get();
            log.info("telnetWelcomeMessages: " + s1);
            String s2 =  shell.executeCommandWithSimpleResults(config.getTelnetPasswd(), false).get();
            log.info("telnetWelcomeMessages: " + s2);

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    shell.executeCommandWithSimpleResults("gt", false).thenAccept(s -> {
                        isAlive.set(s.startsWith("Day"));
                    });
                    //todo логировать смену состояния, если предыдущее удержалось хотя бы 3 раза (cluth?)
                }
            }, 5, 10);

            //todo add handler for string "Connection closed by foreign host." - this means disconnects from telnet
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<String> executeCommandWithSimpleResults(String cmd) {
        if(isAlive.get()) {
            return shell.executeCommandWithSimpleResults(cmd, false);
        } else {
            return CompletableFuture.completedFuture("Connection lost");
        }
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
