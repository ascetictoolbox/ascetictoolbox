#!/bin/bash

IMAGE_PATH="$1"
OS="$2"
RUNTIME_DIR="$(cd $(dirname $0); cd .. ; pwd -P)"
INSTALL_DIR="$(dirname $RUNTIME_DIR)"

INIT_TIMEOUT=90

if [ "$OS" == "windows" ]
then
  cd $INSTALL_DIR/base-images/windows
  
  # 1) Copy the base image reporting progress / performance
  mkdir -p "$(dirname $IMAGE_PATH)"
  echo "Copying windows base image via efficient inplace partial file delta encoding"
  sudo su - root -c "rsync -avPh --inplace --no-whole-file $INSTALL_DIR/base-images/windows/win-2k3.raw.img $IMAGE_PATH"
  if [ $? -ne 0 ]
  then
    echo "Error copying windows base image"
    exit 1
  fi

  # 2) Add the IMAGE_PATH to the libvirt XML, set the name to the IP and ask for the next available MAC/IP pair
  MAC_IP="$($RUNTIME_DIR/scripts/utils/add-mac-ip.sh $RUNTIME_DIR)"
  # Test to make sure a MAC/IP pair is available
  if [ $? -ne 0 ]
  then
    echo "Error assigning MAC/IP pair to VM, ran out of IPs"
    exit 1
  fi
  MAC="$(echo $MAC_IP | cut -d ' ' -f1)" 
  IP="$(echo $MAC_IP | cut -d ' ' -f2)"
  # Output the new XML here
  truncate -s 0 $IMAGE_PATH.xml
  echo "Creating libvirt domain XML definition"
  cat win-2k3.raw.img.xml | sed -e "s|port='5910' ||" | sed -e "s|<name>win-2k3.raw.img</name>|<name>$IP</name>|" | sed -e "s|$INSTALL_DIR/base-images/windows/win-2k3.raw.img|$IMAGE_PATH|" | sed -e "s|02:00:0a:0a:ef:fe|$MAC|" >> $IMAGE_PATH.xml
 
  # 3) Create the VM
  virsh create $IMAGE_PATH.xml
  if [ $? -ne 0 ]
  then
    echo "Error instantiating VM via libvirt"
    echo $IP # Needed for clean up
    exit 1
  fi

  # 4) Confirm that VM has booted into OS
  cd ../../runtime/chef-repo
  START_TIME=$(date +%s)
  REBOOT=0
  while true
  do
    knife wsman test $IP -m > /dev/null 2>&1
    if [ $? -ne 0 ]
    then
      TIME_NOW=$(date +%s)
      TIME=$((TIME_NOW - START_TIME))
      if [ $TIME -gt $INIT_TIMEOUT ]
      then
        # Timeout reached
        if [ $REBOOT -eq 1 ]
        then
          # Reboot didn't work so error out
          echo "OS init failed after reboot"
          echo $IP # Needed for clean up
          exit 1
        else
          # First time we've reached the timeout so try rebooting
          echo "OS init timeout reached, rebooting VM with IP $IP"
          virsh reboot $IP
          REBOOT=1
          # Reset the start time
          START_TIME=$(date +%s)
        fi
      else
        # Keep waiting...
        echo "Waiting for VM OS to initialise (Time waiting: $TIME)"
        sleep 1
      fi
    else
      echo "Connected successfully to $IP at http://$IP:5985/wsman."
      break
    fi
  done

  # 5) Return IP
  echo "$IP"

elif [ "$OS" == "linux" ]
then
  cd $INSTALL_DIR/base-images/linux

  # 1) Copy the base image reporting progress / performance
  mkdir -p "$(dirname $IMAGE_PATH)"
  echo "Copying linux base image via efficient inplace partial file delta encoding"
  sudo su - root -c "rsync -avPh --inplace --no-whole-file $INSTALL_DIR/base-images/linux/deb-wheezy.raw.img $IMAGE_PATH"
  if [ $? -ne 0 ]
  then
    echo "Error copying linux base image"
    exit 1
  fi

  # 2) Add the IMAGE_PATH to the libvirt XML, set the name to the IP and ask for the next available MAC/IP pair
  MAC_IP="$($RUNTIME_DIR/scripts/utils/add-mac-ip.sh $RUNTIME_DIR)"
  # Test to make sure a MAC/IP pair is available
  if [ $? -ne 0 ]
  then
    echo "Error assigning MAC/IP pair to VM, ran out of IPs"
    exit 1
  fi
  MAC="$(echo $MAC_IP | cut -d ' ' -f1)"
  IP="$(echo $MAC_IP | cut -d ' ' -f2)"
  # Output the new XML here
  truncate -s 0 $IMAGE_PATH.xml
  echo "Creating libvirt domain XML definition"
  cat deb-wheezy.raw.img.xml | sed -e "s|port='5910' ||" | sed -e "s|<name>deb-wheezy.raw.img</name>|<name>$IP</name>|" | sed -e "s|$INSTALL_DIR/base-images/linux/deb-wheezy.raw.img|$IMAGE_PATH|" | sed -e "s|02:00:0a:0a:ef:fe|$MAC|" >> $IMAGE_PATH.xml

  # 3) Create the VM
  virsh create $IMAGE_PATH.xml
  if [ $? -ne 0 ]
  then
    echo "Error instantiating VM via libvirt"
    echo $IP # Needed for clean up
    exit 1
  fi

  # 4) Confirm that VM has booted into OS
  START_TIME=$(date +%s)
  REBOOT=0
  while true
  do
    sshpass -p "password" ssh -q -o "StrictHostKeyChecking no" -o "ConnectTimeout=1" root@$IP exit
    if [ $? -ne 0 ]
    then
      TIME_NOW=$(date +%s)
      TIME=$((TIME_NOW - START_TIME))
      if [ $TIME -gt $INIT_TIMEOUT ]
      then
        # Timeout reached
        if [ $REBOOT -eq 1 ]
        then
          # Reboot didn't work so error out
          echo "OS init failed after reboot"
          echo $IP # Needed for clean up
          exit 1
        else
          # First time we've reached the timeout so try rebooting
          echo "OS init timeout reached, rebooting VM with IP $IP"
          virsh reboot $IP
          REBOOT=1
          # Reset the start time
          START_TIME=$(date +%s)
        fi
      else
        # Keep waiting...
        echo "Waiting for VM OS to initialise (Time waiting: $TIME)"
        sleep 1
      fi
    else
      echo "Connected successfully to $IP via SSH"
      break
    fi
  done

  # 5) Return IP
  echo "$IP"

fi

exit 0
