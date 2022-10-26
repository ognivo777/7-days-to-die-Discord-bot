package org.obiz.sdtdbot;

public class ShellCommandResult {
    private boolean isSuccess;
    private String result;

    public ShellCommandResult(boolean isSuccess, String result) {
        this.isSuccess = isSuccess;
        this.result = result;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getResult() {
        return result;
    }
}
