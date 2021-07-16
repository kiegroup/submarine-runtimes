# Generate Kogito Project based on Spring Boot runtimes

To generate new project based on Spring Boot use following command:

```shell
mvn archetype:generate \
    -DarchetypeGroupId=org.kie.kogito \
    -DarchetypeArtifactId=kogito-springboot-archetype \
    -DarchetypeVersion=2.0.0-SNAPSHOT \
    -DgroupId=com.company \
    -DartifactId=sample-kogito
```

## Adding Kogito Spring Boot Starters

Optionally, you can generate the project with additional Spring Boot Starters provided by Kogito.

<!-- Include Starters Table or link to the README -->

Run the following command with the property `starters` with every starter you want to be added to the project as a comma
separated list:

```shell
mvn archetype:generate \
    -DarchetypeGroupId=org.kie.kogito \
    -DarchetypeArtifactId=kogito-springboot-archetype \
    -DarchetypeVersion=2.0.0-SNAPSHOT \
    -DgroupId=com.company \
    -DartifactId=sample-kogito
    -Dstarters=monitoring-prometheus,persistence-postgres
```

You can find a list with the provided starters here. <!-- Include Starters Table or link to the README -->
