package org.obiz.sdtdbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;

public class ShellCommandResult {
    private boolean isSuccess;
    private List<String> result;

    private ShellCommandResult(boolean isSuccess, List<String> result) {
        this.isSuccess = isSuccess;
        this.result = result;
    }

    private ShellCommandResult(boolean isSuccess, String error) {
        this(isSuccess, Collections.singletonList(error));
    }

    public static ShellCommandResult success(List<String> result) {
        return new ShellCommandResult(true, result);
    }

    public static ShellCommandResult error(List<String> result) {
        return new ShellCommandResult(false, result);
    }

    public static ShellCommandResult error(String result) {
        return new ShellCommandResult(false, result);
    }

    public static ShellCommandResult success(BlockingDeque<String> shellResponse) {
        return success(new ArrayList<>(shellResponse));
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public List<String> getResult() {
        return result;
    }

    public String lastLine() {
        if(result!=null && !result.isEmpty()) {
            return result.get(result.size()-1);
        }
        return "";
    }

    @Override
    public String toString() {
        if(result!=null)
            return (isSuccess?"":"<Error>:") + String.join("\n", result);
        else
            return "<NULL>";
    }
}
