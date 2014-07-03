#
# Cookbook Name:: ascetic-iaas
# Recipe:: default
#
# Copyright 2014, Michael Kammer
#
apt_repo "zabbix" do
  key_id "79EA5ED4"
  key_package "zabbix-release"
  url "http://repo.zabbix.com/zabbix/2.2/ubuntu"
  distribution "precise"
end

package "openjdk-7-jre" do
  action :install
end

package "mysql-server" do
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

