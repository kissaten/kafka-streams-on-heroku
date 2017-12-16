package io.jeffchao.streams.anomalydetector.sinks;

import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlertSink implements Processor<Windowed<String>, Long> {

  private static final Logger log = LoggerFactory.getLogger(AlertSink.class);

  @Override
  public void init(ProcessorContext context) {
  }

  @Override
  public void process(Windowed<String> key, Long value) {
      log.info("Too many login failures for user: {}, count: {}. Alerting to PagerDuty.",
          key.key(), value);
  }

  @Override
  public void punctuate(long timestamp) {
  }

  @Override
  public void close() {
  }
}
