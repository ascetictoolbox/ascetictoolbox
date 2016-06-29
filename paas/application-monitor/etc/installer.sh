#! /bin/sh

user=$(id -un)
home=$(getent passwd | grep $user | awk -F: '{print $6}')


echo "Terminating running instances"
signal=-TERM
while ps aux | grep "$home/appmon/" | grep -v grep | awk '{print $2}' | xargs kill $signal 2>/dev/null; do
  sleep 5s
  signal=-KILL
done

rm $home/appmon -Rf
mkdir -p $home/appmon
cd $home || exit 1

git clone http://github.com/mariomac/appmon.git

cd appmon/appmonitor || exit 1
screen -dmS appmon ./activator start
