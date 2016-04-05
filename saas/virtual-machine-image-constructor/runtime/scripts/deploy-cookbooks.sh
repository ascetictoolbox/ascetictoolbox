#!/bin/bash

CHEF_CLIENT_IP=$1
OS=$2

RUNTIME_DIR="$(cd $(dirname $0); cd .. ; pwd -P)"
cd $RUNTIME_DIR/chef-repo

# Force an update of the chef client deploy the cookbooks in its runlist.
NODE_NAME="vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4)"
echo "Searching for $NODE_NAME"

# Make sure the node is registered before we try and invoke chef-client
RETRIES=4 # This should be set to twice: chef-server.rb > chef_solr['commit_interval']
T=1
while true; do
  knife search node "name:$NODE_NAME" -a ipaddress -s https://$(hostname):443 2>&1 | grep ipaddress
  if [ $? -ne 0 ]; then
    echo "Node not registered in server. Trying again ($T/$RETRIES)"
    sleep 1
    T=$((T+1))
    if [ $T -gt $RETRIES ]; then
      echo "Error: node not registered after $RETRIES retries"
      exit 1
    fi
  else
    break
  fi
done

# Execute chef-client
if [ "$OS" == "windows" ]
then
  knife winrm "name:$NODE_NAME" 'chef-client' -x Administrator -P 'password' --winrm-authentication-protocol basic -a ipaddress --no-color -s https://$(hostname):443
  if [ $? -ne 0 ]
  then
    echo "Error invoking chef-client via winrm"
    exit 1
  fi
elif [ "$OS" == "linux" ]
then
  knife ssh "name:$NODE_NAME" 'chef-client' -x root -P 'password' -a ipaddress --no-color -s https://$(hostname):443
  if [ $? -ne 0 ]
  then
    echo "Error invoking chef-client via ssh"
    exit 1
  fi
fi

exit 0
