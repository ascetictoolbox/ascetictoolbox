#!/bin/bash
CPU=$1

if [ "$CPU" == "" ]; then
  echo "First Argument should be CPU usage, exiting..."
  exit 1
fi

CORES=$2

if [ "$CORES" == "" ]; then
  echo "Second Argument should be core count, exiting..."
  exit 1
fi

stress -c $CORES --backoff 1000000 &
PID=$!
sleep 0.5
CPIDS=$(pidof -o $PID stress)
echo "Stress PIDs are: $CPIDS"
CORE=0
for CPID in $CPIDS; do
  cpulimit -p $CPID -l $CPU &
  taskset -cp $CORE $CPID
  let "CORE++"
done
