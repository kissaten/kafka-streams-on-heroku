package io.jeffchao.streams.anomalydetector.sinks;

import java.io.IOException;

import com.google.common.base.Strings;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailSink implements Processor<Windowed<String>, Long> {

  private static final Logger log = LoggerFactory.getLogger(EmailSink.class);

  @Override
  public void init(ProcessorContext context) {
  }

  private Content generateContent(Windowed<String> key, Long value) {
    return new Content("text/plain", "Hello, our realtime anomaly detector "
        + "has detected an issue for " + key.key() + " with "
        + value + " failed login attempts");
  }

  private void sendEmail(Windowed<String> key, Long value) throws IOException {
    Email from = new Email("example@example.com");
    String subject = String.format("Anomaly detected for %s", value);
    Email to = new Email(System.getenv("TESTING_EMAIL"));
    Content content = generateContent(key, value);
    Mail mail = new Mail(from, subject, to, content);

    SendGrid sendGrid = new SendGrid(System.getenv("SENDGRID_API_KEY"));
    Request request = new Request();
    request.method = Method.POST;
    request.endpoint = "mail/send";
    request.body = mail.build();
    sendGrid.api(request);
  }

  @Override
  public void process(Windowed<String> key, Long value) {
    if (Strings.isNullOrEmpty(System.getenv("SENDGRID_API_KEY"))) {
      log.info(generateContent(key, value).getValue());
    } else {
      try {
        log.info("Sending email to {} with content {}",
            System.getenv("TESTING_EMAIL"),
            generateContent(key, value).toString());
        sendEmail(key, value);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  @Override
  public void punctuate(long timestamp) {
  }

  @Override
  public void close() {
  }
}
