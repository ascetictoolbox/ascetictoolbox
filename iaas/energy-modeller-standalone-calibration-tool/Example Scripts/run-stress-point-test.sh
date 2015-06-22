#!/bin/bash
PERCENTAGE=50
CORES=4
TIME=5
./start-stress.sh $PERCENTAGE $CORES >output.log 2>error.log
htop
sleep $TIME;
./stop-stress.sh
