package io.jeffchao.streams.anomalydetector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import io.jeffchao.streams.anomalydetector.sinks.AlertSink;
import io.jeffchao.streams.anomalydetector.sinks.EmailSink;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnomalyDetector {

  private static final Logger log = LoggerFactory.getLogger(AnomalyDetector.class);

  private static final String ADDON_SUFFIX = Optional.ofNullable(
      System.getenv("ADDON_SUFFIX")).orElse("");
  private static final String HEROKU_KAFKA = String.format("HEROKU_KAFKA%s", ADDON_SUFFIX);
  private static final String HEROKU_KAFKA_PREFIX = Optional.ofNullable(
      System.getenv(String.format("%s_PREFIX", HEROKU_KAFKA))).orElse("");

  public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException,
      KeyStoreException, IOException, URISyntaxException {
    Properties streamsConfig = new AnomalyDetectorConfig().getProperties();

    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<String, String> words =
        builder.stream( String.format("%sloglines", HEROKU_KAFKA_PREFIX));

    KStream<Windowed<String>, Long> anomalies = words
        .filter((key, value) -> value.contains("failed login"))
        .selectKey((key, value) -> value.split("\\|")[0])
        .groupByKey()
        .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(10)))
        .count(Materialized.as("windowed-counts"))
        .toStream();

    @SuppressWarnings("unchecked")
    KStream<Windowed<String>, Long>[] branches = anomalies
        .branch(
            (key, value) -> value > 1,
            (key, value) -> value > 0
        );

    branches[0].process(AlertSink::new);
    branches[1].process(EmailSink::new);

    final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfig);

    streams.cleanUp();
    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}