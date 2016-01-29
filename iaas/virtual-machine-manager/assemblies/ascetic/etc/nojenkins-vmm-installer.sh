#!/bin/bash

if [[ "$1" == "--debug" || "$2" == "--debug" ]]
then
	export DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=54321"
fi


user=$(id -un)
home=$(getent passwd | grep $user | awk -F: '{print $6}')

echo "Terminating running instances"
signal=-TERM
while ps aux | grep uber-vmmanagercore-0.0.1-SNAPSHOT.jar | grep -v grep | awk '{print $2}' | xargs kill $signal 2>/dev/null; do
  sleep 5s
  signal=-KILL
done

rm -Rf $home/vmmanager
rm -Rf $home/vmmanager

mkdir -p $home/vmmanager

cp /etc/ascetic/em/*.properties $home/vmmanager/

# comment the next block variables if you want to disable kynerix support
if [[ "$1" != "--no-kynx" && "$2" != "--no-kynx" ]]
then
  export KYNERIX_OPTS="-cp kynerix-agent-log4j-1.0-alfa.jar -Dlog4j.configuration=./kynxlog4j.xml -Dkynerix.client.server.endpoint=http://192.168.3.254:8080 -Dkynerix.client.auth.key=KEY_f1c9f736-724b-4ff3-bac7-040ebd768105"
  cp /etc/ascetic/vmm/kyn* $home/vmmanager/
fi

cd $home/vmmanager

echo
echo
echo "Copying from $home/ to $home/vmmanager/..."
cp $home/uber-vmmanagercore-0.0.1-SNAPSHOT.jar $home/vmmanager/

cat > start.sh << EOF
#! /bin/sh
cd $home/vmmanager
echo "Running java with debug options: $DEBUG"
nohup java $DEBUG $KYNERIX_OPTS -jar uber-vmmanagercore-0.0.1-SNAPSHOT.jar &
EOF
chmod 755 start.sh

# Start it
cd $home/vmmanager
./start.sh