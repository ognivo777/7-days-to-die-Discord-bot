package org.obiz.sdtdbot;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obiz.sdtdbot.bus.Events;
import org.obiz.sdtdbot.bus.ServerStartedListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerGameShell implements ServerStartedListener {
    private static final Logger log = LogManager.getLogger(ServerGameShell.class);
//    private final Config config;
    private final Timer timer;
    private final ServerHostShell shell;
    private final EventBus eventBus;
    private boolean isClosed = false;
    private final AtomicBoolean isAlive = new AtomicBoolean();
    private final AtomicInteger sameStateCounter = new AtomicInteger(3);

    public ServerGameShell(ServerHostShell shell) throws Exception {
        log.debug("Starting ServerGameShell!.....");
//        this.config = config;
        this.shell = shell;
        this.eventBus = Context.getContext().getEventBusInstance();
        //todo добавить в параметры Consumer<String> для отправки сообщений
        openTelnetWithPassword();

        timer = new Timer();
        startWatchDog(shell);
        //todo add handler for string "Connection closed by foreign host." - this means disconnects from telnet

    }

    private void startWatchDog(ServerHostShell shell) {
        //todo react to line "Connection closed by foreign host." in shell..
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                shell.executeCommand("gt", false).thenAccept(res -> {
                    if (res.isSuccess()) {
                        boolean startsWithDay = res.lastLine().startsWith("Day");
//                        isAlive.set(startsWithDay);
                        if (isAlive.compareAndSet(!startsWithDay, startsWithDay)) {
                            //isAlive takes new value, so state was changed
                            if (sameStateCounter.getAndSet(0) > 2) {
                                String message = "State changed to: " + (isAlive.get() ? "connected" : "connection lost");
                                log.info(message);
                                //eventBus.post(new Events.DiscordMessage(message));
                            }
                            //TODO после перехода в состояние отключённого не пытаться выполнять gt
                        } else {
                            //isAlive keep old value, so state was NOT changed
                            sameStateCounter.incrementAndGet();
                        }
                    } else {
                        isAlive.set(false);
                    }
                });
            }
        }, 5 * 1000, 30 * 1000);
    }

    private void openTelnetWithPassword() {
        try {
            Config config = Context.getContext().getConfigInstance();
            //todo проверить что мы в консоли сервера а не игры и статус элайв = фолс
            String s1 = shell.executeCommand("telnet 127.0.0.1 " + config.getTelnetPort(), false).get().lastLine();
            log.info("telnetWelcomeMessages: " + s1);
            String s2 = shell.executeCommand(config.getTelnetPasswd(), false).get().lastLine();
            log.info("telnetWelcomeMessages: " + s2);
            String s3 = shell.executeCommand("loglevel ALL false", false).get().lastLine();
            isAlive.set(true);
        } catch (InterruptedException | ExecutionException e) {
            isAlive.set(false);
            log.error("Open telnet error:" + e.getMessage(), e);
        }
    }

    @Override
    @Subscribe
    public void onServerStarted(Events.ServerStarted event) {
        log.info("Message <ServerStarted> received!");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                openTelnetWithPassword();
            }
        }, 3 * 1000);

    }

    public CompletableFuture<ShellCommandResult> executeCommand(String cmd) {
        if (isAlive.get()) {
            return shell.executeCommand(cmd, false).thenApply(shellCommandResult -> {
                if(shellCommandResult.isSuccess()
                         //&& shellCommandResult.toString().contains("Executing command") //not working with 'LOGLEVEL ALL FALSE'
                ) {
                    return shellCommandResult;
                }
                return ShellCommandResult.error("Wrong response: " + shellCommandResult);
            });
        } else {
            return CompletableFuture.completedFuture(ShellCommandResult.error("Telnet connection lost"));
        }
    }

    public void close() {
        if (!isClosed) {
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

    public boolean getIsAlive() {
        return isAlive.get();
    }
}
