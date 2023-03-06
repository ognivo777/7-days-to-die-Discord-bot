package org.obiz.sdtdbot.bus;

import org.obiz.sdtdbot.bus.Events;

public interface DiscordMessageListener {
    public void onDiscordMessage(Events.DiscordMessage event);
}
