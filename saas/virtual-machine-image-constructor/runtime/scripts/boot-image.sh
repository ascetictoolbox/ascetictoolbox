#!/bin/bash

BASE_IMAGE_TYPE="$1"

# TODO: Execute a script 1) create a snapshot (qmeu-img create -f qcow2
# -b ...) of the base image 2) boot the VM (virsh create ...) using the
# snapshot via an altered xml def returning its IP address.

if [ "$BASE_IMAGE_TYPE" == "linux" ]
then
  echo "1.1.1.1"
elif [ "$BASE_IMAGE_TYPE" == "windows" ]
then 
  echo "2.2.2.2"
fi

exit 0