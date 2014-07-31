#
# Cookbook Name:: ascetic-saas
# Recipe:: default
#
# Copyright 2014, Django Armstrong
#
package "qemu-utils" do
  action :install
end

package "sudo" do
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
