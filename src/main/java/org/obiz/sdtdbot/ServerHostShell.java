package org.obiz.sdtdbot;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * core shh server shell
 * */
public class ServerHostShell {
    private static final Logger log = LogManager.getLogger(ServerHostShell.class);
    private final PrintStream commander;
    private final Thread readShellOutThread;
    private final int FIRST_BYTE_WAIT_PERIOD = 100;
    private final int WAIT_PERIOD = 300;
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
            prompt = executeCommand("", false).get(WAIT_PERIOD*10, TimeUnit.MILLISECONDS).lastLine();
            log.info("welcome string = <" + prompt + ">");

        } catch (JSchException | IOException | InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<ShellCommandResult> executeCommand(String cmd, boolean sudo) {
        log.info("Command: " + cmd);

        return CompletableFuture.supplyAsync(() -> {
            shellResponse.clear();
            commander.println(cmd);
            commander.flush();
            if (commander.checkError()) {
                log.error("Error on send ssh command.");
                return ShellCommandResult.error("Error on send ssh command.");
            }
            try {
                shellResponse.take(); //eat input reply
                //wait for first data
                String nextLine = null;
                Thread.sleep(FIRST_BYTE_WAIT_PERIOD); //small-time period
                for (int i = 0; i < 5; i++) {
                    nextLine = shellResponse.peek();
                    if(nextLine!=null) {
                        break;
                    }
                    Thread.sleep(WAIT_PERIOD); //a little greater time period
                }

                if(sudo && nextLine!=null && nextLine.startsWith("[sudo]")) {
                    commander.println(config.getSshPasswd());
                }

                //TODO ???????????? ???????????????? ???????????????????????? ?????????????????? prompt ?????????????? ?????? ?????????????? ???????? ?????? ???????????????????? ??????????????????????
                // - ???? ?????? ???? ???????????????? ?????? ???????????????? telnet ?? ???????????? ??????????????????????????. ???????????????? ?????????? ??????????????????????????????.
            } catch (InterruptedException e) {
                log.error(e);
                throw new CompletionException(e);
            }
            return ShellCommandResult.success(shellResponse);
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
            }).get(3, TimeUnit.SECONDS); //todo ?????????????????? ????????????????! ?????????? 3 ?????? ???????????? ????????????, ?????????????????????? ?????????? ???????? ????????????/, ?? ???????????? ?????????? ???? ?????? ?????????????? ????????????????
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

