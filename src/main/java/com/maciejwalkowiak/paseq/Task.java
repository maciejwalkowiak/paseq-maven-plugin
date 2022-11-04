package com.maciejwalkowiak.paseq;


import java.util.Arrays;
import org.jetbrains.annotations.Nullable;

public class Task {
    private boolean async;
    private boolean wait;
    private @Nullable String[] goals;
    private @Nullable Exec exec;

    // public no-arg constructor is required by maven
    public Task() {
    }

    static TaskBuilder withGoals(String... goals) {
        return new TaskBuilder().setGoals(goals);
    }

    static TaskBuilder withCommand(String command) {
        return withCommand(command, null);
    }

    static TaskBuilder withCommand(String command, @Nullable String directory) {
        return new TaskBuilder().setExec(new Exec(command, directory));
    }

    Task(boolean async, boolean wait, @Nullable String[] goals, @Nullable Exec exec) {
        this.async = async;
        this.wait = wait;
        this.goals = goals;
        this.exec = exec;
    }

    public void validate() {
        if (!hasGoals() && !hasCommand()) {
            throw new InvalidTaskDefinitionException("Either `goals` or `command` must be set on a task");
        }
        if (hasGoals() && hasCommand()) {
            throw new InvalidTaskDefinitionException("Task cannot have both `goals` or `command` set");
        }
    }

    boolean hasGoals() {
        return goals != null && goals.length > 0;
    }

    boolean hasCommand() {
        return exec != null && exec.getCommand() != null;
    }

    public boolean isWait() {
        return wait;
    }

    public @Nullable Exec getExec() {
        return exec;
    }

    public @Nullable String[] getGoals() {
        return goals;
    }

    public boolean isAsync() {
        return async;
    }

    String toLoggableString() {
        String loggableString;
        if (hasGoals()) {
            loggableString = Arrays.toString(goals);
        } else if (hasCommand()) {
            loggableString = exec.toLoggableString();
        } else {
            throw new RuntimeException(
                    "Task is invalid state. Looks like a bug in task validation. You must set either task goals or the command");
        }
        return loggableString + " (" + (async ? "async" : "sync") + ")";
    }

    @Override
    public String toString() {
        return "Task{" + "async=" + async + ", wait=" + wait + ", goals=" + Arrays.toString(goals) + ", exec=" + exec
                + '}';
    }
}
