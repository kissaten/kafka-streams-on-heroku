require 'kafka'
require 'tempfile'


def initialize_kafka
  tmp_ca_file = Tempfile.new('ca_certs')
  tmp_ca_file.write(ENV.fetch('HEROKU_KAFKA_TRUSTED_CERT'))
  tmp_ca_file.close

  producer_kafka = Kafka.new(
    seed_brokers: ENV.fetch('HEROKU_KAFKA_URL'),
    ssl_ca_cert_file_path: tmp_ca_file.path,
    ssl_client_cert: ENV.fetch('HEROKU_KAFKA_CLIENT_CERT'),
    ssl_client_cert_key: ENV.fetch('HEROKU_KAFKA_CLIENT_CERT_KEY')
  )
  $producer = producer_kafka.async_producer(
    delivery_interval: 1,
    max_buffer_size: 10_000,
    max_buffer_bytesize: 100_000_000,
    required_acks: :all
  )
  puts "Producer connected to Kafka."

  at_exit do
    $producer.shutdown
    tmp_ca_file.unlink
    puts "Producer shutdown."
  end
end

def kafka_topic(topic_name)
  kafka_topic = ENV.fetch('HEROKU_KAFKA_TOPIC', topic_name)
  if ENV['HEROKU_KAFKA_PREFIX']
    kafka_topic = ENV['HEROKU_KAFKA_PREFIX'] + kafka_topic
  end
  
  kafka_topic
end

def produce(message, to: 'textlines')
  $producer.produce(message, topic: kafka_topic(to))
end
