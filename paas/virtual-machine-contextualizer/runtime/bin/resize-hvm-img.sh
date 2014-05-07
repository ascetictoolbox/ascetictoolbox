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

#Script for resizing a hvm image files

IMG=null #Name of image
SIZE=null #Size of image

echo "############"
echo "# WARNING: #"
echo "############"
echo ""
echo "Before using this command to shrink a disk image, you MUST use file system and partitioning tools inside the VM to reduce allocated file systems and partition sizes accordingly. Failure to do so will result in data loss!"
echo "After using this command to grow a disk image, you must use file system and partitioning tools inside the VM to actually begin using the new space on the device."
echo ""


if [ $# -eq 2 ]; then

  IMG=$1
  if [ -f "`pwd`/$IMG" ]; then
    IMG="`pwd`/$IMG"
  fi
  
  SIZE=$2

  if [ -f $IMG ]; then
    echo "Found image file at: $IMG"
  else
    echo "ERROR: image does not exist"
    exit 1
  fi
else
  echo "ERROR: NOT ENOUGH ARGUMENTS"
  echo "Usage:"
  echo "  ./resize-hvm-img.sh <IMAGE_LOCATION> [+/-]<SIZE>[units: kilobyte=K, megabyte=M, gigabyte=G]"
  echo "Example:"
  echo "  ./resize-hvm-img.sh /home/me/my-images/my-hvm.img 10G"
  exit 1
fi

echo "Image details:"
qemu-img info $IMG
echo ""
echo "Resizing image..."
qemu-img resize $IMG $SIZE
echo ""
echo "New image details:"
qemu-img info $IMG
