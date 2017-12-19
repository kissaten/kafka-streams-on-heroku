require_relative '../stream-to-kafka'

initialize_kafka

puts "Reading text file."
count = 0
ARGF.each_line do |line|
  line.strip!
  unless line.empty?
    produce(line)
    count += 1
  end
  print '.' if (ARGF.lineno % 100) == 0
end

puts "\nRead #{count} (non-blank) lines."
