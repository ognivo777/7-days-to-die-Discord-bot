package org.obiz.sdtdbot.bus;

import org.obiz.sdtdbot.bus.Events;

public interface ServerStartedListener {
    public void onServerStart(Events.ServerStarted event);
}
