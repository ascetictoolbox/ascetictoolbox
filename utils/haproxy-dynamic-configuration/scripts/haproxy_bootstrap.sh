#!/bin/bash

################################################################################
# Under Apache 2.0 Licences. See LICENSE.TXT for details                       @
# Author: David García Pérez, Atos Research and Innovation                     @
################################################################################

################################################################################
## This creates the initial configuration for HAProxy.                        ##
## This will probably work only NA UC of ASCETiC...                           ##
## Although it is as easy as modifying a bit the configuration files          ##
## The first parameter is the directory from which the bootstrap.sh scripts   ##
## has been lanched                                                           ##
################################################################################

PWD=$1
DEPLOYMENT_ID=$2
APPLICATION_NAME=$3
APP_MANAGER_URL=$4
OVF_ID=$5

## Stopping the service
service haproxy stop

## Backup of old HAproxy configuraiton:
cp /etc/haproxy/haproxy.cfg /etc/haproxy/haproxy.cfg.old

## We create the initial configuration structure:
cp $PWD/conf_files/haproxy.cfg /etc/haproxy/haproxy.cfg

## We find our the number of interested instances
vms_string=`curl ${APP_MANAGER_URL}/applications/${APPLICATION_NAME}/deployments/${DEPLOYMENT_ID}/vms | tail -n +2 | awk '{gsub("xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\"", "");print}' | xmlstarlet sel -T -t -m /collection/items/vm -s A:N:- "id" -v "concat(id,'|',ovf-id,'|',ip,' ')"`

vm_array=($vms_string)

for i in ${vm_array[@]}; do
  vm=(${i//|/ })

  if [ "${vm[1]}" = "$OVF_ID" ]
  then
    echo "        na_app_server_${vm[0]} ${vm[2]}:80 check" >> /etc/haproxy/haproxy.cfg
  fi
done
