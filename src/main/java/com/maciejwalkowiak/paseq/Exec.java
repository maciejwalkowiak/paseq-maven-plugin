package com.maciejwalkowiak.paseq;

import org.jetbrains.annotations.Nullable;

public class Exec {
    private String command;
    private @Nullable String directory;

    // public no-arg constructor is required by maven
    public Exec() {
    }

    Exec(String command) {
        this(command, null);
    }

    public Exec(String command, @Nullable String directory) {
        this.command = command;
        this.directory = directory;
    }

    public String getCommand() {
        return command;
    }

    public @Nullable String getDirectory() {
        return directory;
    }

    @Override public String toString() {
        return "Exec{" +
                "command='" + command + '\'' +
                ", directory='" + directory + '\'' +
                '}';
    }

    String toLoggableString() {
        return command + (directory != null ? " in directory: " + directory : "");
    }
}
