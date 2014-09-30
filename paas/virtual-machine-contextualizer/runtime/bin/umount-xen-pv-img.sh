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

#Script for unmounting a xen-pv image file

MOUNTPOINT=null #Mountpoint of image

if [ $# -eq 1 ]; then

  MOUNTPOINT=$1
  if [ -d "`pwd`/$MOUNTPOINT" ]; then
    MOUNTPOINT="`pwd`/$MOUNTPOINT"
  fi

  if [ -d $MOUNTPOINT ]; then
    if [ "`mount | grep \"$MOUNTPOINT \"`" == "" ]; then
      echo "ERROR: no device mounted at $MOUNTPOINT"
      exit
    else
      echo "Found mount point at: $MOUNTPOINT"
    fi
  else
    echo "ERROR: mount point does not exist"
    exit 1
  fi
else
  echo "ERROR: NOT ENOUGH ARGUMENTS"
  echo "Usage:"
  echo "  ./umount-xen-pv-img.sh <MOUNT_POINT>"
  echo "Example:"
  echo "  ./umount-xen-pv-img.sh /home/me/my-images/mount-point"
  exit 1
fi

LOOPDEVPATH=`mount | grep "$MOUNTPOINT " | cut -d ' ' -f 1`
echo "Removing mount point..."
sudo umount -v $MOUNTPOINT

if [ -f /etc/redhat-release ]
then
  echo "Removing loop device..."
  sudo losetup -d $LOOPDEVPATH
fi

if [ -f /etc/debian_version ]
then
  echo "Removing loop device..."
  LOOPDEV=`echo ${LOOPDEVPATH:12} | cut -d 'p' -f -2`
  if [ "$LOOPDEV" == "" ]
  then
    sudo losetup -d $LOOPDEVPATH
  else
    sudo kpartx -v -d /dev/$LOOPDEV
    sudo losetup -d /dev/$LOOPDEV
  fi
fi