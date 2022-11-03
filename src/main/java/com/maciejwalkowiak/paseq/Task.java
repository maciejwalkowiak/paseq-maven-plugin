package com.maciejwalkowiak.paseq;

import java.util.Arrays;

public class Task {
    enum Execution {
        SYNC,
        ASYNC
    }
    private boolean async;
    private boolean wait;
    private String[] goals;
    private Exec exec;

    public boolean isWait() {
        return wait;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }

    public Exec getExec() {
        return exec;
    }

    public void setExec(Exec exec) {
        this.exec = exec;
    }

    public String[] getGoals() {
        return goals;
    }

    public void setGoals(String[] goals) {
        this.goals = goals;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    Execution getExecution() {
        return async ? Execution.ASYNC : Execution.SYNC;
    }

    @Override public String toString() {
        return "Task{" +
                "async=" + async +
                ", wait=" + wait +
                ", goals=" + Arrays.toString(goals) +
                ", exec=" + exec +
                '}';
    }
}

