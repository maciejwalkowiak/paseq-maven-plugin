package com.maciejwalkowiak.paseq;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

@MavenJupiterExtension
public class RunMojoTestIT {

    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:run")
    @MavenTest // <2>
    void the_first_test_case(MavenExecutionResult result) { // <3>
        assertThat(result).isSuccessful(); // <4>
    }

}

