#!/bin/bash

# FIXME: Current version only supports URLs, should support URIs

CHEF_CLIENT_IP=$1
COOKBOOK_URI=$2

RUNTIME_DIR="$(cd $(dirname $0); cd .. ; pwd -P)"
cd $RUNTIME_DIR/chef-repo
COOKBOOKS="./cookbooks/"

COOKBOOK="$(echo "${COOKBOOK_URI##*/}")"
COOKBOOK_NAME="$(echo $COOKBOOK | cut -d'.' -f1)"

# Download cookbook
wget $COOKBOOK_URI -P $COOKBOOKS

# Extract cookbook to workspace
tar zxvf $COOKBOOKS$COOKBOOK -C $COOKBOOKS
rm $COOKBOOKS$COOKBOOK

# TODO: Add default attributes from input arguments skipping first 2
for i in ${@:3}
do
  echo $i
done

# Upload the cookbook
knife upload $COOKBOOKS$COOKBOOK_NAME

# Add the cookbook to the VMs (node) runlist using its IP
knife node run_list add vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4) recipe[$COOKBOOK_NAME]

exit 0
