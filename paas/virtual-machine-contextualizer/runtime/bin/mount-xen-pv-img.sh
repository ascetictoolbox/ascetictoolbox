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

#Script for mounting a xen-pv images file

IMAGENAME=null #Image name
MOUNTPOINT=null #Mountpoint of image

if [ $# -eq 2 ]; then
  IMAGENAME=$1
  if [ -f "`pwd`/$IMAGENAME" ]; then
    IMAGENAME="`pwd`/$IMAGENAME"
  fi

  MOUNTPOINT=$2
  if [ -d "`pwd`/$MOUNTPOINT" ]; then
    MOUNTPOINT="`pwd`/$MOUNTPOINT"
  fi

  if [ -f $IMAGENAME ]; then
    echo "Found image file at: $IMAGENAME"
    if [ "`losetup -j $IMAGENAME`" != ""  ]; then
      echo "ERROR: image file seems to already be in use by losetup"
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
  echo "  ./mount-xen-pv-img.sh <IMAGE_NAME> <MOUNT_POINT>"
  echo "Example:"
  echo "  ./mount-xen-pv-img.sh /home/me/my-images/my-image.raw.img /home/me/my-images/moint-point"  
  exit 1
fi

if [ -f /etc/redhat-release ]
then
  LOOPDEV=`sudo losetup -f`
  echo "Using first spare loop back device: $LOOPDEV"

  sudo losetup $LOOPDEV $IMAGENAME

  sudo mount $LOOPDEV $MOUNTPOINT
fi

if [ -f /etc/debian_version ]
then
  LOOPDEV=`sudo kpartx -a -v $IMAGENAME | cut -d ' ' -f 3`

  if [ "$LOOPDEV" == "" ] #No partition table mount as loop device
  then
    LOOPDEV="`sudo losetup -j $IMAGENAME | cut -d ':' -f 1`" # Using -j as losetup truncates on >64 chars
    sudo mount $LOOPDEV $MOUNTPOINT
  else
    sudo mount /dev/mapper/$LOOPDEV $MOUNTPOINT
  fi
fi

echo "Image mounted at: $MOUNTPOINT" 