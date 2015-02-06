#!/bin/bash
echo "Starting emulated watt meter..."
(
while true; do
  kill -0 $(cat watt-meter-emulator.pid)
  if [ "$?" != "0" ]; then
    java -classpath . -XX:MaxPermSize=128m -Xms512m -Xmx512m -jar host-power-emulator-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
    echo "$!" > energy-modeller.pid
  else
    sleep 10
  fi
done
) > /dev/null 2>&1 &
echo $! > watt-meter-emulator-restarter.pid
echo "Emulated watt meter started with PID: $(cat energy-modeller.pid)"

./em-push-exec.sh
