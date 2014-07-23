#! /bin/sh

user=$(id -un)
home=$(getent passwd | grep $user | awk -F: '{print $6}')

svn_amdb_root="https://ascetic-dev.cit.tu-berlin.de/svn/trunk/utils/application-manager-database"
svn_conf_root="https://ascetic-dev.cit.tu-berlin.de/svn/trunk/paas/application-manager/src/main/resources/"
mysql_conf="/etc/mysql/debian.cnf"

is_updated_db_scheme() {
  [ -z "$1" ] && exit 1
  tmp=$(mktemp)
  if ! wget "$svn_amdb_root/db/changelog/db.changelog-master.xml" -O "$tmp" --no-check-certificate 1>/dev/null 2>&1; then
    echo "Unable to get DB changelog"
    exit 1
  fi
  
  if [ -e "$1" ] && diff "$tmp" "$1" >/dev/null; then
   rm "$tmp"
   return 1
  else
   mv "$tmp" "$1"
   return 0
  fi
}

mkdir -p $home/amanager
if ! is_updated_db_scheme $home/amanager/db.changelog-master.xml; then
  echo "DB unchanged"
else
  echo "Creating MySQL user and DB"
  sudo mysql --defaults-extra-file="$mysql_conf" << END
CREATE DATABASE IF NOT EXISTS application_manager;
GRANT ALL ON application_manager.* TO 'app-manager'@'%' IDENTIFIED BY 'ascetic-secret';
END

  echo "Using liquibase to update DB"
  tmp=$(mktemp -d)
  svn export --force --non-interactive --trust-server-cert "$svn_amdb_root"/ "$tmp"

  opwd=$(pwd)
  cd "$tmp"

  LIQUIBASE_HOME="$tmp/liquibase" liquibase/liquibase --driver=com.mysql.jdbc.Driver --changeLogFile=db/changelog/db.changelog-master.xml --url="jdbc:mysql://localhost/application_manager" --username="app-manager" --password="ascetic-secret" migrate

  cd "$opwd"
  rm "$tmp" -Rf
fi

sudo mkdir -p /etc/ascetic/paas/application-manager/
sudo chmod 755 /etc/ascetic /etc/ascetic/paas/
sudo chown $user /etc/ascetic/paas/application-manager/

curl -k https://ascetic-dev.cit.tu-berlin.de/svn/trunk/paas/application-manager/src/main/resources/application-manager.properties |
 sed 's/^vm-manager.url=http:\/\/.*:/vm-manager.url=http:\/\/iaas-vm-dev:/g' |
 tee /etc/ascetic/paas/application-manager/application-manager.properties >/dev/null
if ! grep -q 'vm-manager.url=' /etc/ascetic/paas/application-manager/application-manager.properties; then
 echo 'vm-manager.url=http://iaas-vm-dev:34372/vmmanager' >> /etc/ascetic/paas/application-manager/application-manager.properties
fi

curl -k https://ascetic-dev.cit.tu-berlin.de/svn/trunk/paas/application-manager/src/main/resources/applicationContext.xml |
 sed 's/p:username="[^"]*"/p:username="app-manager"/g' | sed 's/p:password="[^"]*"/p:password="ascetic-secret"/g' |
 sed 's/p:url="[^"]*"/p:url="jdbc:mysql:\/\/localhost:3306\/application_manager"/g' |
 sed 's/aplicationManagerDB/applicationManagerDB/g' |
 tee /etc/ascetic/paas/application-manager/applicationContext.xml >/dev/null

curl -k https://ascetic-dev.cit.tu-berlin.de/svn/trunk/paas/application-manager/src/main/resources/META-INF/persistence.mysql.xml |
 sed 's/name="hibernate.connection.url" value="[^"]*"/name="hibernate.connection.url" value="jdbc:mysql:\/\/localhost:3306\/application_manager"/g' |
 sed 's/name="hibernate.connection.username" value="[^"]*"/name="hibernate.connection.username" value="app-manager"/g' |
 sed 's/name="hibernate.connection.password" value="[^"]*"/name="hibernate.connection.password" value="ascetic-secret"/g' |
 sed 's/aplicationManagerDB/applicationManagerDB/g' |
 tee /etc/ascetic/paas/application-manager/persistence.mysql.xml >/dev/null

# TODO: Be more restrictive (chgrp tomcat, chmod o-rwx)
chmod 755 /etc/ascetic/paas/application-manager/
chmod 644 /etc/ascetic/paas/application-manager/*

tmp=$(mktemp)
curl -k 'https://ascetic-jenkins.cit.tu-berlin.de/job/ASCETiC%20Reference%20Architecture/ws/trunk/paas/application-manager/target/application-manager-0.1-SNAPSHOT.war' > "$tmp"
chmod 644 "$tmp"
sudo mv "$tmp" /var/lib/tomcat7/webapps/application-manager.war || rm "$tmp"

exit 0

