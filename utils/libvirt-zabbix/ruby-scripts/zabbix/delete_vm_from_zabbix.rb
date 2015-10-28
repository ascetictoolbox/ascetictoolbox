require '/opt/ruby-scripts/zabbix/zabbix_ascetic.rb'

uuid = ARGV[0]

zabbix_client=ZabbixAscetic.new
zabbix_client.setup
zabbix_client.delete_host(uuid)