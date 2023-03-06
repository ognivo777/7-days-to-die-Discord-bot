package org.obiz.sdtdbot.bus;

import org.obiz.sdtdbot.bus.Events;

public interface ServerStoppedListener {
    public void onServerStopped(Events.ServerStopped event);
}
