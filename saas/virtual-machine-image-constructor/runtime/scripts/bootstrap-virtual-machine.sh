#!/bin/bash

BOOTSTRAP_IP=$1
OS=$2

# TODO: Bootstrap chef agent to chef server

if [ "$OS" == "windows" ]
then
  #cd /mnt/cephfs/ascetic/vmic/runtime/chef-repo
  #knife bootstrap windows winrm 10.0.0.18 -x Administrator -P 'password' --winrm-authentication-protocol basic --msi-url 'http://10.0.0.1:8080/chef-windows-11.18.6-1.windows.msi' -N vmic-test --no-node-verify-api-cert --node-ssl-verify-mode none
  exit 0 # remove me
elif [ "$OS" == "linux" ]
then
  exit 0 # remove me
fi

exit 0
