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

require "zabbixapi"

class ZabbixAscetic

  def initialize(ip_address, usename, password)
    
    url = 'http://' + ip_address + '/zabbix/api_jsonrpc.php'
    
    @zbx = ZabbixApi.connect(
      :url => url,
      :user => usename,
      :password => password,
      :http_password => usename,
      :http_user => password
    )
  end

  def setup(hostgroup, template, delay)
    @hostgroup=hostgroup
    @template=template
    @delay=delay

    # If the host group does not exists it is created
    @hostgroup_id = @zbx.hostgroups.get_id(:name => @hostgroup)

    if @hostgroup_id.nil?
      @hostgroup_id = @zbx.hostgroups.create(:name => @hostgroup)
    end

    # If the template does not exists it is created
    @template_id = @zbx.templates.get_id(:host => @template)

    if @template_id.nil? 
      @template_id = @zbx.templates.create(
                  :host => @template,
                  :groups => [:groupid => @zbx.hostgroups.get_id(:name => hostgroup)]
                )
    end

    # We check that the Applications are associated to the template
    ["CPU", "MEMORY", "DISK", "NETWORK", "HOST"].each { |application| check_create_application(application)}

    # Now we need to verify that the necessary items are there or create them
    #    value_type = 3 -> integer
    #    value_type = 0 -> decimal

    # CPU Metrics
    application = @zbx.applications.get_id(
                            :name => "CPU",
                            :hostid => @template_id
                          )
    check_create_item("cpu_time", "Libvirt CPU time for the VM", "cpu.time", 3, application)
    check_create_item("user_time", "Libvirt CPU User time for the VM", "user.time", 3, application)
    check_create_item("system_time", "Libvirt CPU System time for the VM", "system.time", 3, application)
    check_create_item("cpu-measured", "Libvirt % HOST CPU for the VM", "cpu.measured", 0, application)
    
    # Memory metrics
    application = @zbx.applications.get_id(
                            :name => "MEMORY",
                            :hostid => @template_id
                          )
    check_create_item("memory", "Libvirt allocated memory for the VM", "memory", 3, application)

    # DISK metrics (for the first disk of the VM)
    application = @zbx.applications.get_id(
                            :name => "DISK",
                            :hostid => @template_id
                          )
    check_create_item("wr_bytes", "Libvirt wr_bytes metrics for the first disk of the VM", "wr.bytes", 3, application)
    check_create_item("wr_operations", "Libvirt wr_operations metrics for the first disk of the VM", "wr.operations", 3, application)
    check_create_item("rd_bytes", "Libvirt rd_bytes metrics for the first disk of the VM", "rd.bytes", 3, application)
    check_create_item("rd_operations", "Libvirt rd_operations metrics for the first disk of the VM", "rd.operations", 3, application)
    check_create_item("flush_operations", "Libvirt flush_operations metrics for the first disk of the VM", "flush.operations", 3, application)
    check_create_item("wr_total_times", "Libvirt wr_total_times metrics for the first disk of the VM", "wr.total.times", 3, application)
    check_create_item("rd_total_times", "Libvirt rd_total_times metrics for the first disk of the VM", "rd.total.times", 3, application)
    check_create_item("flush_total_times", "Libvirt flush_total_times metrics for the first disk of the VM", "flush.total.times", 3, application)

    # NETWORK Metrics
    application = @zbx.applications.get_id(
                            :name => "NETWORK",
                            :hostid => @template_id
                          )
    check_create_item("rx_bytes", "Libvirt rx_bytes metrics for the first disk of the VM", "rx.bytes", 3, application)
    check_create_item("rx_drop", "Libvirt rx_drop metrics for the first disk of the VM", "rx.drop", 3, application)
    check_create_item("rx_packets", "Libvirt rx_packets metrics for the first disk of the VM", "rx.packets", 3, application)
    check_create_item("rx_errs", "Libvirt rx_errs metrics for the first disk of the VM", "rx.errs", 3, application)
    check_create_item("tx_bytes", "Libvirt tx_bytes metrics for the first disk of the VM", "tx.bytes", 3, application)
    check_create_item("tx_drop", "Libvirt tx_drop metrics for the first disk of the VM", "tx.drop", 3, application)
    check_create_item("tx_packets", "Libvirt tx_packets metrics for the first disk of the VM", "tx.packets", 3, application)
    check_create_item("tx_errs", "Libvirt tx_errs metrics for the first disk of the VM", "tx.errs", 3, application)

    #PHYSICAL HOST Metrics
    application = @zbx.applications.get_id(
                            :name => "HOST",
                            :hostid => @template_id
                          )
    check_create_item("physical_host", "Name of the physical host where the virtual machine is", "physical.host", 4, application)
  end

  def add_host_to_template(create, hostname)
    host = @zbx.hosts.get_id( :host => hostname )

    if host.nil? && create
        host = @zbx.hosts.create(
          :host => hostname,
          :interfaces => [
            {
              :type => 1,
              :main => 1,
              :ip => '0.0.0.0',
              :dns => '',
              :port => 10050,
              :useip => 1
            }
          ],
          :groups => [ :groupid => @hostgroup_id ]
        )
    end

    # We continue if the host is not nill
    unless host.nil?
      @zbx.query(
        :method => "host.update",
        :params => {
          :hostid => host,
          :templates => [ :templateid => @template_id]
        }
      )
    end
  end

  private

    def check_create_application(name)

      application = @zbx.applications.get_id(
                              :name => name,
                              :hostid => @template_id
                            )

      if application.nil?
        @zbx.applications.create(
                             :name => name,
                             :hostid => @template_id
                           )
      end
    end

    def check_create_item(name, description, key, value_type, application)
      item = @zbx.items.get_id(
                    :name => name,
                    :hostid => @template_id
                  )

      if item.nil?
        item = @zbx.items.create(
                :name => name,
                :description => description,
                :key_ => key,
                :delay => @delay,
                :type => 2,
                :value_type => value_type,
                :hostid => @template_id,
                :applications => [application],
                :trapper_hosts => ""
              )
      end
    end
end



