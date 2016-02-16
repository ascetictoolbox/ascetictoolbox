#!/bin/bash

# FIXME: Current version only supports URLs, should support URIs

CHEF_CLIENT_IP=$1
COOKBOOK_URI=$2

RUNTIME_DIR="$(cd $(dirname $0); cd .. ; pwd -P)"
cd $RUNTIME_DIR/chef-repo
COOKBOOKS="./cookbooks/"

COOKBOOK="$CHEF_CLIENT_IP-$(echo "${COOKBOOK_URI##*/}")"
COOKBOOK_NAME="$(echo $COOKBOOK | cut -d'.' -f1-4)"
COOKBOOK_ORIGINAL_NAME="$(echo $COOKBOOK_NAME | cut -d'-' -f2-)"

# Download cookbook
wget -q $COOKBOOK_URI -O $COOKBOOKS$COOKBOOK

# Extract cookbook to workspace
mkdir $COOKBOOKS$COOKBOOK_NAME
tar zxvf $COOKBOOKS$COOKBOOK -C $COOKBOOKS$COOKBOOK_NAME --strip-components=1
rm -r $COOKBOOKS$COOKBOOK

# Change the version number to be VM specific
if [ -f $COOKBOOKS$COOKBOOK_NAME/metadata.rb ]
then 
  sed -i -e "s/version\s\+['\"].\{2,10\}['\"]/version '$(echo $CHEF_CLIENT_IP | cut -d'.' -f4).0.0'/1" $COOKBOOKS$COOKBOOK_NAME/metadata.rb
fi

if [ -f $COOKBOOKS$COOKBOOK_NAME/metadata.json ]
then
  jq -c '. + { "version": "'$(echo $CHEF_CLIENT_IP | cut -d'.' -f4)'.0.0" }' $COOKBOOKS$COOKBOOK_NAME/metadata.json > $COOKBOOKS$COOKBOOK_NAME/metadata.json.tmp
  mv $COOKBOOKS$COOKBOOK_NAME/metadata.json.tmp $COOKBOOKS$COOKBOOK_NAME/metadata.json
fi

# Add base64 encoded default attributes from input arguments skipping first 2
for i in ${@:3}
do
  ATTRIBUTE="$(echo $i | base64 --decode)"
  echo "Decoded attribute: $ATTRIBUTE"
  echo $ATTRIBUTE >> $COOKBOOKS$COOKBOOK_NAME/attributes/default.rb
done

# Upload the cookbook
knife upload $COOKBOOKS$COOKBOOK_NAME

# Add the cookbook to the VMs (node) runlist using its IP
knife node run_list add vmic-$(echo $CHEF_CLIENT_IP | cut -d'.' -f 4) recipe[$COOKBOOK_ORIGINAL_NAME@$(echo $CHEF_CLIENT_IP | cut -d'.' -f4).0.0]

exit 0
