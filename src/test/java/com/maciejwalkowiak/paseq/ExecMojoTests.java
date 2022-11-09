package com.maciejwalkowiak.paseq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ExecMojo}.
 *
 * @author Maciej Walkowiak
 */
class ExecMojoTests {

    private final Invoker invoker = mock(Invoker.class);

    // configured in maven-surefire-plugin
    private final String directory = System.getProperty("buildDirectory");

    @BeforeEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Path.of(directory).resolve("test-file"));
    }

    @Test
    void failsWhenTasksAreInvalid() {
        var mojo = new ExecMojo(Collections.singletonList(new Task()), invoker);
        assertThatThrownBy(mojo::execute).isInstanceOf(InvalidTaskDefinitionException.class);
    }

    @Test
    void executesMavenGoals() throws MavenInvocationException, MojoExecutionException {
        when(invoker.execute(any())).thenReturn(new DummyInvocationResult(0));
        var mojo = new ExecMojo(Collections.singletonList(Task.withGoals("clean", "install").build()), invoker);
        mojo.execute();
        verify(invoker).execute(argThat(invocationRequest -> {
            assertThat(invocationRequest.getGoals()).containsExactly("clean", "install");
            return true;
        }));
    }

    @Test
    void executesCommand() throws MojoExecutionException {
        var command = "touch test-file";
        var mojo = new ExecMojo(Collections.singletonList(Task.withCommand(command, directory).build()), invoker);
        mojo.execute();
        assertThat(Path.of(directory).resolve("test-file")).exists();
    }

    @Test
    void failsWhenCommandExecutionFails() {
        var mojo = new ExecMojo(
                Arrays.asList(Task.withCommand("touch non-existing-directory/bar.txt", directory).build(),
                        Task.withCommand("touch test-file", directory).build()),
                invoker);
        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
        assertThat(Path.of(directory).resolve("test-file")).doesNotExist();
    }

    @Test
    void failsWhenGoalExecutionFails() throws MavenInvocationException {
        when(invoker.execute(any())).thenReturn(new DummyInvocationResult(1));
        var mojo = new ExecMojo(Arrays.asList(Task.withGoals("compile", directory).build(),
                Task.withCommand("touch test-file", directory).build()), invoker);
        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class);
        assertThat(Path.of(directory).resolve("test-file")).doesNotExist();
    }

    @Test
    void executesGoalAndCommandSequentially() throws MavenInvocationException, MojoExecutionException {
        var commandTask = Task.withCommand("touch test-file", directory).build();
        var goalTask = Task.withGoals("package").build();
        var mojo = new ExecMojo(Arrays.asList(goalTask, commandTask), invoker);
        when(invoker.execute(any())).thenReturn(new DummyInvocationResult(0));

        mojo.execute();

        verify(invoker).execute(argThat(invocationRequest -> {
            assertThat(invocationRequest.getGoals()).containsExactly("package");
            return true;
        }));
        assertThat(Path.of(directory).resolve("test-file")).exists();
    }

    @Test
    void waitsForAllAsyncTasksToFinish() throws MavenInvocationException, MojoExecutionException {
        var commandTask = Task.withCommand("touch test-file", directory).async().build();
        var goalTask = Task.withGoals("package").async().build();
        var mojo = new ExecMojo(Arrays.asList(goalTask, commandTask), invoker);
        when(invoker.execute(any())).thenReturn(new DummyInvocationResult(0));

        mojo.execute();

        verify(invoker).execute(argThat(invocationRequest -> {
            assertThat(invocationRequest.getGoals()).containsExactly("package");
            return true;
        }));
        assertThat(Path.of(directory).resolve("test-file")).exists();
    }

    static class DummyInvocationResult implements InvocationResult {
        private final int exitCode;
        private final CommandLineException commandLineException;

        DummyInvocationResult(int exitCode, CommandLineException commandLineException) {
            this.exitCode = exitCode;
            this.commandLineException = commandLineException;
        }

        public DummyInvocationResult(int exitCode) {
            this(exitCode, null);
        }

        @Override
        public CommandLineException getExecutionException() {
            return commandLineException;
        }

        @Override
        public int getExitCode() {
            return exitCode;
        }
    }
}
