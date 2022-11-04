package com.maciejwalkowiak.paseq;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

@MavenJupiterExtension
public class ExecMojoTestIT {

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:exec")
    @MavenTest // <2>
    void the_first_test_case(MavenExecutionResult result) { // <3>
        assertThat(result).isSuccessful(); // <4>
    }

}
