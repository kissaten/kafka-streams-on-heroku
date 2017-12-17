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
2. Kafka
3. Heroku CLI
4. Heroku Kafka CLI Plugin

### Config Vars

1. `SENDGRID_API_KEY` (optional via SendGrid addon)
2. `TESTING_EMAIL` (optional for sinking to a test email using SendGrid addon)


### Setup

Install the Heroku CLI: https://devcenter.heroku.com/articles/heroku-cli


Install the Heroku Kafka CLI Plugin:

```
heroku plugins:install heroku-kafka
```

Clone the application:

```
$ git clone git@github.com:jeffchao/kafka-streams-on-heroku.git
```

Create the application:

```
$ cd kafka-streams-on-heroku
$ heroku apps:create <application name>
```

Deploy the application:

```
$ git push heroku master
```

Run the setup script:

```
$ ./setup <app name> <plan>
```

### Smoke Testing

```
$ heroku kafka:topics:write [prefix]textlines "hello world" -a <app>
$ heroku pg:psql -c 'select * from windowed_counts' HEROKU_POSTGRESQL_URL -a <app>
```

### Example Use Cases

#### Word Count

Let's generate some sample data. This will produce into Kafka lines from _Alice's Adventures in Wonderland_.

```bash
$ heroku run ruby data-generators/text-generator/stream-lines-to-kafka.rb data-generators/text-generator/alice-in-wonderland.txt
```

Alternatively, if you have Ruby and Bundler installed locally, you can run the data generator locally

```bash
$ bundle install --path=vendor/gems
$ cd data-generators/text-generator
$ HEROKU_KAFKA_URL=$(heroku config:get HEROKU_KAFKA_URL) \
HEROKU_KAFKA_CLIENT_CERT=$(heroku config:get HEROKU_KAFKA_CLIENT_CERT) \
HEROKU_KAFKA_CLIENT_CERT_KEY=$(heroku config:get HEROKU_KAFKA_CLIENT_CERT_KEY) \
HEROKU_KAFKA_TRUSTED_CERT=$(heroku config:get HEROKU_KAFKA_TRUSTED_CERT) \
HEROKU_KAFKA_PREFIX=$(heroku config:get HEROKU_KAFKA_PREFIX) \
bundle exec ruby stream-lines-to-kafka.rb alice-in-wonderland.txt
```

Now we can see the word count for specific time windows:

```bash
$ heroku pg:psql -c 'select * from windowed_counts order by count desc' HEROKU_POSTGRESQL_URL
```

#### Anomaly Detection

WIP
