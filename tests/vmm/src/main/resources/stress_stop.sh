#!/bin/bash
if [ -z "$1" ]
then
  echo "No argument supplied. Specify the applicationID, please."
  exit 1  
fi

if [ -z "$2" ]
then
  echo "No argument supplied. Specify the PEM ID, please."
  exit 1  
fi

APP_ID=$1
PEM_FILE=$2

wget http://192.168.3.223:34372/api/v1/vms -O /tmp/vms.json
cat /tmp/vms.json | tr '{' '\n' | grep "\"applicationId\":\"$(echo $APP_ID)\"" | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}' | grep 192. > /tmp/vms.txt

for i in `cat /tmp/vms.txt`
do
    echo "Stopping all cpus at ${i}."
    ssh -i ${PEM_FILE} -o StrictHostKeyChecking=no root@${i} "kill -9 \$(ps -ef | grep stress-ng | grep -v grep | awk '{print \$2}')"
done