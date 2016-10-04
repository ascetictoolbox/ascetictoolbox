#!/bin/bash

################################################################################
# Under Apache 2.0 Licences. See LICENSE.TXT for details                       @
# Author: David García Pérez, Atos Research and Innovation                     @
################################################################################

################################################################################
# Subscribes to the AMQP for that deployment and monitors it                   #
################################################################################

DEPLOYMENT_ID="1152"
APPLICATION_NAME="davidgpTestApp"
APP_MANAGER_URL="http://localhost:8080/application-manager"
OVF_ID="jboss"
SERVER_NAME="na_app_server"
HAPROXY_CONFIG_FILE="/etc/haproxy/haproxy.cfg"

get_id() {
  ID=`echo $5 | awk -F'.' '{print $6}'`
}

regex_vm_deployed="^Message received for destination: APPLICATION\.${APPLICATION_NAME}\.DEPLOYMENT\.${DEPLOYMENT_ID}\.VM\.[0-9]+\.DEPLOYED$"
regex_vm_deleted="^Message received for destination: APPLICATION\.${APPLICATION_NAME}\.DEPLOYMENT\.${DEPLOYMENT_ID}\.VM\.[0-9]+\.DELETED$"

while read -r line
do
    if [[ $line =~ $regex_vm_deployed ]]
    then
      ## We need to parse the ID of the VM
      echo $line
      get_id $line
      echo "ADDED: $ID"
      ## We add the VM to the HAProxy configuration
      vms_string=`curl ${APP_MANAGER_URL}/applications/${APPLICATION_NAME}/deployments/${DEPLOYMENT_ID}/vms | tail -n +2 | awk '{gsub("xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\"", "");print}' | xmlstarlet sel -T -t -m /collection/items/vm -s A:N:- "id" -v "concat(id,'|',ovf-id,'|',ip,' ')"`

      vm_array=($vms_string)

       for i in ${vm_array[@]}; do
         vm=(${i//|/ })

         if [ "${vm[1]}" = "$OVF_ID" ] && [ "${vm[0]}" = "$ID" ]
         then
           echo "        ${SERVER_NAME}_${vm[0]} ${vm[2]}:80 check"  >> ${HAPROXY_CONFIG_FILE}
        fi
       done
    elif [[ $line =~ $regex_vm_deleted ]]
    then
      get_id $line
      echo "DELETED: $ID"
      sed -i.bak "/${SERVER_NAME}_${ID}/d" ${HAPROXY_CONFIG_FILE}
    fi
done < ../tests/amq-messages.txt
