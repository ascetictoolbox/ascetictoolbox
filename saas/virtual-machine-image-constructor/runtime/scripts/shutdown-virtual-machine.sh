#!/bin/bash

RUNTIME_DIR=$1
IP=$2

SHUTDOWN_TIMEOUT=60

# Shutdown VM
virsh shutdown $IP
if [ $? -ne 0 ]
then
  echo "Error shutting down VM with IP $IP"
  exit 1
fi

# Poll to see if VM has been shutdown, after timeout destroy
START_TIME=$(date +%s)
while true
do
  virsh list | grep -q $IP
  if [ $? -ne 1 ]
  then
    TIME_NOW=$(date +%s)
    TIME=$((TIME_NOW - START_TIME))
    if [ $TIME -gt $SHUTDOWN_TIMEOUT ]
    then
      # Timeout reached so kill the VM 
      virsh destroy $IP
      echo "Shutdown timeout reached, destroyed VM with IP $IP"
      break
    else
      # Wait
      echo "Waiting for VM to shutdown (Time waiting: $TIME)"
      sleep 1
    fi
  else
    echo "VM with IP $IP successfully shutdown"
    break
  fi
done

# Return MAC/IP pair so that it can be used by another VM
$RUNTIME_DIR/scripts/utils/rm-mac-ip.sh $RUNTIME_DIR $IP

exit 0
