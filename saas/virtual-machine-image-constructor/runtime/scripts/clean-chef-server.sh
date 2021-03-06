#!/bin/bash

CHEF_CLIENT_IP=$1

RUNTIME_DIR="$(cd $(dirname $0); cd .. ; pwd -P)"
cd $RUNTIME_DIR/chef-repo

# Using run list of node remove uploaded cookbooks from server and workspace
while read -r COOKBOOK; do
  # Server clean up
  knife cookbook delete $COOKBOOK $(echo $CHEF_CLIENT_IP | cut -d'.' -f4).0.0 -y -s https://$(hostname):443
  # Workspace clean up
  rm -r ./cookbooks/$CHEF_CLIENT_IP-$COOKBOOK
done <<< "$(knife node show vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4) -r -s https://$(hostname):443 | grep recipe | cut -d'[' -f2 | cut -d'@' -f1)"

# Finally remove registration of node from server
knife client delete --yes vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4) -s https://$(hostname):443
knife node delete --yes vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4) -s https://$(hostname):443

exit 0
