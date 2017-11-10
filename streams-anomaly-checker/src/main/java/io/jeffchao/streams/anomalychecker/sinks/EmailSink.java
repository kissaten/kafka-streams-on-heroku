package io.jeffchao.streams.anomalychecker.sinks;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailSink implements Processor<String, String> {

  private static final Logger log = LoggerFactory.getLogger(EmailSink.class);

  @Override
  public void init(ProcessorContext context) {
  }

  @Override
  public void process(String key, String value) {
    log.info("sending email: {}", value);
  }

  @Override
  public void punctuate(long timestamp) {
  }

  @Override
  public void close() {
  }
}
