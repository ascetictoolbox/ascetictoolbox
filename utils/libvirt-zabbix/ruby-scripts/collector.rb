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
require './libvirt/libvirt_events_listener.rb'
require 'logger'
require 'libvirt'

# Configuration
#$zabbix_ip_address="192.168.3.199"
#zabbix_username="Admin"
#zabbix_password="Brandmeldeanlage104"
#zabbix_hostgroup="Virtual machines"
#zabbix_template="Template.Virt.Libvirt"
#zabbix_client_create_host=true

$send_disk=false
$send_network=true
$send_memory=true
$send_cpu=true
$send_hostname=true

$zabbix_ip_address = ZabbixAscetic::ZABBIX_IP_ADDRESS

sleep_time=10

logger = Logger.new(STDOUT)
logger.level = Logger::DEBUG

$hostname=`hostname -s`

# send network content to zabbix
def zabbix_send_network(uuid,metrics,zabbix_sender_string)
    
    zabbix_sender_string<<"#{uuid} rx.bytes #{metrics[:timestamp]} #{metrics[:rx_bytes]} \n"
    zabbix_sender_string<<"#{uuid} rx.drop #{metrics[:timestamp]} #{metrics[:rx_drop]} \n"
    zabbix_sender_string<<"#{uuid} rx.errs #{metrics[:timestamp]} #{metrics[:rx_errs]} \n"
    zabbix_sender_string<<"#{uuid} rx.packets #{metrics[:timestamp]} #{metrics[:rx_packets]} \n"
    zabbix_sender_string<<"#{uuid} tx.bytes #{metrics[:timestamp]} #{metrics[:tx_bytes]} \n"
    zabbix_sender_string<<"#{uuid} tx.drop #{metrics[:timestamp]} #{metrics[:tx_drop]} \n"
    zabbix_sender_string<<"#{uuid} tx.errs #{metrics[:timestamp]} #{metrics[:tx_errs]} \n"
    zabbix_sender_string<<"#{uuid} tx.packets #{metrics[:timestamp]} #{metrics[:tx_packets]} "
end

# send disk content to zabbix
def zabbix_send_disk(uuid,metrics)

    zabbix_sender_string<<"#{uuid} wr.bytes #{metrics[:timestamp]} #{metrics[:wr_bytes]} \n"
    zabbix_sender_string<<"#{uuid} wr.operations #{metrics[:timestamp]} #{metrics[:wr_operations]} \n"
    zabbix_sender_string<<"#{uuid} rd.bytes #{metrics[:timestamp]} #{metrics[:rd_bytes]} \n"
    zabbix_sender_string<<"#{uuid} rd.operations #{metrics[:timestamp]} #{metrics[:rd_operations]} \n"
    zabbix_sender_string<<"#{uuid} flush.operations #{metrics[:timestamp]} #{metrics[:flush_operations]} \n"
    zabbix_sender_string<<"#{uuid} wr.total.times #{metrics[:timestamp]} #{metrics[:wr_total_times]} \n"
    zabbix_sender_string<<"#{uuid} rd.total.times #{metrics[:timestamp]} #{metrics[:rd_total_times]} \n"
    zabbix_sender_string<<"#{uuid} flush.total.times #{metrics[:timestamp]} #{metrics[:flush_total_times]} "
end

# send cpu content to zabbix
def zabbix_send_cpu(uuid,metrics,zabbix_sender_string)
    zabbix_sender_string<<"#{uuid} cpu.time #{metrics[:timestamp]} #{metrics[:cpu_time]} \n"
    zabbix_sender_string<<"#{uuid} user.time #{metrics[:timestamp]} #{metrics[:user_time]} \n"
    zabbix_sender_string<<"#{uuid} system.time #{metrics[:timestamp]} #{metrics[:system_time]} "
    if metrics[:percentage_cpu] > 0
      zabbix_sender_string<<"\n"
      zabbix_sender_string<<"#{uuid} cpu.measured #{metrics[:timestamp]} #{metrics[:percentage_cpu]} "
    end
end

#send memory content to zabbix
def zabbix_send_memory(uuid,metrics,zabbix_sender_string)
    zabbix_sender_string<<"#{uuid} memory #{metrics[:timestamp]} #{metrics[:memory]} "
end

#send hostname 
def zabbix_send_hostname(uuid,metrics,zabbix_sender_string)
  zabbix_sender_string<<"#{uuid} physical.host #{metrics[:timestamp]} #{$hostname}"
end

def empty_string(zabbix_sender_string)
  unless zabbix_sender_string==""
    zabbix_sender_string<<"\n"
  end
end

def zabbix_send_data(uuid,metrics)
    zabbix_sender_string=""

    # Hostname metrics...
    if $send_hostname
      empty_string(zabbix_sender_string)  
      zabbix_send_hostname(uuid,metrics,zabbix_sender_string)
    end
    
    # CPU Metrics...
    if $send_cpu 
      zabbix_send_cpu(uuid,metrics,zabbix_sender_string)
    end
    # Memory Metrics...
    if $send_memory 
     empty_string(zabbix_sender_string)   
     zabbix_send_memory(uuid,metrics,zabbix_sender_string)
    end

    # Disk Metrics... 
    if $send_disk 
     empty_string(zabbix_sender_string)
     zabbix_send_disk(uuid,metrics,zabbix_sender_string) 
    end   
    
    #Network Metrics
    if $send_network
     empty_string(zabbix_sender_string)
     zabbix_send_network(uuid,metrics,zabbix_sender_string) 
    end

    output=`echo '#{zabbix_sender_string}' |  zabbix_sender -vv --zabbix-server #{$zabbix_ip_address} -T --input-file - > /dev/null 2>&1`      
end

# We create de Zabbix Client
# We configure the Zabbix Client
zabbix_client=ZabbixAscetic.new
zabbix_client.setup

# Libvirt lifecycle events codes
VIR_DOMAIN_EVENT_DEFINED = 0
VIR_DOMAIN_EVENT_UNDEFINED = 1
VIR_DOMAIN_EVENT_STARTED = 2
VIR_DOMAIN_EVENT_SUSPENDED = 3
VIR_DOMAIN_EVENT_RESUMED = 4
VIR_DOMAIN_EVENT_STOPPED = 5
VIR_DOMAIN_EVENT_SHUTDOWN = 6
VIR_DOMAIN_EVENT_PMSUSPENDED = 7
VIR_DOMAIN_EVENT_CRASHED = 8

# Defined details
VIR_DOMAIN_EVENT_DEFINED_ADDED = 0
VIR_DOMAIN_EVENT_DEFINED_UPDATED = 1
VIR_DOMAIN_EVENT_DEFINED_RENAMED = 2

# Undefined details events
VIR_DOMAIN_EVENT_UNDEFINED_REMOVED = 0
VIR_DOMAIN_EVENT_UNDEFINED_RENAMED = 1

# Start details events
VIR_DOMAIN_EVENT_STARTED_BOOTED = 0
VIR_DOMAIN_EVENT_STARTED_MIGRATED = 1
VIR_DOMAIN_EVENT_STARTED_RESTORED = 2
VIR_DOMAIN_EVENT_STARTED_FROM_SNAPSHOT = 3
VIR_DOMAIN_EVENT_STARTED_WAKEUP = 4

# Suspended detail events
VIR_DOMAIN_EVENT_SUSPENDED_PAUSED = 0
VIR_DOMAIN_EVENT_SUSPENDED_MIGRATED = 1
VIR_DOMAIN_EVENT_SUSPENDED_IOERROR = 2
VIR_DOMAIN_EVENT_SUSPENDED_WATCHDOG = 3
VIR_DOMAIN_EVENT_SUSPENDED_RESTORED = 4
VIR_DOMAIN_EVENT_SUSPENDED_FROM_SNAPSHOT = 5
VIR_DOMAIN_EVENT_SUSPENDED_API_ERROR = 6

# Resumed detail events
VIR_DOMAIN_EVENT_RESUMED_UNPAUSED = 0
VIR_DOMAIN_EVENT_RESUMED_MIGRATED = 1
VIR_DOMAIN_EVENT_RESUMED_FROM_SNAPSHOT = 2

# Stopped detail events
VIR_DOMAIN_EVENT_STOPPED_SHUTDOWN = 0
VIR_DOMAIN_EVENT_STOPPED_DESTROYED = 1
VIR_DOMAIN_EVENT_STOPPED_CRASHED = 2
VIR_DOMAIN_EVENT_STOPPED_MIGRATED = 3
VIR_DOMAIN_EVENT_STOPPED_SAVED = 4
VIR_DOMAIN_EVENT_STOPPED_FAILED = 5
VIR_DOMAIN_EVENT_STOPPED_FROM_SNAPSHOT = 6

# Shutdown detail events
VIR_DOMAIN_EVENT_SHUTDOWN_FINISHED = 0

# PMSuspended detail events
VIR_DOMAIN_EVENT_PMSUSPENDED_MEMORY = 0
VIR_DOMAIN_EVENT_PMSUSPENDED_DISK = 1

# Panic detail events
VIR_DOMAIN_EVENT_CRASHED_PANICKED = 0

# It process the incomming events for the lifecycle
def dom_event_callback_lifecycle(conn, dom, event, detail, opaque)
  $uuid = dom.uuid

  logger = Logger.new(STDOUT)
  logger.level = Logger::DEBUG

  case event
  when VIR_DOMAIN_EVENT_DEFINED
    logger.info("Domain #{$uuid} has been defined")

    case detail
      when VIR_DOMAIN_EVENT_DEFINED_ADDED
        logger.info(" it has been added to libvirt")
      when VIR_DOMAIN_EVENT_DEFINED_UPDATED
        logger.info("it has been updated in libvirt")
      when VIR_DOMAIN_EVENT_DEFINED_RENAMED
        logger.info("it has been renamed in libvirt")
    end
  when VIR_DOMAIN_EVENT_UNDEFINED
    logger.info("Domain #{$uuid} has been undefined")

    case detail
    when VIR_DOMAIN_EVENT_UNDEFINED_REMOVED
      logger.info("it has been removed in libvirt")
    when VIR_DOMAIN_EVENT_UNDEFINED_RENAMED
      logger.info("it has been renamed in libvirt")
    end
  when VIR_DOMAIN_EVENT_STARTED
    logger.info("Domain #{$uuid} has been started")

    case detail
    when VIR_DOMAIN_EVENT_STARTED_BOOTED
      logger.info("it has been booted in libvirt")
      logger.info("VM that needs to be check if already exits in zabbix, otherwise add it.")
      fork do
        ENV['UUID_ADD'] = $uuid
        exec "ruby2.0 zabbix/add_vm_to_zabbix.rb $UUID_ADD"
      end
    when VIR_DOMAIN_EVENT_STARTED_MIGRATED
      logger.info("it has been migrated in libvirt")
    when VIR_DOMAIN_EVENT_STARTED_RESTORED
      logger.info("it has been restored in libvirt")
    when VIR_DOMAIN_EVENT_STARTED_FROM_SNAPSHOT
      logger.info("it has been started from snapshot in libvirt")
    when VIR_DOMAIN_EVENT_STARTED_WAKEUP
      logger.info("it has been wakeup in libvirt")
    end
  when VIR_DOMAIN_EVENT_SUSPENDED
    logger.info("Domain #{$uuid} has been suspended")

    case detail
    when VIR_DOMAIN_EVENT_SUSPENDED_PAUSED
        logger.info("it has been paused by libvirt")
    when VIR_DOMAIN_EVENT_SUSPENDED_MIGRATED
      logger.info("it has been migrated by libvirt")
    when VIR_DOMAIN_EVENT_SUSPENDED_IOERROR
      logger.info("it has reporterd and IO Error libvirt" )
    when VIR_DOMAIN_EVENT_SUSPENDED_WATCHDOG
      logger.info("it has been suspended by watchdog in libvirt")
    when VIR_DOMAIN_EVENT_SUSPENDED_RESTORED 
      logger.info("it has been restored in libvirt")
    when VIR_DOMAIN_EVENT_SUSPENDED_FROM_SNAPSHOT
      logger.info("it has been suspended from snapshoot in libvirt" )
    when VIR_DOMAIN_EVENT_SUSPENDED_API_ERROR 
      logger.info("it has been due to api error in libvirt")
    end
  when VIR_DOMAIN_EVENT_RESUMED
    logger.info("Domain #{$uuid} has been resumed")

    case detail
    when VIR_DOMAIN_EVENT_RESUMED_UNPAUSED
      logger.info("it has been resumed from unpaused error in libvirt")
    when VIR_DOMAIN_EVENT_RESUMED_MIGRATED
      logger.info("it has been resumed after migration in libvirt")
    when VIR_DOMAIN_EVENT_RESUMED_FROM_SNAPSHOT
      logger.info("it has been resumed from snapshot in libvirt")
    else
      logger.info("unknown detail reason: #{detail}")
    end
  when VIR_DOMAIN_EVENT_STOPPED
    logger.info("Domain #{$uuid} has been stopped")

    case detail
    when VIR_DOMAIN_EVENT_STOPPED_SHUTDOWN
      logger.info("it has been stopped from shutdown in libvirt")
    when VIR_DOMAIN_EVENT_STOPPED_DESTROYED
      logger.info("it has been destroyed in libvirt")
      logger.info("VM is deleted from Zabbix.")
      fork do
        ENV['UUID_DELETE'] = $uuid
        exec "ruby2.0 zabbix/delete_vm_from_zabbix.rb $UUID_DELETE"
      end
    when VIR_DOMAIN_EVENT_STOPPED_CRASHED
      logger.info("it crashed in libvirt")
    when VIR_DOMAIN_EVENT_STOPPED_MIGRATED
      logger.info("it has been stopped by migrationg in libvirt")
    when VIR_DOMAIN_EVENT_STOPPED_SAVED
      logger.info("it has been stopped and saved in libvirt")
    when VIR_DOMAIN_EVENT_STOPPED_FAILED
      logger.info("it has failed in libvirt")
    when VIR_DOMAIN_EVENT_STOPPED_FROM_SNAPSHOT
      logger.info("it has been stopped from snapshot in libvirt")
    else
      logger.info("unknown detail reason: #{detail}")
    end
  when VIR_DOMAIN_EVENT_SHUTDOWN
    logger.info("Domain #{$uuid} has been shutdown")

    case detail
    when VIR_DOMAIN_EVENT_SHUTDOWN_FINISHED
      logger.info("it has been finished by libvirt")
    else
      logger.info("unknown detail reason: #{detail}")
    end
  when VIR_DOMAIN_EVENT_PMSUSPENDED
    logger.info("Domain #{$uuid} has been pmshuspended")

    case detail
    when VIR_DOMAIN_EVENT_PMSUSPENDED_MEMORY
      logger.info("Guest domain has been suspended to memory")
    when VIR_DOMAIN_EVENT_PMSUSPENDED_DISK
      logger.info("Guest domain has been suspended to disk")
    else
      logger.info("unknown detail reason: #{detail}")
    end
  when VIR_DOMAIN_EVENT_CRASHED
    logger.info("Domain #{$uuid} has been crashed")

    case detail
    when VIR_DOMAIN_EVENT_CRASHED_PANICKED
      logger.info("Guest domain has panicked")
    else
      logger.info("unknown detail reason: #{detail}")
    end
  else 
    puts "Not recognize event for domain: #{$uuid} and event: #{event}"
  end
end

# We connect to Libvirt    
uri = "qemu:///system"
@conn = Libvirt::open(uri)

# We get a list of all active VMs and determine if needed to be added to Zabbix
uuids = Array.new

domains = @conn.list_all_domains
domains.each do |domain|
  active = domain.active?

  if active
    uuid = domain.uuid
    uuids.push(uuid)
  end
end

# We add then to Zabbix if necessary
uuids.each do |uuid|
    logger.info("VM that needs to be check if already exits in zabbix, otherwise add it: #{uuid}")
    zabbix_client.add_host_to_template(uuid)
end

# We start a new Thread to monitor the creation and delation of VMs.
t = Thread.new {

  # We setup Libvirt connector
  metrics_collector = LibvirtMetricsCollector.new
  host_cpu_cores = metrics_collector.get_number_cores_host

  #We start the bucle of measurments for CPU
  vms=Hash.new

  uuids_old=Hash.new

  while true 
   
    vms=metrics_collector.get_metrics(vms, host_cpu_cores, uuids_old)
    uuids_new=Hash.new

    vms.each do |uuid, metrics|

      unless uuids_old.has_key?(uuid)
        # We register the VM to the template if does not exits in Zabbix
        # zabbix_client.add_host_to_template(zabbix_client_create_host, uuid)
        # We need to know the ids of the disk
        disk_network_ids = metrics_collector.get_disk_and_network_ids(uuid)

        # Unless it returns nil (VM Deleted between measurements...) we added to or list to monitor
          unless disk_network_ids.nil?
            uuids_new[uuid]=disk_network_ids
          end
      else
        uuids_new[uuid]=uuids_old[uuid]
        uuids_old.delete(uuid)
      end

      # start send data to zabbix
      zabbix_send_data(uuid,metrics)
    end

    #uuids_old.each do |uuid, metrics|
    #  zabbix_client.delete_host(uuid)
    #end

    uuids_old=uuids_new
    sleep(sleep_time)
  end
}

# We activate the event listeners...
virEventLoopStart

# We register a listner... needs to be done quick if not a core_dump exception is rising
@conn2 = Libvirt::open(uri)
cb2 = @conn2.domain_event_register_any(Libvirt::Connect::DOMAIN_EVENT_ID_LIFECYCLE, :dom_event_callback_lifecycle, nil, "sweet")

while true
  sleep 1
end

t.join