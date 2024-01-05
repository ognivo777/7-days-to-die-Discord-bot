package org.obiz.sdtdbot.loghandlers;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class LogHandler {
    private Predicate<String> condition;
    private Consumer<String> consumer;

    protected LogHandler(Predicate<String> condition, Consumer<String> consumer) {
        this.condition = condition;
        this.consumer = consumer;
    }

    public boolean applies(String line) {
        boolean testResult = condition.test(line);
        if(testResult) {
            consumer.accept(line);
        }
        return testResult;
    }
}
