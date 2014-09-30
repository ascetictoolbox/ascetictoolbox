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

# Example script to whip images. Images must be "zeroed" internaly before hand using something like:
#   dd if=/dev/zero of=/zero bs=1M; rm /zero;
# Advisable to also truncate all log files and recreate swap files before zeroing.

# Make sure only root can run our script
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

PWD=`pwd`

#Example directory where you keep all your xen pv base images:
cd /opt/images/base-images/xen-pv
echo "Please edit this script with an appropriate directory!"; exit 1 # Comment out this after editing above line

FILES=`find . -name "*.img"`

for f in $FILES
do
  echo "Compressing image: $f"
  qemu-img convert -O raw $f $f.base
  cp $f.base $f
done

cd $PWD
