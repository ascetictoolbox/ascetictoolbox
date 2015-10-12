#!/bin/bash

RUNTIME_DIR=$1
IP=$2

# Shutdown VM
virsh shutdown $IP
if [ $? -ne 0 ]
then
  echo "Error shutting down VM with IP $IP"
  exit 1
fi

# TODO Poll to see if VM has been shutdown, after timeout destroy

# Return MAC/IP pair so that it can be used by another VM
$RUNTIME_DIR/scripts/utils/rm-mac-ip.sh $RUNTIME_DIR $IP

exit 0
