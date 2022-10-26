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
import java.util.concurrent.atomic.AtomicInteger;

public class ServerGameShell {
    private static final Logger log = LogManager.getLogger(ServerGameShell.class);
    private ServerHostShell shell;
    private boolean isClosed = false;
    private AtomicBoolean isAlive = new AtomicBoolean();
    private AtomicInteger stateCounter = new AtomicInteger(0);

    public ServerGameShell(Config config, ServerHostShell shell) {
        //todo добавить в параметры Consumer<String> для отправки сообщений
        try {
            this.shell = shell;
            String s1 = shell.executeCommand("telnet 127.0.0.1 " + config.getTelnetPort(), false).get().lastLine();
            log.info("telnetWelcomeMessages: " + s1);
            String s2 =  shell.executeCommand(config.getTelnetPasswd(), false).get().lastLine();
            log.info("telnetWelcomeMessages: " + s2);

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    shell.executeCommand("gt", false).thenAccept(res -> {
                        boolean startsWithDay = res.lastLine().startsWith("Day");
//                        isAlive.set(startsWithDay);
                        if(isAlive.compareAndSet(!startsWithDay, startsWithDay)) {
                            //isAlive takes new value, so state was changed
                            if(stateCounter.getAndSet(0) > 2) {
                                log.info("State changed to: " + (isAlive.get()?"connected":"connection lost"));
                            }

                        } else {
                            //isAlive keep old value, so state was NOT changed
                            stateCounter.incrementAndGet();
                        }
                    });
                }
            }, 5 *1000, 10 *1000);

            //todo add handler for string "Connection closed by foreign host." - this means disconnects from telnet
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<ShellCommandResult> executeCommand(String cmd) {
        if(isAlive.get()) {
            return shell.executeCommand(cmd, false);
        } else {
            return CompletableFuture.completedFuture(ShellCommandResult.error("Telnet connection lost"));
        }
    }

    public void close() {
        if(!isClosed) {
            isClosed = true;
            try {
                shell.executeCommand("exit", false).thenAccept(commandResult -> {
                    log.info("Game shell exit message: " + commandResult);
                    shell.signal("2");
                }).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Can't close telnet: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

    }
}
