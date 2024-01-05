package org.obiz.sdtdbot;

import com.google.common.eventbus.AsyncEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obiz.sdtdbot.loghandlers.LogHandler;

import java.util.ArrayList;
import java.util.List;

public class ServerFileTailer {
    private static final Logger log = LogManager.getLogger(ServerFileTailer.class);
    private final Config config;
    private ServerHostShell shell;
    private AsyncEventBus eventBus;

    private List<LogHandler> handlers = new ArrayList<>();

    /** какие задачи выполняет? Мониторинг лога на предмет поиска евентов и отправки их в чат?
     */

    public ServerFileTailer(Config config, ServerHostShell shell, AsyncEventBus eventBus) {
        this.config = config;
        this.shell = shell;
        this.eventBus = eventBus;
    }

    public void start() {
        shell.executeEndlessCommand("tail -F -n 0 " + config.getLogFileName(), false, line -> {
            handlers.forEach(handler -> {
                /*
                TODO
                1. после успешного остальные не запускать
                2. запоминать предыдущий успешный и начинать с него (для обработки моногострочек)
                 */
                log.info(line);
                handler.applies(line);
            });
        });
    }
    public void addHandler(LogHandler handler) {
        handlers.add(handler);
    }
}
