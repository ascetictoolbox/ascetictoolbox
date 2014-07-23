#! /bin/sh

user=$(id -un)
home=$(getent passwd | grep $user | awk -F: '{print $6}')

svn_root="https://ascetic-dev.cit.tu-berlin.de/svn/trunk/paas/application-monitor"

echo "Terminating running instances"
signal=-TERM
while ps aux | grep "$home/amonitor/" | grep -v grep | awk '{print $2}' | xargs kill $signal 2>/dev/null; do
  sleep 5s
  signal=-KILL
done

rm $home/amonitor -Rf
mkdir -p $home/amonitor
cd $home || exit 1
svn export --force --non-interactive --trust-server-cert https://ascetic-dev.cit.tu-berlin.de/svn/trunk/paas/application-monitor/ amonitor || exit 1

cd amonitor || exit 1
screen -dmS amonitor ./activator start
