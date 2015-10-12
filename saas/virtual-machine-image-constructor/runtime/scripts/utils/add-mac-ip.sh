#!/bin/bash

# Change to working directory from runtime directory
cd $1/scripts/utils

#TODO Implement file lock to enable concurrency

LINES=""
IFS=$'\n' read -rd '' -a LINES < available-mac-ips.list

# Find the first available MAC/IP pair
NEXT_MAC_IP="${LINES[0]}"

if [ "$NEXT_MAC_IP" == "" ]
then
  # No more MAC/IPs so return error
  exit 1
else
  # Store the remaining MAC/IP pairs as long as we have something to store
  if [ "${LINES[1]}" != "" ] 
  then
    printf '%s\n' "${LINES[@]:1}" > available-mac-ips.list
  else
    # Nothing to store so truncate the list
    truncate -s 0 available-mac-ips.list
  fi
  # Add this pair to the used list
  echo $NEXT_MAC_IP >> used-mac-ips.list
  # return the MAC/IP pair
  echo $NEXT_MAC_IP
  exit 0
fi
