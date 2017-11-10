package io.jeffchao.streams.aggregator.sinks;

import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PostgresSink implements Processor<Windowed<String>, Long> {

  private static final Logger log = LoggerFactory.getLogger(PostgresSink.class);

  @Override
  public void init(ProcessorContext context) {
  }

  @Override
  public void process(Windowed<String> key, Long value) {
    log.info("writing to pg: window: {}, key: {}, value: {}", key.window(), key.key(), value);
  }

  @Override
  public void punctuate(long timestamp) {
  }

  @Override
  public void close() {
  }
}
