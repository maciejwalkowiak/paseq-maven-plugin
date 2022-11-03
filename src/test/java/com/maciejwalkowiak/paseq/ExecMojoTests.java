package com.maciejwalkowiak.paseq;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExecMojoTests {

    private final Invoker invoker = mock(Invoker.class);

    // configured in maven-surefire-plugin
    private final String directory = System.getProperty("buildDirectory");

    @Test
    void failsWhenTasksAreInvalid() {
        var mojo = new ExecMojo(Collections.singletonList(new Task()), invoker);
        assertThatThrownBy(mojo::execute).isInstanceOf(InvalidTaskDefinitionException.class);
    }

    @Test
    void executesMavenGoals() throws MavenInvocationException {
        var mojo = new ExecMojo(Collections.singletonList(Task.withGoals("clean", "install").build()), invoker);
        mojo.execute();
        verify(invoker).execute(argThat(invocationRequest -> {
            assertThat(invocationRequest.getGoals()).containsExactly("clean", "install");
            return true;
        }));
    }

    @Test
    void executesCommand() {
        var command = "touch test-file";
        var mojo = new ExecMojo(Collections.singletonList(Task.withCommand(command, directory).build()), invoker);
        mojo.execute();
        assertThat(Path.of(directory).resolve("test-file")).exists();
    }

    @Test
    void executesGoalAndCommandSequentially() throws MavenInvocationException {
        var commandTask = Task.withCommand("touch test-file", directory).build();
        var goalTask = Task.withGoals("package").build();
        var mojo = new ExecMojo(Arrays.asList(goalTask, commandTask), invoker);

        mojo.execute();

        verify(invoker).execute(argThat(invocationRequest -> {
            assertThat(invocationRequest.getGoals()).containsExactly("package");
            return true;
        }));
        assertThat(Path.of(directory).resolve("test-file")).exists();
    }

    @Test
    void waitsForAllAsyncTasksToFinish() throws MavenInvocationException {
        var commandTask = Task.withCommand("touch test-file", directory).async().build();
        var goalTask = Task.withGoals("package").async().build();
        var mojo = new ExecMojo(Arrays.asList(goalTask, commandTask), invoker);

        mojo.execute();

        verify(invoker).execute(argThat(invocationRequest -> {
            assertThat(invocationRequest.getGoals()).containsExactly("package");
            return true;
        }));
        assertThat(Path.of(directory).resolve("test-file")).exists();
    }

}
