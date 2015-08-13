#!/bin/zsh

kill `cat spot-metrics-sender.pid`
logger -t spot-metrics "Terminated spot metrics zabbix sender"
rm spot-metrics-sender.pid
kill `cat spot-metrics.pid`
logger -t spot-metrics "Terminated spot metrics scraper"
kill `cat spot-metrics-network.pid`
logger -t spot-metrics-network "Terminated spot metrics scraper for network"
kill `cat spot-metrics-disk.pid`
logger -t spot-metrics "Terminated spot metrics scraper for disk"
kill `cat spot-metrics-cache.pid`
logger -t spot-metrics "Terminated spot metrics scraper for cache"
rm spot-metrics.pid
rm spot-metrics-network.pid
rm spot-metrics-disk.pid
rm spot-metrics-cache.pid
