require_relative '../stream-to-kafka'
require 'faker'

# spec: user_id | timestamp | ip_address | action | message
def log_line(action: 'login succeeded')
  user_id    = Faker::Internet.user_name
  timestamp  = Time.now
  ip_address = Faker::Internet.ip_v4_address
  message    = Faker::Lorem.words(3,0).join(' ')

  "user_id: #{user_id} | timestamp: #{timestamp} | "\
  "ip_address: #{ip_address} | action: #{action} | message: #{message}"
end

def log_line_with_anomaly
  log_line(action: 'login failed')
end

initialize_kafka

messages_per_second = ARGV[0]
probability_of_anomality = ARGV[1].to_f * 100

puts "Generating #{messages_per_second} log lines per second "\
     "with #{probability_of_anomality}% chance of anomaly. "\
     "CTRL-C to stop."

count = 0

trap "SIGINT" do
  puts "\nGenerated #{count} log lines. Stopping."
  exit 130
end

while true
  if rand(100) <= probability_of_anomality
    produce(log_line_with_anomaly, to: 'loglines')
  else
    produce(log_line, to: 'loglines')
  end
  count += 1
  print "#{count}..." if (count % 10) == 0
  sleep (1 / messages_per_second.to_f)
end
