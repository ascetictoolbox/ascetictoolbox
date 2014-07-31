#
# Cookbook Name:: apt
# Recipe:: default
#
# Copyright 2014, Django Armstrong
#
execute "apt-get update" do
  command "apt-get update"
end
