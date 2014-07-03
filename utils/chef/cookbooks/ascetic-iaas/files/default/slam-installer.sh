#! /bin/sh

user=$(id -un)
home=$(getent passwd | grep $user | awk -F: '{print $6}')
mysql_admin=root
config_scripts_dir=$home/slam
echo "Going to setup SLAM into $config_scripts_dir"

echo "Terminating running instances"
signal=-TERM
while ps aux | grep "$config_scripts_dir" | grep -v grep | awk '{print $2}' | xargs kill $signal 2>/dev/null; do
  sleep 5s
  signal=-KILL
done

rm -Rf $config_scripts_dir
mkdir -p $config_scripts_dir 
cd $config_scripts_dir

wget -O - http://sourceforge.net/projects/contra-slaatsoi/files/pax-runner/pax-runner-1.7.1.tar.gz | tar -xz
wget -O - http://sourceforge.net/projects/contra-slaatsoi/files/sla-at-soi-platform/sla-at-soi-ascetic.tar.gz | tar -xz
wget -O - http://sourceforge.net/projects/contra-slaatsoi/files/ascetic-infrastructure-slam/ascetic-infrastructure-slam.tar.gz | tar -xz

chmod +x $config_scripts_dir/config-package.sh
chmod +x $config_scripts_dir/install/*.sh

repl=$(echo $config_scripts_dir | sed 's/\//\\\//g')
for file in config-package.sh install/*.sh; do
 sed 's/\/opt\/ascetic\//'"$repl"'\//g' "$file" > "$file.new"
 sed 's/mysql -p/mysql -u '"$mysql_admin"' -p/' "$file.new" > "$file"
 rm $file.new
done
#sh ./config-package.sh

# Start it
cd $config_scripts_dir/slam/sla-at-soi/
screen -dmS slamanager startup.sh
