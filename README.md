# Paseq Maven Plugin

![Logo](docs/logo.png)

by [@maciejwalkowiak](https://twitter.com/maciejwalkowiak)

Paseq Maven Plugin executes series of commands or Maven goals sequentially or in parallel.

**Warning**: early stage of development. Not yet in Maven Central. Feedback very welcome!

## Example

Plugin has to be configured in `build/plugins` section of `pom.xml`:

```xml
<build>
    <plugins>
        <!-- ... -->
        <plugin>
            <groupId>com.maciejwalkowiak.paseq</groupId>
            <artifactId>paseq-maven-plugin</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <configuration>
                <tasks>
                    <!-- runs docker-compose from 'etc' directory relative to pom.xml -->
                    <task>
                        <exec>
                            <directory>etc</directory>
                            <command>docker-compose up -d --wait</command>
                        </exec>
                    </task>
                    <!-- runs npx in a background process -->
                    <task>
                        <async>true</async>
                        <exec>
                            <command>npx run develop</command>
                        </exec>
                    </task>
                    <!-- runs spring-boot:run after previous sync task finishes -->
                    <task>
                        <goals>spring-boot:run</goals>
                    </task>
                </tasks>
            </configuration>
        </plugin>
        <!-- ... -->
    </plugins>
</build>
```

Then the series of commands can be executed with:

```bash
$ mvn paseq:exec
```

## Configuration

**Task** can have either `goals` or `exec` configured:

- `goals` - Maven goals or lifecycle phases. Can be either a list or comma-separated list
- `exec` - executable run in a separate process. Must have `command` configured, and optionally can have `directory` which sets the directory in which the command gets executed
- `async` - if task should be executed in the background thread. By default `false`
- `wait` - if task should wait for all async tasks started before

---
<a href="https://www.flaticon.com/free-icons/belt" title="belt icons">Belt icons created by Freepik - Flaticon</a>
