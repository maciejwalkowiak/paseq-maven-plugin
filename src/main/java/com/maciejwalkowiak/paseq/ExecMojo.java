package com.maciejwalkowiak.paseq;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
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

    public void execute() throws MojoExecutionException {
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
                    try {
                        runAndLog(task);
                    } catch (MojoExecutionException e) {
                        throw new CompletionException(e);
                    }
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

    private void runAndLog(Task task) throws MojoExecutionException {
        getLog().info("Started invocation of " + task.toLoggableString());
        run(task);
        getLog().info("Finished invocation of " + task.toLoggableString());
    }

    private void run(Task task) throws MojoExecutionException {
        if (task.hasGoals()) {
            invoke(toInvocationRequest(task));
        } else if (task.getExec() != null && task.hasCommand()) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                Exec exec = task.getExec();
                if (exec.getDirectory() != null) {
                    processBuilder.directory(new File(exec.getDirectory()));
                }
                Process process = processBuilder.command(exec.getCommand().split(" ")).inheritIO().start();
                int result = process.waitFor();
                process.destroy();

                if (result != 0) {
                    throw new MojoExecutionException("Running task " + task + " finished with a exit code " + result);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("either goals or command has to be set");
        }
    }

    private void waitAndClear(List<CompletableFuture<?>> futures) throws MojoExecutionException {
        if (!futures.isEmpty()) {
            for (CompletableFuture<?> future : futures) {
                try {
                    future.join();
                } catch (CompletionException e) {
                    if (e.getCause() instanceof MojoExecutionException) {
                        throw (MojoExecutionException) e.getCause();
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
            futures.clear();
        }
    }

    private void invoke(InvocationRequest invocationRequest) throws MojoExecutionException {
        try {
            InvocationResult result = invoker.execute(invocationRequest);
            if (result.getExitCode() != 0 || result.getExecutionException() != null) {
                throw new MojoExecutionException(result.getExecutionException());
            }
        } catch (MavenInvocationException e) {
            throw new MojoExecutionException(e);
        }
    }

    private static InvocationRequest toInvocationRequest(Task task) {
        InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setGoals(Arrays.asList(task.getGoals()));
        return invocationRequest;
    }
}
