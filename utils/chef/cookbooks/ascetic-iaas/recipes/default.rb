#
# Cookbook Name:: ascetic-iaas
# Recipe:: default
#
# Copyright 2014, Michael Kammer
#
package "subversion" do
  action :install
end

package "screen" do
  action :install
end

package "python-pip" do
  action :install
end

package "gcc" do
  action :install
end

package "python-dev" do
  action :install
end

package "libxml2-dev" do
  action :install
end

package "libxslt1-dev" do
  action :install
end

script "run_pip" do
  interpreter "bash"
  user "root"
  group "root"
  cwd "/"
  not_if "test -d /usr/local/lib/python2.7/dist-packages/glanceclient"
  code <<-EOH
pip install python-glanceclient
  EOH
end

apt_repo "zabbix" do
  key_id "79EA5ED4"
  key_package "zabbix-release"
  url "http://repo.zabbix.com/zabbix/2.2/ubuntu"
  distribution "precise"
end

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

package "zabbix-server-mysql" do
  action :install
end

package "zabbix-frontend-php" do
  action :install
end

cookbook_file "/home/ubuntu/slam-installer.sh" do
  source "slam-installer.sh"
  mode 0755
  owner "ubuntu"
  group "ubuntu"
end

cookbook_file "/home/ubuntu/vmm-installer.sh" do
  source "vmm-installer.sh"
  mode 0755
  owner "ubuntu"
  group "ubuntu"
end

script "install_slam" do
  interpreter "bash"
  user "ubuntu"
  group "ubuntu"
  cwd "/home/ubuntu"
  not_if "test -d /home/ubuntu/slam"
  code <<-EOH
sh slam-installer.sh
  EOH
end

script "install_vmmanager" do
  interpreter "bash"
  user "ubuntu"
  group "ubuntu"
  cwd "/home/ubuntu"
  not_if "test -d /home/ubuntu/vmmanager"
  code <<-EOH
sh vmm-installer.sh
  EOH
end

script "install_vmmanger_frontend" do
  interpreter "bash"
  user "root"
  group "root"
  cwd "/var/www"
  not_if "test -d /var/www/vmmanager-frontend"
  code <<-EOH
svn export "https://ascetic-dev.cit.tu-berlin.de/svn/trunk/iaas/virtual-machine-manager/vmmanager-frontend/"
  EOH
end

