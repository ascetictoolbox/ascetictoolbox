#!/bin/bash

BOOTSTRAP_IP=$1
OS=$2

# Bootstrap chef agent to chef server
if [ "$OS" == "windows" ]
then
  cd /mnt/cephfs/ascetic/vmic/runtime/chef-repo
  knife bootstrap windows winrm $BOOTSTRAP_IP -x Administrator -P 'password' --winrm-authentication-protocol basic --msi-url 'http://10.0.0.1:8080/chef-windows-11.18.6-1.windows.msi' -N vmic-test
  if [ $? -ne 0 ]
  then
    echo "Error bootstrapping windows base image to chef server"
    exit 1
  fi
elif [ "$OS" == "linux" ]
then
  cd /mnt/cephfs/ascetic/vmic/runtime/chef-repo
  knife bootstrap $BOOTSTRAP_IP -x root -P 'password' -N vmic-test --bootstrap-url 'http://10.0.0.1:8080/install.sh'
  if [ $? -ne 0 ]
  then
    echo "Error bootstrapping linux base image to chef server"
    exit 1
  fi
fi

exit 0
