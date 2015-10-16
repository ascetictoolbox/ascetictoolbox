#!/bin/bash

CHEF_CLIENT_IP=$1

cd /mnt/cephfs/ascetic/vmic/runtime/chef-repo

# TODO: Using run list of node remove uploaded cookbooks from server and workspace

# Finally remove registration of node from server
knife client delete --yes vmic-test
knife node delete --yes vmic-test

exit 0
