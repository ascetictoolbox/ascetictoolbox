#
# Cookbook Name:: ascetic-paas
# Recipe:: default
#
# Copyright 2014, Michael Kammer
#

#apt_repo "gluster" do
#  key_id "21C74DF2"
#  key_package "gluster"
#  url "http://download.gluster.org/pub/gluster/glusterfs/3.5/3.5.1/Debian/apt"
#  distribution "wheezy"
#end

package "openjdk-7-jre" do
  action :install
end

package "mysql-server" do
  action :install
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

script "install_amanager" do
  interpreter "bash"
  user "ubuntu"
  group "ubuntu"
  cwd "/home/ubuntu"
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
