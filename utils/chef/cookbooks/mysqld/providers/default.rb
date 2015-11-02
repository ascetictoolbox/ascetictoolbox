#
# Cookbook Name:: mysqld
# Provider:: default
#
# Copyright 2013, Chris Aumann
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

action :create do
  # Install required packages
  Array(new_resource.packages).each { |pkg| package pkg }

  # deep_merge configuration with default
  config = Chef::Mixin::DeepMerge.deep_merge!(node['mysqld']['my.cnf'], new_resource.my_cnf)

  # Generate my.cnf from attributes
  my_cnf = ''
  config.each do |category, conf|
    my_cnf << "[#{category}]\n"
    conf.each do |key, value|
      next unless value
      my_cnf << "#{key} = #{value}\n"
    end
    my_cnf << "\n"
  end

  # Add includedir on debian/ubuntu
  my_cnf << "!includedir /etc/mysql/conf.d/\n" if new_resource.includedir

  r = template 'my.cnf' do
    path      new_resource.my_cnf_path
    mode      00644
    cookbook  'mysqld'
    source    'my.cnf.erb'
    variables config: my_cnf
  end
  new_resource.updated_by_last_action(true) if r.updated_by_last_action?

  service new_resource.service_name do
    subscribes :restart, 'template[my.cnf]'
    action     [:enable, :start]
    not_if     { new_resource.service_name.empty? }
  end
end
