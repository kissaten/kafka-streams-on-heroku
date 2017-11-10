package io.jeffchao.streams.aggregator.sinks;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PostgresSink implements Processor<Windowed<String>, Long> {

  private static final Logger log = LoggerFactory.getLogger(PostgresSink.class);

  private Connection connection;

  private static Connection getConnection() throws URISyntaxException, SQLException {
    URI dbUri = new URI(System.getenv("HEROKU_POSTGRESQL_URL"));

    String[] userInfo = Optional.ofNullable(dbUri.getUserInfo()).orElse(":").split(":");
    String username = userInfo.length == 0 ? null : userInfo[0];
    String password = userInfo.length == 0 ? null : userInfo[1];
    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

    return DriverManager.getConnection(dbUrl, username, password);
  }

  @Override
  public void init(ProcessorContext context) {
    try {
      connection = getConnection();
    } catch (URISyntaxException | SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void process(Windowed<String> key, Long value) {
    log.info("writing to pg: window: {}, key: {}, value: {}", key.window(), key.key(), value);
    try {
      PreparedStatement statement = connection.prepareStatement(
          "INSERT INTO windowed_counts (time_window, word, count) VALUES (?, ?, ?)");
      statement.setLong(1, key.window().start());
      statement.setString(2, key.key());
      statement.setLong(3, value);

      statement.execute();
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
    }

  }

  @Override
  public void punctuate(long timestamp) {
  }

  @Override
  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
