## Installation

This scripts need to have ruby installed. To do so execute:

  api-get install ruby2.0 ruby2.0-dev libxml2-dev libxslt-dev libvirt-dev zabbix-sender

Now we need some extra gems to be installed:

  gem2.0 install ffi -v '1.9.10'
  gem2.0 install nokogiri -v '1.4.7' -- --with-cflags=\"-Wformat-nonliteral -Wno-format-security\"
  gem2.0 install ruby-libvirt
  gem2.0 install zabbixapi -v 2.2.2

It is necessary to install this script in all compute nodes of OpenStack. 

Note: This instructions where written for Ubuntu LTS 14.04 distribution, where ruby2 it is executed under the command ruby2.0. The script was tested with ruby2.0, although I suspect it also works with ruby1.9.x but it was never tested.

## Configuration

In the file: collector.rb (inside the folder ruby-scripts) edit the following variables:

  zabbix_ip_address="192.168.252.40"
  zabbix_username="Admin"
  zabbix_password="zabbix"
  zabbix_hostgroup="vms"
  zabbix_template="Template.Virt.Libvirt"
  zabbix_client_create_host=false

The variables: zabbix_ip_address, zabbix_username, and zabbix_password are self explanatory. 

zabbix_hostgroup it is the host group where the ASCETiC VMs are assigned to Zabbix.

The script will create its own template and items for the Libvirt metrics, by default under the name: Template.Virt.Libvirt, you can change it whatever you like.

If you set the zabbix_client_create_host to true, the script will create the host if does not already exits in the Zabbix DB, since the VMM is creating and deleting those entries in Zabbix, leave it as "false" so the script does not enters in conflict with it. 

# Execution

Just make sure that this process is always running in all compute nodes of OpenStack:

   nohup ruby2.0 ./collector.rb

