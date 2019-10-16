package io.jeffchao.streams.aggregator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import io.jeffchao.streams.aggregator.sinks.PostgresSink;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Aggregator {

  private static final Logger log = LoggerFactory.getLogger(Aggregator.class);

  private static final String ADDON_SUFFIX = Optional.ofNullable(
      System.getenv("ADDON_SUFFIX")).orElse("");
  private static final String HEROKU_KAFKA = String.format("HEROKU_KAFKA%s", ADDON_SUFFIX);
  private static final String HEROKU_KAFKA_PREFIX = Optional.ofNullable(
      System.getenv(String.format("%s_PREFIX", HEROKU_KAFKA))).orElse("");

  public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException,
      KeyStoreException, IOException, URISyntaxException {
    Properties streamsConfig = new AggregatorConfig().getProperties();

    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<Windowed<String>, String> words =
        builder.stream(String.format("%swords", HEROKU_KAFKA_PREFIX));

    words
        .groupBy((key, word) -> word)
        .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(10)))
        .count(Materialized.as("windowed-counts"))
        .toStream()
        .process(PostgresSink::new);

    final Topology topology = builder.build();

    // Log topology at startup, for debugging
    // More about the topology graph: https://www.confluent.io/blog/optimizing-kafka-streams-applications
    System.out.println(topology.describe());

    final KafkaStreams streams = new KafkaStreams(topology, streamsConfig);

    streams.cleanUp();
    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}
