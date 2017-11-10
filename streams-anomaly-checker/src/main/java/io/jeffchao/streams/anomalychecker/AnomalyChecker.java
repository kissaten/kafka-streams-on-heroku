package io.jeffchao.streams.anomalychecker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.Properties;

import io.jeffchao.streams.anomalychecker.sinks.EmailSink;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnomalyChecker {

  private static final Logger log = LoggerFactory.getLogger(AnomalyChecker.class);

  private static final String TOPIC_PREFIX =
      Optional.ofNullable(System.getenv("TOPIC_PREFIX")).orElse("");

  public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException,
      KeyStoreException, IOException, URISyntaxException {
    Properties streamsConfig = new AnomalyCheckerConfig().getProperties();

    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<String, String> words =
        builder.stream(String.format("%swords", TOPIC_PREFIX));

    words
        .filter((key, value) -> value.equalsIgnoreCase("1337"))
        .process(EmailSink::new);

    final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfig);

    streams.cleanUp();
    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}