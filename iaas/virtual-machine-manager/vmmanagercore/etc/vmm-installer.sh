#! /bin/sh

user=$(id -un)
home=$(getent passwd | grep $user | awk -F: '{print $6}')

echo "Terminating running instances"
signal=-TERM
while ps aux | grep uber-vmmanagercore-0.0.1-SNAPSHOT.jar | grep -v grep | awk '{print $2}' | xargs kill $signal 2>/dev/null; do
  sleep 5s
  signal=-KILL
done

rm -Rf $home/vmmanager/*.jar
rm -Rf $home/vmmanager/db
cp /etc/ascetic/em/*.properties $home/vmmanager/

mkdir -p $home/vmmanager
cd $home/vmmanager

echo
echo "Downloading from Jenkins..."
curl -k 'https://ascetic-jenkins.cit.tu-berlin.de/job/ASCETiC%20Reference%20Architecture/ws/trunk/iaas/virtual-machine-manager/vmmanagercore/target/uber-vmmanagercore-0.0.1-SNAPSHOT.jar' > uber-vmmanagercore-0.0.1-SNAPSHOT.jar || exit 1

cat > start.sh << EOF
#! /bin/sh
cd $home/vmmanager
export config=/home/ubuntu/vmmanager/vmmconfig.properties
exec java -cp uber-vmmanagercore-0.0.1-SNAPSHOT.jar es.bsc.vmmanagercore.rest.Main -Dconfig.file=/home/ubuntu/vmmanager/vmmconfig.properties
EOF
chmod 755 start.sh

# Start it
screen -dmS vmmanager $home/vmmanager/start.sh
