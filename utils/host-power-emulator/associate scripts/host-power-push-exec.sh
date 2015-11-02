#!/bin/bash
echo "Starting zabbix push..."
(
while true; do
  kill -0 $(cat watt-meter-emulator-push.pid)
  if [ "$?" != "0" ]; then
    tail -n 1 -F EstimatedHostPowerData.txt | /usr/local/zabbix-2.2.6/bin/zabbix_sender -z 127.0.0.1 -p 10051 -r -i "-" > watt-meter-push.log &
    echo $! > watt-meter-emulator-push.pid
  else
    sleep 10
  fi
done
) > /dev/null 2>&1 &
echo $! > watt-meter-emulator-push-restarter.pid
echo "Zabbix push started with PID: $(cat watt-meter-emulator-push.pid)"
