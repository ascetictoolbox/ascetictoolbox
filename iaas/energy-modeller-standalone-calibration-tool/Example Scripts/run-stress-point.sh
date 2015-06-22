#!/bin/bash
PERCENTAGE=$1
CORES=$2
TIME=$3
echo "Running stress test with $PERCENTAGE% CPU Usage"
./start-stress.sh $PERCENTAGE $CORES
sleep $TIME;
./stop-stress.sh