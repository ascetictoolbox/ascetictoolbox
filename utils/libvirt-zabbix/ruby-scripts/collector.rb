# Copyright 2015 ATOS SPAIN S.A.
#
# Licensed under the Apache License, Version 2.0 (the License);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
# david.garciaperez@atos.net

require './libvirt/libvirt_metrics_collector.rb'
require './zabbix/zabbix_ascetic.rb'

# Configuration
zabbix_ip_address="192.168.252.40"
zabbix_username="Admin"
zabbix_password="zabbix"
zabbix_hostgroup="vms"
zabbix_template="Template.Virt.Libvirt"
zabbix_client_create_host=false

sleep_time=10

# We configure the Zabbix Client
zabbix_client=ZabbixAscetic.new(zabbix_ip_address, zabbix_username, zabbix_password)
zabbix_client.setup(zabbix_hostgroup, zabbix_template, sleep_time)

# We setup Libvirt connector
metrics_collector = LibvirtMetricsCollector.new
host_cpu_cores = metrics_collector.get_number_cores_host

# We determine the name of the host
hostname=`hostname`

#We start the bucle of measurments for CPU
vms=Hash.new

uuids_old=Hash.new

while true 
 
  vms=metrics_collector.get_metrics(vms, host_cpu_cores, uuids_old)
  uuids_new=Hash.new

  vms.each do |uuid, metrics|

    unless uuids_old.has_key?(uuid)
      # We register the VM to the template if does not exits in Zabbix
      zabbix_client.add_host_to_template(zabbix_client_create_host, uuid)
      # We need to know the ids of the disk
      disk_network_ids = metrics_collector.get_disk_and_network_ids(uuid)

      # Unless it returns nil (VM Deleted between measurements...) we added to or list to monitor
      unless disk_network_ids.nil?
        uuids_new[uuid]=disk_network_ids
      end
    else
      uuids_new[uuid]=uuids_old[uuid]
    end

    zabbix_sender_string="#{uuid} physical.host #{metrics[:timestamp]} #{hostname}"
    zabbix_sender_string<<"#{uuid} cpu.time #{metrics[:timestamp]} #{metrics[:cpu_time]} \n"
    zabbix_sender_string<<"#{uuid} user.time #{metrics[:timestamp]} #{metrics[:user_time]} \n"
    zabbix_sender_string<<"#{uuid} system.time #{metrics[:timestamp]} #{metrics[:system_time]} \n"
    zabbix_sender_string<<"#{uuid} memory #{metrics[:timestamp]} #{metrics[:memory]} "

    if metrics[:percentage_cpu] > 0
      zabbix_sender_string<<"\n"
      zabbix_sender_string<<"#{uuid} cpu.measured #{metrics[:timestamp]} #{metrics[:percentage_cpu]}"
    end

    if metrics[:wr_bytes] > 0
      zabbix_sender_string<<"\n"
      # Disk Metrics... 
      zabbix_sender_string<<"#{uuid} wr.bytes #{metrics[:timestamp]} #{metrics[:wr_bytes]} \n"
      zabbix_sender_string<<"#{uuid} wr.operations #{metrics[:timestamp]} #{metrics[:wr_operations]} \n"
      zabbix_sender_string<<"#{uuid} rd.bytes #{metrics[:timestamp]} #{metrics[:rd_bytes]} \n"
      zabbix_sender_string<<"#{uuid} rd.operations #{metrics[:timestamp]} #{metrics[:rd_operations]} \n"
      zabbix_sender_string<<"#{uuid} flush.operations #{metrics[:timestamp]} #{metrics[:flush_operations]} \n"
      zabbix_sender_string<<"#{uuid} wr.total.times #{metrics[:timestamp]} #{metrics[:wr_total_times]} \n"
      zabbix_sender_string<<"#{uuid} rd.total.times #{metrics[:timestamp]} #{metrics[:rd_total_times]} \n"
      zabbix_sender_string<<"#{uuid} flush.total.times #{metrics[:timestamp]} #{metrics[:flush_total_times]}"
    end

    if metrics[:rx_bytes] > 0
      zabbix_sender_string<<"\n"
      #Network Metrics
      zabbix_sender_string<<"#{uuid} rx.bytes #{metrics[:timestamp]} #{metrics[:rx_bytes]} \n"
      zabbix_sender_string<<"#{uuid} rx.drop #{metrics[:timestamp]} #{metrics[:rx_drop]} \n"
      zabbix_sender_string<<"#{uuid} rx.errs #{metrics[:timestamp]} #{metrics[:rx_errs]} \n"
      zabbix_sender_string<<"#{uuid} rx.packets #{metrics[:timestamp]} #{metrics[:rx_packets]} \n"
      zabbix_sender_string<<"#{uuid} tx.bytes #{metrics[:timestamp]} #{metrics[:tx_bytes]} \n"
      zabbix_sender_string<<"#{uuid} tx.drop #{metrics[:timestamp]} #{metrics[:tx_drop]} \n"
      zabbix_sender_string<<"#{uuid} tx.errs #{metrics[:timestamp]} #{metrics[:tx_errs]} \n"
      zabbix_sender_string<<"#{uuid} tx.packets #{metrics[:timestamp]} #{metrics[:tx_packets]}"
    end

    puts zabbix_sender_string

    output=`echo '#{zabbix_sender_string}' |  zabbix_sender -vv --zabbix-server #{zabbix_ip_address} -T --input-file - > /dev/null 2>&1`      
  end

  uuids_old=uuids_new
  sleep(sleep_time)
end