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

#Script for growing a raw images file in GB increments

SIZE=null #Size in GB of the image
IMAGENAME=null #Name of image

if [[ $# == 2 ]]; then
  SIZE=$1 #Size in GB of the image
  IMAGENAME=$2 #Name of image

  if [ -f "`pwd`/$IMAGENAME" ]; then
    IMAGENAME="`pwd`/$IMAGENAME"
  fi

else
  echo "ERROR: NOT ENOUGH ARGUMENTS!"
  echo "Usage:"
  echo "  ./grow-xen-pv-img.sh <SIZE_IN_GB> <IMAGE_NAME>"
  echo "Example:"
  echo "  ./grow-xen-pv-img.sh 10 /home/me/my-images/file.img"  
  exit 1
fi

FILESIZE=$(stat -c%s "$IMAGENAME")
SIZEINBYTES="$(echo "$SIZE*1000*1024*1024" | bc)"

if [ "$FILESIZE" -ge "$SIZEINBYTES" ]; then
  echo "ERORR: NEW SIZE LESS THAN OR EQUAL TO OLD SIZE"
  exit 1
fi

if [ -f $IMAGENAME.bck ]; then
  echo "ERROR: BACKUP OF IMAGE ALREADY EXISTS, MOVE IT TO A SAFE LOCATION OR DELETE"
  echo "Note: Delete the backup only if you are sure that the current image is not broken"
  exit 1
fi

echo "Creating backup of image..." 
cp --sparse=always $IMAGENAME $IMAGENAME.bck
echo "Created backup of image called $IMAGENAME.bck"
echo ""

echo "Increasing size of image..."
dd if=/dev/zero of=$IMAGENAME bs=1M conv=notrunc count=1 seek=$[999+1000*(($SIZE-1))]
echo ""

LOOPDEV=`sudo losetup -f`
echo "Using first spare loop back device: $LOOPDEV"
echo ""
sudo losetup $LOOPDEV $IMAGENAME #Use an unused loop device
sudo e2fsck -f $LOOPDEV
sudo resize2fs $LOOPDEV
sudo e2fsck -f $LOOPDEV
sudo losetup -d $LOOPDEV

echo ""
echo "Image growing complete!" 
