package com.maciejwalkowiak.paseq;


import java.util.Arrays;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a task. Either {@link #goals} {@link #exec} can be defined on a
 * single task.
 *
 * @author Maciej Walkowiak
 */
public class Task {
    /**
     * If task is executed in a background thread.
     */
    private boolean async;

    /**
     * If task should wait for all previously started async tasks to finish.
     */
    private boolean wait;

    /**
     * Maven goals to run in this task.
     */
    private @Nullable String[] goals;

    /**
     * Configuration for the command to run in this task.
     */
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

    /**
     * Checks if task is in valid state.
     *
     * @throws InvalidTaskDefinitionException
     *             when task is not in valid state.
     */
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
