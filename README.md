# kafka-streams-on-heroku

Kafka Streams example on Heroku with a multi-project gradle build

## Dependencies

1. Postgres
2. Kafka (+ Zookeeper) 0.10+ (this uses 0.11 brokers against 1.0 client)
3. Java 8
4. Gradle 4.3 (use sdkman)

## Local Development

### Building

```
$ ./gradlew clean build
```

### Testing

```
$ ./gradlew clean test
```

### Building FatJar Artifacts

```
$ ./gradlew clean stage
```

### Running Locally

Topologies are organized as subprojects. You can run any or all of them

```
(start postgres - optional, zookeeper - required, kafka - required)
$ ./gradlew streams-text-processor:run
$ ./gradlew streams-aggregator:run
$ ./gradlew streams-anomaly-checker:run

```

## Deployment: Heroku

### Dependencies

1. Postgres
2. Sendgrid
3. Kafka (dedicated)

### Config Vars

1. `ADDON_SUFFIX` (if addon name is different from `HEROKU_KAFKA`)
2. `SENDGRID_API_KEY`
3. `TESTING_EMAIL` (for sinking to a test email)

### Deploying

```
git push heroku master (runs ./gradlew clean stage)
```