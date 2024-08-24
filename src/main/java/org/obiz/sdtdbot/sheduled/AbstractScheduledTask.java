package org.obiz.sdtdbot.sheduled;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class AbstractScheduledTask {
    private static final Logger log = LogManager.getLogger(AbstractScheduledTask.class);
    protected Predicate<Integer> condition;
    private final AtomicInteger skippedTicksCounter = new AtomicInteger(0);
    public AbstractScheduledTask(Predicate<Integer> condition) {
        this.condition = condition;
    }

    protected void init(){}

    public void start(int periodInSeconds, int delayInSeconds) {
        init();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (condition.test(skippedTicksCounter.get())) {
                        skippedTicksCounter.set(0);
                        tick();
                    } else {
                        log.debug("Skipped ticks:{}", skippedTicksCounter.incrementAndGet());

                    }
                }catch (Exception e) {
                    log.error("Sheduler err!", e );
                }
            }
        }, 1000L * delayInSeconds, 1000L * periodInSeconds);

    }

    protected abstract void tick();
}
