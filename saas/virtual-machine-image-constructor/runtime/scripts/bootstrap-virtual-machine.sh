#!/bin/bash

BOOTSTRAP_IP=$1
OS=$2

RUNTIME_DIR="$(cd $(dirname $0); cd .. ; pwd -P)"
cd $RUNTIME_DIR/chef-repo

# Bootstrap chef agent to chef server
if [ "$OS" == "windows" ]
then
  knife bootstrap windows winrm $BOOTSTRAP_IP -x Administrator -P 'password' --winrm-authentication-protocol basic --msi-url 'http://10.0.0.1:8080/chef-windows-11.18.6-1.windows.msi' -N vmic-$(echo $BOOTSTRAP_IP | cut -d'.' -f 4) --no-color
  if [ $? -ne 0 ]
  then
    echo "Error bootstrapping windows base image to chef server"
    exit 1
  fi
elif [ "$OS" == "linux" ]
then
  knife bootstrap $BOOTSTRAP_IP -x root -P 'password' -N vmic-$(echo $BOOTSTRAP_IP | cut -d'.' -f 4) --bootstrap-url 'http://10.0.0.1:8080/install.sh' --no-color
  if [ $? -ne 0 ]
  then
    echo "Error bootstrapping linux base image to chef server"
    exit 1
  fi
fi

exit 0
