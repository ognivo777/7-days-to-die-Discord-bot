package org.obiz.sdtdbot;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * core shh server shell
 * */
public class ServerHostShell {
    private static final Logger log = LogManager.getLogger(ServerHostShell.class);
    private final PrintStream commander;
    private final Thread readShellOutThread;
    private final int WAIT_PERIOD = 3000;
    private final String prompt;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BlockingDeque<String> shellResponse = new LinkedBlockingDeque<>();
    private JSch jSch;
    private Session session;
    private ChannelShell shell;
    private Config config;

    public ServerHostShell(Config config) {
        this.config = config;
        jSch = new JSch();
        try {
            session = jSch.getSession(config.getSshUser(), config.getHost(), config.getPort());
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.setPassword(config.getSshPasswd());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            shell = (ChannelShell) session.openChannel("shell");
            commander = new PrintStream(shell.getOutputStream(), true, StandardCharsets.UTF_8.name());

            readShellOutThread = new Thread(() -> {
                try {
                    log.info("Read output thread started.");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(shell.getInputStream(), "windows-1251"));
                    while (true) {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            Thread.sleep(WAIT_PERIOD);
                        } else if (line.isEmpty()) {
                        } else {
                            log.debug("s: <" + line + ">");
                            shellResponse.add(line);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error read ssh output: " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    log.info("Read output thread stopped.");
                }
            });
            readShellOutThread.start();

            shell.connect();

            String line;
            log.info("-------======= Ssh welcome messages =======-------");
            while ((line = shellResponse.poll(WAIT_PERIOD, TimeUnit.MILLISECONDS))!=null) {
                log.info(line);
            }
//            welcome = executeCommand("").get(WAIT_PERIOD*10, TimeUnit.MILLISECONDS).get();
            prompt = executeCommandWithSimpleResults("", false).get(WAIT_PERIOD*10, TimeUnit.MILLISECONDS);
            log.info("welcome string = <" + prompt + ">");

        } catch (JSchException | IOException | InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<String> executeCommandWithSimpleResults(String cmd, boolean sudo) {
        CompletableFuture<Optional<List<String>>> optionalCompletableFuture = executeCommand(cmd, sudo);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return String.join("\n", optionalCompletableFuture.get(WAIT_PERIOD, TimeUnit.MILLISECONDS).get());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    public CompletableFuture<Optional<List<String>>> executeCommand(String cmd, boolean sudo) {
        log.info("Command: " + cmd);
        return CompletableFuture.supplyAsync(() -> {
            shellResponse.clear();
            commander.println(cmd);
            commander.flush();
            if (commander.checkError()) {
                log.error("Error on send ssh command.");
                return Optional.empty();
            }
            try {
                String commandSelf = shellResponse.take(); //input reply
                Thread.sleep(WAIT_PERIOD);
                String nextLine = shellResponse.peek();
                if(sudo && nextLine!=null && nextLine.startsWith("[sudo]")) {
                    commander.println(config.getSshPasswd());
                }
                //TODO вместо таймаута использовать появление prompt мессажэ как признак того как исполнение закончилось
                // - но это не подходит при открытии telnet и прочих интерактивных. Возможно стоит параметризовать.
            } catch (InterruptedException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
//            return String.join("\n", shellResponse);
            return Optional.of(new ArrayList<>(shellResponse));
        }, executor);
    }

    public void close() {
        try {
            executor.submit(() -> {
                log.debug("executor.shutdown();");
                executor.shutdown();
                log.debug("shell.disconnect();");
                shell.disconnect();
                log.debug("session.disconnect();");
                session.disconnect();
                log.debug("readShellOutThread.interrupt();");
                readShellOutThread.interrupt();
            }).get(3, TimeUnit.SECONDS);
            log.info("ServerHostShell close done.");
        } catch (InterruptedException | ExecutionException e) {
            log.error("Can't stop ssh connection: " + e.getMessage(), e);
        } catch (TimeoutException e) {

        }
    }

    public void signal(String signal) {
        try {
            shell.sendSignal(signal);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
}

