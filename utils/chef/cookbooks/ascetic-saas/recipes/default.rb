#
# Cookbook Name:: ascetic-saas
# Recipe:: default
#
# Copyright 2014, Django Armstrong
#
package "sudo" do
  action :install
end

# Chef server, TODO: Start server while booting up
script "install_chef_server" do
  interpreter "bash"
  user "root"
  cwd "/tmp"
  not_if "dpkg -s chef-server"
  code <<-EOH
  wget https://opscode-omnibus-packages.s3.amazonaws.com/ubuntu/12.04/x86_64/chef-server_11.0.12-1.ubuntu.12.04_amd64.deb -O chef-server.deb && dpkg -i chef-server.deb && chef-server-ctl reconfigure
  rm chef-server.deb
EOH
end

# VMIC dependency
package "qemu-utils" do
  action :install
end

# VMIC dependency
package "nbd-client" do
  action :install
end

# VMIC dependency
package "unzip" do
  action :install
end

cookbook_file "/etc/sudoers.d/10-ascetic-saas" do
  source "ascetic-sudoers"
  mode 0640
  owner "root"
  group "root"
end

group "saas_user" do
  action :create
end

user "saas_user" do
  supports :manage_home => true
  password "$6$g9NBu6dC$IqyFU3FwhtP3SJfqmKp99VPUswUhc5tWidoN25/EOF59UIE37YbD8tauAQ0CacdAI7hNwHYePC66B7.jZKCz0/"
  comment "SaaS User"
  home "/home/saas_user"
  shell "/bin/bash"
  gid "saas_user"
end
