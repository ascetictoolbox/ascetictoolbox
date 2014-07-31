#
# Cookbook Name:: ascetic-paas
# Recipe:: default
#
# Copyright 2014, Michael Kammer
#

apt_repo "mongodb" do
  key_id "7F0CEB10"
  key_url "http://docs.mongodb.org/10gen-gpg-key.asc"
  url "http://downloads-distro.mongodb.org/repo/ubuntu-upstart"
  distribution "dist"
  components ["10gen"]
end

package "mongodb-org" do
  action :install
end

#apt_repo "gluster" do
#  key_id "21C74DF2"
#  key_package "gluster"
#  url "http://download.gluster.org/pub/gluster/glusterfs/3.5/3.5.1/Debian/apt"
#  distribution "wheezy"
#end

package "screen" do
  action :install
end

package "openjdk-7-jre" do
  action :install
end

package "mysql-server" do
  action :install
end

package "tomcat7" do
  action :install
  # TODO: Set port to 80 in config, enable authbind
end

package "ntpdate" do
  action :install
end

package "ntp" do
  action :install
end

package "subversion" do
  action :install
end

package "sudo" do
  action :install
end

cookbook_file "/etc/sudoers.d/10-ascetic-paas" do
  source "ascetic-sudoers"
  mode 0640
  owner "root"
  group "root"
end

user "slam" do
  supports :manage_home => true
  comment "PaaS SLA Manager"
  home "/home/slam"
  shell "/bin/bash"
  gid "nogroup"
end

cookbook_file "/home/slam/slam-installer.sh" do
  source "slam-installer.sh"
  mode 0755
  owner "slam"
  group "nogroup"
end

script "install_slam" do
  interpreter "bash"
  user "slam"
  group "nogroup"
  cwd "/home/slam"
  not_if "test -d /home/slam/slam"
  code <<-EOH
sh slam-installer.sh
  EOH
end

cookbook_file "/home/ubuntu/am-installer.sh" do
  source "am-installer.sh"
  mode 0755
  owner "ubuntu"
  group "ubuntu"
end

cookbook_file "/home/ubuntu/pr-installer.sh" do
  source "pr-installer.sh"
  mode 0755
  owner "ubuntu"
  group "ubuntu"
end


cookbook_file "/home/ubuntu/amonitor-installer.sh" do
  source "amonitor-installer.sh"
  mode 0755
  owner "ubuntu"
  group "ubuntu"
end

script "install_amanager" do
  interpreter "bash"
  user "ubuntu"
  group "ubuntu"
  cwd "/home/ubuntu"
  not_if "test -d /var/lib/tomcat7/webapps/application-manager"
  code <<-EOH
sh am-installer.sh
  EOH
end

script "install_pregistry" do
  interpreter "bash"
  user "ubuntu"
  group "ubuntu"
  cwd "/home/ubuntu"
  code <<-EOH
sh pr-installer.sh
  EOH
end

script "install_amonitor" do
  interpreter "bash"
  user "ubuntu"
  group "ubuntu"
  cwd "/home/ubuntu"
  not_if "test -d /home/ubuntu/amonitor"
  code <<-EOH
sh amonitor-installer.sh
  EOH
end

