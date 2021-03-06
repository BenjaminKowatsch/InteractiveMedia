#!/bin/bash
echo "Start chrome with remote debugging..."

sh /usr/bin/start.sh &

echo "Waiting for chrome to launch..."

while ! nc -z localhost 9222; do   
  sleep 0.1 # wait for 1/10 of the second before check again
done

rm report.report.json report.report.html

echo "Chrome started. Perform audit..."
lighthouse --port 9222 $URL --output json --output html --output-path ./report.json --config-path=/usr/bin/config.js
chmod uog+wxr ./ -R
chmod uog+wxr report.report.json report.report.html
