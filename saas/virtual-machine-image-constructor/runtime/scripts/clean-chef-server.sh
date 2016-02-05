#!/bin/bash

CHEF_CLIENT_IP=$1

RUNTIME_DIR="$(cd $(dirname $0); cd .. ; pwd -P)"
cd $RUNTIME_DIR/chef-repo

# TODO: Using run list of node remove uploaded cookbooks from server and workspace

# Server cleanup
knife cookbook bulk delete '.*' -y # FIXME: This is a hack for the moment
# Workspace clean up
find ./cookbooks -maxdepth 1 ! -path ./cookbooks -type d \( ! -name ".svn" \) -exec rm -rf {} \;

# Finally remove registration of node from server
knife client delete --yes vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4)
knife node delete --yes vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4)

exit 0
