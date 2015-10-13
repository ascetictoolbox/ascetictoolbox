#!/bin/bash

# TODO: make VNC port match last octet

RUNTIME_DIR="$1"
IMAGE_PATH="$2"
OS="$3"
INSTALL_DIR="$(dirname $RUNTIME_DIR)"

if [ "$OS" == "windows" ]
then
  cd $INSTALL_DIR/base-images/windows
  
  # 1) Copy the base image reporting progress / performance
  mkdir -p "$(dirname $IMAGE_PATH)"
  rsync -avzPh win-2k3.raw.img $IMAGE_PATH
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
  cat win-2k3.raw.img.xml | sed -e "s|<name>win-2k3.raw.img</name>|<name>$IP</name>|" | sed -e "s|$INSTALL_DIR/base-images/windows/win-2k3.raw.img|$IMAGE_PATH|" | sed -e "s|02:00:0a:0a:ef:fe|$MAC|" >> $IMAGE_PATH.xml
 
  # 3) Create the VM
  virsh create $IMAGE_PATH.xml
  if [ $? -ne 0 ]
  then
    echo "Error instantiating VM via libvirt"
    exit 1
  fi

  # 4) Confirm that VM has booted into OS
  cd ../../runtime/chef-repo
  while true
  do
    knife wsman test $IP -m > /dev/null 2>&1
    if [ $? -ne 0 ]
    then
      echo "Waiting for VM OS to initialise"
      sleep 2
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
  rsync -avzPh deb-wheezy.raw.img $IMAGE_PATH
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
  cat deb-wheezy.raw.img.xml | sed -e "s|<name>deb-wheezy.raw.img</name>|<name>$IP</name>|" | sed -e "s|$INSTALL_DIR/base-images/linux/deb-wheezy.raw.img|$IMAGE_PATH|" | sed -e "s|02:00:0a:0a:ef:fe|$MAC|" >> $IMAGE_PATH.xml

  # 3) Create the VM
  virsh create $IMAGE_PATH.xml
  if [ $? -ne 0 ]
  then
    echo "Error instantiating VM via libvirt"
    exit 1
  fi

  # 4) Confirm that VM has booted into OS
  while true
  do
    sshpass -p "password" ssh -q -o "StrictHostKeyChecking no" -o "ConnectTimeout=1" root@$IP exit
    if [ $? -ne 0 ]
    then
      echo "Waiting for VM OS to initialise"
      sleep 1
    else
      echo "Connected successfully to $IP via SSH"
      break
    fi
  done

  # 5) Return IP
  echo "$IP"
fi

exit 0
