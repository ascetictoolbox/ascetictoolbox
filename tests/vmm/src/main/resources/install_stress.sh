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
cat /tmp/vms.json | tr '{' '\n' | grep "\"applicationId\":\"$(echo $APP_ID)\"" | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}' | grep '192.' | tee /tmp/vms.txt

for i in `cat /tmp/vms.txt`
do
    ssh -i ${PEM_FILE} -o StrictHostKeyChecking=no root@${i} "mkdir -p /tmp/stress-ng && wget -qO- http://kernel.ubuntu.com/~cking/tarballs/stress-ng/stress-ng-0.03.11.tar.gz | tar xvz -C /tmp/stress-ng && cd /tmp/stress-ng/stress-ng-0.03.11 && make install"
done