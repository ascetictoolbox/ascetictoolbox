#!/bin/bash

# Change to working directory from runtime directory
cd $1/scripts/utils
IP="$2"

#TODO Implement file lock to enable concurrency

LINES=()
IFS=$'\n' read -rd '' -a LINES < used-mac-ips.list

# Look through each lines looking for the IP to mark as available
for LINE in "${LINES[@]}"
do
  NEXT_IP="$(echo $LINE | cut -d ' ' -f2 )"
  if [ "$NEXT_IP" == "$IP" ]
  then
    # Found the IP so add the MAC/IP pair back
    echo $LINE >> available-mac-ips.list
    # Recreate the used MAC/IP pair list without the MAC/IP pair just found
    truncate -s 0 used-mac-ips.list
    for NEW_LINE in "${LINES[@]}"
    do
      if [ "$NEW_LINE" != "$LINE" ]
      then
        echo $NEW_LINE >> used-mac-ips.list
      fi
    done
    # All is well
    exit 0
  fi
done
 
# Didn't find the IP so error out
echo "Failed to find IP address $IP in used MAC/IP list"
exit 1
