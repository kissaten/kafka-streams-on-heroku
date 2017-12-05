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
3. Kafka

### Config Vars

1. `SENDGRID_API_KEY` (optional via SendGrid addon)
2. `TESTING_EMAIL` (optional for sinking to a test email using SendGrid addon)

### Deploying

```
git push heroku master (runs ./gradlew clean stage)
```

### Setup

```
./setup <app name> <plan>
```

### Smoke Testing

```
heroku kafka:topics:write [prefix]textlines "hello world" -a <app>
heroku pg:psql -c 'select * from windowed_counts' HEROKU_POSTGRESQL_URL -a <app>
```
