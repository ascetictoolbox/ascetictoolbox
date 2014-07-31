#
# Cookbook Name:: demo
# Recipe:: default
#
# Copyright 2014, Django Armstrong
#
include_recipe "apt"

package 'apache2' do
  action :install
end

service 'apache2' do
  action [ :enable, :start ]
end

cookbook_file '/var/www/index.html' do
  source 'index.html'
  mode '0644'
end
