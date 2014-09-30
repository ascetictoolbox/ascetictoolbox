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

#TODO: Skip the "sleep 1" on any timeout

#Create dirs we need
if [ ! -d /tmp/recontext ]; then
	mkdir /tmp/recontext
fi

if [ ! -d /tmp/test-mount ]; then
	mkdir /tmp/test-mount
fi

#See which device we'll be using
DEVICE=""
while true; do
	if [ -e /dev/sr1 ]; then
		DEVICE="/dev/sr1"
		break
	elif [ -e /dev/hdd ] ; then
		DEVICE="/dev/hdd"
		break
	elif [ -e /dev/xvdd ] ; then
		DEVICE="/dev/xvdd"
		break
	else
		echo "ERROR: Neither xvdd, sr1 nor hdd device exist :-( Going to retry soon..."
		sleep 1
	fi
done
echo "INFO: Device found at: $DEVICE"

#Try to mount for the first time
echo "REALMOUNT INIT: Mounting $DEVICE for the first time"
if [ "`mount | grep -c /tmp/recontext`" != "1" ]
then
	echo "REALMOUNT INIT: /tmp/recontext not mounted, trying to mount"

	timeout -s SIGKILL 1 mount -o context=system_u:object_r:removable_t,sync $DEVICE /tmp/recontext
	if [ "$?" == "0" ]; then
		echo "REALMOUNT INIT: /tmp/recontext is now mounted from $DEVICE"
	else
		echo "REALMOUNT INIT: Failed to mount /tmp/recontext from $DEVICE"
		exit 1
	fi
else
	echo "REALMOUNT INIT: /tmp/recontext already mounted" #This should not happen
fi

while true; do

	#Test to see if we want to mount
	timeout -s SIGKILL 1 mount -o context=system_u:object_r:removable_t,sync $DEVICE /tmp/test-mount
	if [ "$?" == "0" ]; then
		unset type
		unset version
		source /tmp/test-mount/.metadata
		if [ "$type" == "recontext" ]; then #Make sure this is the ISO we are looking for
			echo "TESTMOUNT: Found recontext ISO image, /tmp/test-mount/ is mounted and metadata file has type=recontext accessible from $DEVICE, going to remount /tmp/recontext"
			timeout -s SIGKILL 1 umount -f /tmp/test-mount
			if [ "$?" == "0" ]; then
				echo "TESTMOUNT: /tmp/test-mount is now umounted from $DEVICE"
			else
				echo "ERROR TESTMOUNT: Failed to umount /tmp/test-mount from $DEVICE"
				exit 1
			fi
			
			#Remount /tmp/recontext
			echo "REALMOUNT: Mounting /tmp/recontext"
			if [ ! -f /tmp/recontext/.metadata ]; then #Only remount if the data dir doesn't exist
				#Mount the device back to /tmp/recontext
				timeout -s SIGKILL 1 mount -o context=system_u:object_r:removable_t,sync $DEVICE /tmp/recontext
				if [ "$?" == "0" ]; then
					echo "REALMOUNT: /tmp/recontext is now mounted from $DEVICE"
				else
					echo "ERROR REALMOUNT: Failed to mount /tmp/recontext from $DEVICE"
					exit 1
				fi
			else
				#TODO: detect here if the mount point truely exists
				echo "REALMOUNT: Nothing to do, /tmp/recontext/.metadata file accessible"
			fi
		else
			if [ -f /tmp/test-mount/.metadata ]; then
				echo "TESTMOUNT: Incorrect ISO image, /tmp/test-mount/ is mounted and found /tmp/test-mount/.metatdata but with type!=recontext, contents are:"
				cat /tmp/test-mount/.metadata
			else 
				echo "TESTMOUNT: Incorrect ISO image, /tmp/test-mount/ is mounted but /tmp/test-mount/.metadata could not be found"
			fi
			timeout -s SIGKILL 1 umount -f /tmp/test-mount
			if [ "$?" == "0" ]; then
				echo "TESTMOUNT: /tmp/test-mount is now umounted from $DEVICE"
			else
				echo "ERROR TESTMOUNT: Failed to umount /tmp/test-mount from $DEVICE"
				exit 1
			fi
		fi
	else
		echo "TESTMOUNT: Failed to mount /tmp/test-mount from $DEVICE"
		#Umount /tmp/recontext
		if [ "`mount | grep -c /tmp/recontext`" != "1" ]; then
			echo "REALMOUNT: Does not exist, not un-mounting /tmp/recontext"
		else
			echo "REALMOUNT: Un-mounting /tmp/recontext"
			timeout -s SIGKILL 1 umount -f /tmp/recontext
			if [ "$?" == "0" ]; then
				echo "REALMOUNT: /tmp/recontext is now un-mounted from $DEVICE"
			else
				echo "WARN REALMOUNT: Failed to umount /tmp/recontext from $DEVICE" #If this happens we are in for bad times....
			fi
		fi
	fi
	sleep 1
done
