#
# Cookbook Name:: demo
# Recipe:: default
#
# Copyright 2014, Django Armstrong
#
cookbook_file '/var/www/index.html' do
  source 'index.html'
  action :delete
end

service 'apache2' do
  action [ :stop, :disable ]
end

package 'apache2' do
  action :purge
end

service 'mysql' do
  action [ :stop, :disable ]
end

package 'mysql-server' do
  action :purge
end
