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

require 'libvirt'
require 'nokogiri'

class LibvirtMetricsCollector
  Metrics = Struct.new(:timestamp, :cpu_time, :user_time, :system_time, :percentage_cpu, 
                       :memory, 
                       :wr_bytes, :wr_operations, :rd_bytes, :rd_operations, :flush_operations, :wr_total_times, :rd_total_times, :flush_total_times, 
                       :rx_bytes, :rx_drop, :rx_errs, :rx_packets, :tx_bytes, :tx_drop, :tx_errs, :tx_packets)
  DiskNetworkID = Struct.new(:disk_id, :network_id)

  def initialize
    @conn = Libvirt::open('qemu:///system')
  end

  def get_number_cores_host
    check_connection

    info = @conn.node_get_info

    info.cpus
  end

  def get_disk_and_network_ids(uuid)
    check_connection
    
    begin
      domain = @conn.lookup_domain_by_uuid(uuid)
      
      xml=domain.xml_desc
      doc = Nokogiri::XML(xml)

      disk_path=doc.xpath("//disk/source").first["file"]
      if_path=doc.xpath("//interface/target").first["dev"]

      DiskNetworkID.new(disk_path, if_path)

    rescue Exception
      # If domain does not exits, return null... 
      return nil
    end
  end

  def get_metrics(old_vms, host_cpu_cores, disk_network_ids)
    check_connection

    vms = Hash.new

    domains = @conn.list_all_domains
    domains.each do |domain|
      active = domain.active?

      if active
        uuid = domain.uuid

        cpu_stats = domain.cpu_stats(start_cpu=-1, numcpus=1, flags=0)
        now = Time.now.to_i
        cpu_time = cpu_stats['all']['cpu_time']
        user_time = cpu_stats['all']['user_time']
        system_time = cpu_stats['all']['system_time']
        percentage = -1
        
        # To get the amount of memory
        info = domain.info

        metrics_old = old_vms[uuid]

        # Calculation based on question 4 of
        # this URL: http://people.redhat.com/~rjones/virt-top/faq.html
        unless metrics_old.nil?
          cpu_time_difference = cpu_time - metrics_old[:cpu_time]
          time_difference = now - metrics_old[:timestamp]
          percentage = cpu_time_difference.to_f / (time_difference * host_cpu_cores * 1000000000).to_f * 100.0
        end

        unless disk_network_ids[uuid].nil?
          # We retrive the disk stats
          block_stats = domain.block_stats_flags(disk_network_ids[uuid][:disk_id],0)
          # We retrieve network stats
          if_stats=domain.ifinfo(disk_network_ids[uuid][:network_id])

          metrics_new = Metrics.new(now, cpu_time, user_time, system_time, percentage,
                                    info.memory,
                                    block_stats['wr_bytes'], block_stats['wr_operations'],
                                    block_stats['rd_bytes'], block_stats['rd_operations'], 
                                    block_stats['flush_operations'],
                                    block_stats['wr_total_times'], block_stats['rd_total_times'], block_stats['flush_total_times'],
                                    if_stats.rx_bytes, if_stats.rx_drop, if_stats.rx_errs, if_stats.rx_packets,
                                    if_stats.tx_bytes, if_stats.tx_drop, if_stats.tx_errs, if_stats.tx_packets)
                                    
          vms[uuid] = metrics_new
        else
          metrics_new = Metrics.new(now, cpu_time, user_time, system_time, percentage,
                                    info.memory,
                                    -1, -1, -1, -1, -1, -1, -1, -1,
                                    -1, -1, -1, -1, -1, -1, -1, -1)
          vms[uuid] = metrics_new
        end 
      end
    end

    vms
  end

  private 

    def check_connection
       if @conn.closed?
        @conn = Libvirt::open('qemu:///system')
      end
    end
end
