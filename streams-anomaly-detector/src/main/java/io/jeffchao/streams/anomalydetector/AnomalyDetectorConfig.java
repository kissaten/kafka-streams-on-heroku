package io.jeffchao.streams.anomalydetector;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.heroku.sdk.EnvKeyStore;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class AnomalyDetectorConfig extends Properties {

  private static final Logger log = LoggerFactory.getLogger(AnomalyDetectorConfig.class);

  private static final String ADDON_SUFFIX = Optional.ofNullable(
      System.getenv("ADDON_SUFFIX")).orElse("");
  private static final String HEROKU_KAFKA = String.format("HEROKU_KAFKA%s", ADDON_SUFFIX);
  private static final String HEROKU_KAFKA_PREFIX = Optional.ofNullable(
      System.getenv(String.format("%s_PREFIX", HEROKU_KAFKA))).orElse("");
  private static final String HEROKU_KAFKA_URL = String.format("%s_URL", HEROKU_KAFKA);
  private static final String HEROKU_KAFKA_TRUSTED_CERT =
      String.format("%s_TRUSTED_CERT", HEROKU_KAFKA);
  private static final String HEROKU_KAFKA_CLIENT_CERT_KEY =
      String.format("%s_CLIENT_CERT_KEY", HEROKU_KAFKA);
  private static final String HEROKU_KAFKA_CLIENT_CERT =
      String.format("%s_CLIENT_CERT", HEROKU_KAFKA);

  private String bootstrapServers;

  Properties getProperties() throws URISyntaxException, CertificateException,
      NoSuchAlgorithmException, KeyStoreException, IOException {
    return buildDefaults();
  }

  private Properties buildDefaults() throws CertificateException, NoSuchAlgorithmException,
      KeyStoreException, IOException, URISyntaxException {
    Properties defaultProperties = new Properties();
    Properties herokuKafkaConfigVarProperties = buildHerokuKafkaConfigVars();
    Properties kafkaStreamsProperties = buildKafkaStreamsDefaults();

    defaultProperties.putAll(herokuKafkaConfigVarProperties);
    defaultProperties.putAll(kafkaStreamsProperties);


    return defaultProperties;
  }

  private Properties buildHerokuKafkaConfigVars() throws URISyntaxException, CertificateException,
      NoSuchAlgorithmException, KeyStoreException, IOException {
    Properties properties = new Properties();
    List<String> bootstrapServerList = Lists.newArrayList();

    Iterable<String> kafkaUrl = Splitter.on(",")
        .split(Preconditions.checkNotNull(System.getenv(HEROKU_KAFKA_URL)));

    for (String url : kafkaUrl) {
      URI uri = new URI(url);
      bootstrapServerList.add(String.format("%s:%d", uri.getHost(), uri.getPort()));

      switch (uri.getScheme()) {
      case "kafka":
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
        break;
      case "kafka+ssl":
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        EnvKeyStore envTrustStore = EnvKeyStore.createWithRandomPassword(
            HEROKU_KAFKA_TRUSTED_CERT);
        EnvKeyStore envKeyStore = EnvKeyStore.createWithRandomPassword(
            HEROKU_KAFKA_CLIENT_CERT_KEY, HEROKU_KAFKA_CLIENT_CERT);

        File trustStoreFile = envTrustStore.storeTemp();
        File keyStoreFile = envKeyStore.storeTemp();

        properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, envTrustStore.type());
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
            trustStoreFile.getAbsolutePath());
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, envTrustStore.password());
        properties.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, envKeyStore.type());
        properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreFile.getAbsolutePath());
        properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, envKeyStore.password());
        break;
      default:
        throw new URISyntaxException(uri.getScheme(), "Unknown URI scheme");
      }
    }

    bootstrapServers = Joiner.on(",").join(bootstrapServerList);

    return properties;
  }

  private Properties buildKafkaStreamsDefaults() {
    Properties properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG,
        String.format("%sanomaly-detector-app", HEROKU_KAFKA_PREFIX));
    properties.put(StreamsConfig.CLIENT_ID_CONFIG,
        String.format("%sanomaly-detector-client", HEROKU_KAFKA_PREFIX));
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.put(
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    properties.put(
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        Serdes.String().getClass().getName());
    properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
    properties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
        WallclockTimestampExtractor.class);

    return properties;
  }
}
