package com.maciejwalkowiak.paseq;

public class Exec {
    private String command;
    private String directory;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Override public String toString() {
        return "Exec{" +
                "command='" + command + '\'' +
                ", directory='" + directory + '\'' +
                '}';
    }
}
