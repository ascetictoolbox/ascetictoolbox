#!/bin/zsh

while true; do

  # Start the spot-metrics scraper(s)
  ./spot-metrics.sh $1 & # TODO add other scrapers here
  echo "$!" > spot-metrics.pid
  logger -t spot-metrics "Started spot metrics scraper (cpu)..."
  ./spot-metrics-network.sh $1 &
  echo "$!" > spot-metrics-network.pid
  logger -t spot-metrics-network "Started spot metrics scraper (network)..."
  ./spot-metrics-disk.sh $1 &
  echo "$!" > spot-metrics-disk.pid
  logger -t spot-metrics-disk "Started spot metrics scraper (disk)..."
  ./spot-metrics-cache.sh $1 &
  echo "$!" > spot-metrics-cache.pid  
  logger -t spot-metrics-cache "Started spot metrics scraper (cache)..."

  # Start the spot metric zabbix sender
  tail -n 1 -F spot-metrics.log --pid=$(cat spot-metrics.pid) | $2 -z $3 -p 10051 -r -i "-" > spot-metrics-sender.log &
  echo "$!" > spot-metrics-sender.pid
  logger -t spot-metrics "Started spot metrics zabbix sender..."

  #Check to see if we are still alive   
  while true; do
    if ps -p $(cat spot-metrics.pid) > /dev/null && ps -p $(cat spot-metrics-sender.pid) > /dev/null
    then
      sleep 4 
    else
      # Clean up and break
      if [ -f spot-metrics.pid ] && [ -f spot-metrics-sender.pid ]
      then
        logger -t spot-metrics "ERROR: spot metrics scraper or zabbix sender terminated prematurely (check logs for reason), restarting..."
        ./stop.sh
        break;
      else
        exit 0
      fi
    fi  
  done
done