#!/bin/bash

################################################################################
# Under Apache 2.0 Licences. See LICENSE.TXT for details                       @
# Author: David García Pérez, Atos Research and Innovation                     @
################################################################################

PWD=`pwd`

## We load application name and deployment id into variables
DEPLOYMENT_ID=`cat /mnt/context/SERVICE_ID`
APPLICATION_NAME=`cat /mnt/context/ovf.xml | grep VirtualSystemCollection | head -n 1 | awk -F'"' '{print $2}'`

## We load the configuration variables
source conf_files/config.cfg

# First we make sure we have APT updated:
bash scripts/configure_repositores.sh

# Starting the HAProxy initial configuration:
bash scripts/haproxy_bootstrap.sh $PWD $DEPLOYMENT_ID $APPLICATION_NAME $APP_MANAGER_URL $OVF_ID $HAPROXY_CONFIG_FILE

./scripts/monitor_deployment.sh $DEPLOYMENT_ID $APPLICATION_NAME $APP_MANAGER_URL $OVF_ID $SERVER_NAME $HAPROXY_CONFIG_FILE
