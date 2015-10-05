#!/bin/bash

CHEF_CLIENT_IP=$1
COOKBOOK_URI=$2

# TODO: Extract cookbook to workspace

# TODO: Add default attributes from input arguments skipping first 2
for i in ${@:3}
do
  echo $i
done

# TODO: Add the cookbook to the VMs (node) runlist using its IP

exit 0