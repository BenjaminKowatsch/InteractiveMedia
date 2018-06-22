#!/bin/bash
counter=0
limit=$2
while [ ! -f $1 ] && [ "$counter" -lt "$limit" ]
do 
	sleep 1s 
	counter=$((counter+1))
	echo "Waited one second $counter"
done

if [ ! "$counter" -lt "$limit" ]
then
	echo "Timeout reached"
	exit 1
else
	echo "File exists now"
	exit 0
fi
