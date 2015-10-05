#!/bin/bash

CHEF_CLIENT_IP=$1

# TODO: Script to 1) Shutdown the VM 2) Create a new unique image from
# the snapshot (qemu-img convert -p -f qcow2 -O raw ...) and return its
# URI for inclusion in OVF

exit 0