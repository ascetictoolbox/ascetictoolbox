#! /bin/sh

user=$(id -un)
home=$(getent passwd | grep $user | awk -F: '{print $6}')

svn_root="https://ascetic-dev.cit.tu-berlin.de/svn/trunk/utils/application-manager-database"
mysql_conf="/etc/mysql/debian.cnf"

is_updated_db_scheme() {
  [ -z "$1" ] && exit 1
  tmp=$(mktemp)
  if ! wget "$svn_root/db/changelog/db.changelog-master.xml" -O "$tmp" --no-check-certificate 1>/dev/null 2>&1; then
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
  exit 0
fi

echo "Creating MySQL user and DB"
sudo mysql --defaults-extra-file="$mysql_conf" << END
CREATE DATABASE IF NOT EXISTS application_manager;
GRANT ALL ON application_manager.* TO 'app-manager'@'%' IDENTIFIED BY 'ascetic-secret';
END

echo "Using liquibase to update DB"
tmp=$(mktemp -d)
svn export --force --non-interactive --trust-server-cert "$svn_root"/ "$tmp"

opwd=$(pwd)
cd "$tmp"

LIQUIBASE_HOME="$tmp/liquibase" liquibase/liquibase --driver=com.mysql.jdbc.Driver --changeLogFile=db/changelog/db.changelog-master.xml --url="jdbc:mysql://localhost/application_manager" --username="app-manager" --password="ascetic-secret" migrate

cd "$opwd"
rm "$tmp" -Rf

exit 0

