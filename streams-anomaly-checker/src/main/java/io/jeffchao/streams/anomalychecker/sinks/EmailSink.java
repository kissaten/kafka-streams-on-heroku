package io.jeffchao.streams.anomalychecker.sinks;

import java.io.IOException;

import com.google.common.base.Strings;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailSink implements Processor<String, String> {

  private static final Logger log = LoggerFactory.getLogger(EmailSink.class);

  @Override
  public void init(ProcessorContext context) {
  }

  private void sendEmail(String key, String value) throws IOException {
    Email from = new Email("example@example.com");
    String subject = String.format("Anomaly detected for %s", value);
    Email to = new Email(System.getenv("TESTING_EMAIL"));
    Content content = new Content("text/plain", "Hello, our realtime anomaly detector "
        + "has detected the word " + value + "!");
    Mail mail = new Mail(from, subject, to, content);

    SendGrid sendGrid = new SendGrid(System.getenv("SENDGRID_API_KEY"));
    Request request = new Request();
    request.method = Method.POST;
    request.endpoint = "mail/send";
    request.body = mail.build();
    sendGrid.api(request);
  }

  @Override
  public void process(String key, String value) {
    if (Strings.isNullOrEmpty(System.getenv("ENVIRONMENT"))) {
      try {
        log.info("Sending email to {}", System.getenv("TESTING_EMAIL"));
        sendEmail(key, value);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    } else if (System.getenv("ENVIRONMENT").equalsIgnoreCase("local")) {
      log.info("Sending email: {}", value);
    }

  }

  @Override
  public void punctuate(long timestamp) {
  }

  @Override
  public void close() {
  }
}
