#!/bin/bash
echo "Start chrome with remote debugging..."

sh /usr/bin/start.sh &

echo "Waiting for chrome to launch..."

while ! nc -z localhost 9222; do   
  sleep 0.1 # wait for 1/10 of the second before check again
done

echo "Chrome started. Perform audit..."
lighthouse --verbose --port 9222 $URL --output=html --output-path=./$AUDIT_NAME 
chmod og+wr report.html
