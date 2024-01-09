package org.obiz.sdtdbot;

import com.google.common.base.Strings;
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
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * core shh server shell
 * */
public class ServerHostShell {
    private static final Logger log = LogManager.getLogger(ServerHostShell.class);
    private final PrintStream commander;
    private final Thread readShellOutThread;
    private final int FIRST_BYTE_WAIT_PERIOD = 100;
    private final int WAIT_PERIOD = 300;
    private final int LONG_COMMAND_RESULT_WAIT_PERIOD = 1500;
    private final String prompt;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BlockingDeque<String> shellResponse = new LinkedBlockingDeque<>();
    private final ReentrantLock commandExecutionLock = new ReentrantLock();
    private JSch jSch;
    private Session session;
    private ChannelShell shell;
    private Config config;
    private final String name;

    public ServerHostShell(String name) {
//        this.config = config;
        Config config = Bot.getConfigInstance();
        this.name = name;
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
                    log.info("Constantly read output thread started for '" + name + "'.");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(shell.getInputStream(), StandardCharsets.UTF_8));
                    while (true) {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            Thread.sleep(WAIT_PERIOD);
                        } else if (line.isEmpty()) {
                        } else {
                            log.info("<" + name + "> out: <" + line + ">");
                            shellResponse.put(line);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error read ssh output for '" + name + "': " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    log.info("Read output thread stopped for '" + name + "'.");
                }
            });
            readShellOutThread.start();

            shell.connect();

            String line;
            log.info("-------======= Ssh welcome messages =======-------  for '" + name + "'");
            while ((line = shellResponse.poll(WAIT_PERIOD, TimeUnit.MILLISECONDS))!=null) {
                log.info(line);
            }
//            welcome = executeCommand("").get(WAIT_PERIOD*10, TimeUnit.MILLISECONDS).get();
            prompt = executeCommand("", false).get(WAIT_PERIOD*10, TimeUnit.MILLISECONDS).lastLine();
            log.info("'" + name + "' welcome string = <" + prompt + ">");

        } catch (JSchException | IOException | InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<ShellCommandResult> executeCommand(String cmd, boolean sudo) {
        log.info("<" + name + "> Command to run on HOST Shell: " + cmd);

        return CompletableFuture.supplyAsync(() -> {
            try {
                commandExecutionLock.lock();
                shellResponse.clear();
                commander.println(cmd);
                commander.flush();
                if (commander.checkError()) {
                    log.error("<" + name + "> Error on send ssh command.");
                    return ShellCommandResult.error("Error on send ssh command.");
                } else {
                    try {
                        String nextLine = startReadCommandResult();

                        //if server asks for password - type them
                        if (sudo && nextLine != null && nextLine.startsWith("[sudo]")) {
                            commander.println(config.getSshPasswd());
                        }

                        //TODO вместо таймаута использовать появление prompt мессажэ как признак того как исполнение закончилось
                        // - но это не подходит при открытии telnet и прочих интерактивных. Возможно стоит параметризовать.
                        List<String> lines = new ArrayList<>();
                        while (true) {
                            String nextLineFromOutput = shellResponse.poll(LONG_COMMAND_RESULT_WAIT_PERIOD, TimeUnit.MICROSECONDS);
                            if (!Strings.isNullOrEmpty(nextLineFromOutput)) {
                                lines.add(nextLineFromOutput);
                            } else {
                                break;
                            }
                        }
                        return ShellCommandResult.success(lines);
                    } catch (InterruptedException e) {
                        log.error("<" + name + "> Error:", e);
                        throw new CompletionException(e);
//                    return ShellCommandResult.error("");
                    }
                    //return ShellCommandResult.success(shellResponse);
                }
            } finally {
                commandExecutionLock.unlock();
            }
        }, executor); //using local single thread executor guarantees one by one execution without collisions
    }

    private String startReadCommandResult() throws InterruptedException {
        shellResponse.take(); //eat input reply
        //wait for first data
        String nextLine = null;
        Thread.sleep(FIRST_BYTE_WAIT_PERIOD); //small-time period
        //wait for first line
        for (int i = 0; i < 5; i++) {
            nextLine = shellResponse.peek();
            if (nextLine != null) {
                break;
            }
            Thread.sleep(WAIT_PERIOD); //a little greater time period
        }
        return nextLine;
    }

    public void executeEndlessCommand(String cmd, boolean sudo, final Consumer<String> lineConsumer) {
        executor.execute(() -> {
            try {
                commandExecutionLock.lock();
                shellResponse.clear();
                commander.println(cmd);
                commander.flush();
                if (commander.checkError()) {
                    log.error("<" + name + "> Error on send ssh command.");
                } else {
                    try {
                        startReadCommandResult();
                        //here endless while
                        while (session.isConnected()) {
                            String line = shellResponse.poll(LONG_COMMAND_RESULT_WAIT_PERIOD, TimeUnit.MICROSECONDS);
                            if (!Strings.isNullOrEmpty(line)) {
                                lineConsumer.accept(line);
                            }
                        }
                    } catch (InterruptedException e) {
                        log.error(e);
                        throw new CompletionException(e);
                    }
                }
            } finally {
                commandExecutionLock.unlock();
            }
        });

    }

    public void close() {
        try {
            executor.submit(() -> {
                log.debug("<" + name + "> executor.shutdown();");
                executor.shutdown();
                log.debug("<" + name + "> shell.disconnect();");
                shell.disconnect();
                log.debug("<" + name + "> session.disconnect();");
                session.disconnect();
                log.debug("<" + name + "> readShellOutThread.interrupt();");
                readShellOutThread.interrupt();
            }).get(3, TimeUnit.SECONDS); //todo уменьшить задержку! ждать 3 сек первой строки, переставать ждать если пришла/, а дальше ждать по пол секунды максимум
            log.info("<" + name + "> ServerHostShell close done.");
        } catch (InterruptedException | ExecutionException e) {
            log.error("<" + name + "> Can't stop ssh connection: " + e.getMessage(), e);
        } catch (TimeoutException ignored) {
        }
    }

    public void signal(String signal) {
        try {
            shell.sendSignal(signal);
        } catch (Exception e) {
            log.error("<" + name + "> Error on signal :" + signal, e);
            throw new RuntimeException(e);
        }
    }
}

