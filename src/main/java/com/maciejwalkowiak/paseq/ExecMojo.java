package com.maciejwalkowiak.paseq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Executes series of tasks.
 */
@Mojo(name = "exec")
public class ExecMojo extends AbstractMojo {

    @Parameter(property = "tasks")
    private Task[] tasks;

    @Component
    protected Invoker invoker;

    // public no-arg constructor is required by maven
    public ExecMojo() {
    }

    ExecMojo(List<Task> tasks, Invoker invoker) {
        this.tasks = tasks.toArray(new Task[]{});
        this.invoker = invoker;
    }

    public void execute() {
        // validate
        try {
            Arrays.stream(tasks).forEach(Task::validate);
        } catch (InvalidTaskDefinitionException e) {
            getLog().error(e.getMessage());
            throw e;
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Task task : tasks) {
            if (task.isAsync()) {
                futures.add(CompletableFuture.runAsync(() -> {
                    runAndLog(task);
                }));
            } else {
                if (task.isWait()) {
                    waitAndClear(futures);
                }
                runAndLog(task);
            }
        }
        waitAndClear(futures);
    }

    private void runAndLog(Task task) {
        getLog().info("Started invocation of " + task.toLoggableString());
        run(task);
        getLog().info("Finished invocation of " + task.toLoggableString());
    }

    private void run(Task task) {
        if (task.hasGoals()) {
            invoke(toInvocationRequest(task));
        } else if (task.getExec() != null && task.hasCommand()) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                Exec exec = task.getExec();
                if (exec.getDirectory() != null) {
                    processBuilder.directory(new File(exec.getDirectory()));
                }
                Process process = processBuilder.command(exec.getCommand().split(" "))
                        .inheritIO()
                        .start();
                process.waitFor();
                process.destroy();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("either goals or command has to be set");
        }
    }

    private void waitAndClear(List<CompletableFuture<?>> futures) {
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).join();
            futures.clear();
        }
    }

    private void invoke(InvocationRequest invocationRequest) {
        try {
            invoker.execute(invocationRequest);
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }

    private static InvocationRequest toInvocationRequest(Task task) {
        InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setGoals(Arrays.asList(task.getGoals()));
        return invocationRequest;
    }
}
