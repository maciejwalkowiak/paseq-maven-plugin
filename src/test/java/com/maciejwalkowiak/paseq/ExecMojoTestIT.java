package com.maciejwalkowiak.paseq;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

/**
 * Integration tests for {@link ExecMojo}.
 *
 * @author Maciej Walkowiak
 */
@MavenJupiterExtension
public class ExecMojoTestIT {

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:exec")
    @MavenTest
    void the_first_test_case(MavenExecutionResult result) {
        assertThat(result).isSuccessful();
    }

}
