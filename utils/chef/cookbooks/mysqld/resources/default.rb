#
# Cookbook Name:: mysqld
# Resource:: default
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

actions        :create
default_action :create

attribute :name,         kind_of: String, name_attribute: true
attribute :packages,     kind_of: Array,  default: node['mysqld']['packages']
attribute :my_cnf,       kind_of: Hash,   default: {}
attribute :my_cnf_path,  kind_of: String, default: node['mysqld']['my.cnf_path']
attribute :service_name, kind_of: String, default: node['mysqld']['service_name']
attribute :includedir,   kind_of: [TrueClass, FalseClass], default: node['mysqld']['includedir']
