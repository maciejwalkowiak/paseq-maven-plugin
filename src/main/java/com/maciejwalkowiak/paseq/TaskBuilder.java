package com.maciejwalkowiak.paseq;

import org.jetbrains.annotations.Nullable;

public class TaskBuilder {
    private boolean async;
    private boolean wait;
    private @Nullable String[] goals;
    private @Nullable Exec exec;

    TaskBuilder setGoals(String[] goals) {
        this.goals = goals;
        return this;
    }

    TaskBuilder setExec(Exec exec) {
        this.exec = exec;
        return this;
    }

    TaskBuilder async() {
        this.async = true;
        return this;
    }

    TaskBuilder waits() {
        this.wait = true;
        return this;
    }

    Task build() {
        return new Task(this.async, this.wait, this.goals, this.exec);
    }
}
