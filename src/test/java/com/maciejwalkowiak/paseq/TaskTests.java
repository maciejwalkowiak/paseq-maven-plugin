package com.maciejwalkowiak.paseq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Task}.
 *
 * @author Maciej Walkowiak
 */
class TaskTests {

    @Test
    void throwsExceptionWhenTaskDoesNotHaveGoalNorCommand() {
        var task = new Task();
        assertThatThrownBy(task::validate).isInstanceOf(InvalidTaskDefinitionException.class);
    }

    @Test
    void taskIsSyncByDefault() {
        var task = Task.withGoals("clean").build();
        assertThat(task.isAsync()).isFalse();
    }

    @Test
    void taskDoesNotWaitByDefault() {
        var task = Task.withGoals("clean").build();
        assertThat(task.isWait()).isFalse();
    }

    @Test
    void throwsExceptionWhenTaskHasBothGoalsAndCommand() {
        var task = Task.withGoals("clean").setExec(new Exec("ls -l")).build();
        assertThatThrownBy(task::validate).isInstanceOf(InvalidTaskDefinitionException.class);
    }
}
