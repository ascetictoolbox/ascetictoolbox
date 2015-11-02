#!/bin/bash

# Copyright 2013 University of Leeds
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#  
#       http://www.apache.org/licenses/LICENSE-2.0
#  
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#Script for mounting a hvm image file

IMAGENAME=null #Image name
MOUNTPOINT=null #Mountpoint of image
PARTITION=null #Partition in image


if [ "$#" -ge "2" ]; then

  IMAGENAME=$1
  if [ -f "`pwd`/$IMAGENAME" ]; then
    IMAGENAME="`pwd`/$IMAGENAME"
  fi

  MOUNTPOINT=$2
  if [ -d "`pwd`/$MOUNTPOINT" ]; then
    MOUNTPOINT="`pwd`/$MOUNTPOINT"
  fi

  if [ "$#" -eq "2" ]; then
    echo "Using default partition number \"1\""
    PARTITION=1
  else
    PARTITION=$3
  fi

  if [ -f $IMAGENAME ]; then
    echo "Found image file at: $IMAGENAME"
    if [ "`ps -A -o args | grep '$IMAGENAME' | grep 'qemu-nbd -c /dev/' | grep -v grep`" != ""  ]; then
      echo "ERROR: image file seems to be already mounted or in use by qemu-nbd"
      exit 1
    fi
  else
    echo "ERROR: image file not found"
    exit 1
  fi
  if [ -d $MOUNTPOINT ]; then
    echo "Found mount point at: $MOUNTPOINT"

    if [ "`find $MOUNTPOINT -type f | tail -n 1`" != "" ]; then
      echo "ERROR: mount point is not empty"
      exit 1
    fi

    if [ "`mount | grep \"$MOUNTPOINT \"`" != "" ]; then
      echo "ERROR: device already mounted at $MOUNTPOINT"
      exit 1
    fi
  else
    echo "ERROR: mount point does not exist"
    echo "  Create it using: \"mkdir -p $MOUNTPOINT\""
    exit 1
  fi
else
  echo "ERROR: NOT ENOUGH ARGUMENTS"
  echo "Usage:"
  echo "  ./mount-hvm-img.sh <IMAGE_NAME> <MOUNT_POINT> [PARTITION_NUMBER default=1]"
  echo "Example:"
  echo "  ./mount-hvm-img.sh /home/me/my-images/my-image.raw.img /home/me/my-images/mount-point 1"
  exit 1
fi

LASTNBDNUM=`cat /proc/partitions | grep nbd | awk '{print $4}' | tail -n 1 | cut -c 4-5`

if [ "$LASTNBDNUM" == "" ]; then
  NBDNUM=0
else
  NBDNUM=$(expr $LASTNBDNUM + 1)
fi

while true; do

  if [ $NBDNUM -gt 15 ]; then
    echo "ERROR: No more Network block devices available"
    exit 1
  fi

  NBDDEV=/dev/nbd$NBDNUM

  echo "Using first network block device: $NBDDEV"
  sudo modprobe nbd max_part=32 
  qemu-nbd -c $NBDDEV $IMAGENAME
  QEMU_NBD_CMD="qemu-nbd -c $NBDDEV $IMAGENAME"
  QEMU_NBD_PID=`pgrep -f "$QEMU_NBD_CMD"`
  echo "Found qemu-nbd PID, it is: $QEMU_NBD_PID"

  if [ -f /etc/redhat-release ]; then  
    echo "Mapping partitions from $NBDDEV"
    sudo kpartx -av $NBDDEV
    MAP="/dev/mapper/nbd"$NBDNUM"p"$PARTITION
  elif [ -f /etc/debian_version ]; then
    #Debian uses partx automagically
    MAP="/dev/nbd"$NBDNUM"p"$PARTITION
  fi

  echo "Mounting partition from $MAP"
  sudo mount $MAP $MOUNTPOINT
  if [ $? -ne 0 ]; then
    echo "ERROR: Failed to mount image, trying to clean up..."
    if [ -f /etc/redhat-release ]; then
      sudo kpartx -dv $NBDDEV
    fi
    qemu-nbd -d $NBDDEV
    kill -9 $QEMU_NBD_PID
    NBDNUM=$(expr $NBDNUM + 1)
    echo "Trying again using nbd device: /dev/nbd$NBDNUM"
  else 
    echo "Image mounted at: $MOUNTPOINT"
    break 
  fi

done