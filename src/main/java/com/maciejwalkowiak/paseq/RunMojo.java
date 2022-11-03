package com.maciejwalkowiak.paseq;

import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class RunMojo extends AbstractMojo {

    @Parameter(property = "tasks")
    private Task[] tasks;

    @Component
    protected Invoker invoker;

    public void execute() {
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

    void runAndLog(Task task) {
        getLog().info("Started " + task.getExecution() + " invocation of " + task);
        run(task);
        getLog().info("Finished " + task.getExecution() + " invocation of " + task);
    }

    void run(Task task) {
        if (task.getGoals() != null && task.getGoals().length > 0) {
            invoke(toInvocationRequest(task));
        } else if (task.getExec() != null && task.getExec().getCommand() != null) {
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
