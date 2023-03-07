package org.obiz.sdtdbot;

import com.google.common.eventbus.AsyncEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerFileTailer {
    private static final Logger log = LogManager.getLogger(ServerFileTailer.class);
    private final Config config;
    private ServerHostShell shell;
    private AsyncEventBus eventBus;

    /** какие задачи выполняет? Мониторинг лога на предмет поиска евентов и отправки их в чат?
     */

    public ServerFileTailer(Config config, ServerHostShell shell, AsyncEventBus eventBus) {
        this.config = config;
        this.shell = shell;
        this.eventBus = eventBus;

        shell.executeCommand("tail -F -n 0 " + config.getLogFileName(), false);

    }
}
