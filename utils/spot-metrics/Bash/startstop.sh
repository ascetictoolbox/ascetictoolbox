#!/bin/bash

on_die()
{
        echo "Recieved TERM signal, stopping spot metrics..." | logger -t spot-metrics

        # Kill the spot-metrics stuff
        ./stop.sh

        exit 0
}

INSTALL_PATH=$1
ZABBOIX_HOSTNAME=$2
ZABBIX_SENDER_BIN=$3
ZABBIX_FRONT_END=$4

if [ "$INSTALL_PATH" == "" ]
then
  echo "ERROR: Environment variables are not available, exiting..." | logger -t spot-metrics
  exit 1
fi

# Start it up
cd $INSTALL_PATH

./start.sh $ZABBOIX_HOSTNAME $ZABBIX_SENDER_BIN $ZABBIX_FRONT_END &

# Execute function on_die() receiving TERM signal
trap 'on_die' TERM

# Loop forever
while true ; do
        sleep 1
done

exit 0
