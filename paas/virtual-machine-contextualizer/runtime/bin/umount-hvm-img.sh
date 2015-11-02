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

#Script for unmounting a hvm images file

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
  echo "  ./umount-hvm-img.sh <MOUNT_POINT>"
  echo "Example:"
  echo "  ./umount-hvm-img.sh /home/me/my-images/mount-point"
  exit 1
fi

NBDDEV=/dev/`mount | grep $MOUNTPOINT | cut -d ' ' -f 1 | cut -d 'p' -f 3 | cut -d '/' -f 2`
echo "Removing mount point..."
sudo umount -v $MOUNTPOINT

if [ -f /etc/redhat-release ]; then
  echo "Removing partition map..."
  sudo kpartx -dv $NBDDEV
fi

echo "Disconnecting NDB device..."
qemu-nbd -d $NBDDEV

QEMU_NBD_CMD="qemu-nbd -c $NBDDEV"
QEMU_NBD_PID=`pgrep -f "$QEMU_NBD_CMD"`
if [ "QEMU_NBD_PID" != "" ]; then
  echo "Found qemu-nbd still alive with PID=$QEMU_NBD_PID, killing it..."
  kill -9 $QEMU_NBD_PID
fi