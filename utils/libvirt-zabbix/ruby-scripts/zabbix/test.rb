require "zabbixapi"
require 'logger' 

log = Logger.new(STDOUT)
log.level = Logger::DEBUG

# use basic_auth
zbx = ZabbixApi.connect(
  :url => 'http://192.168.252.40/zabbix/api_jsonrpc.php',
  :user => 'Admin',
  :password => 'zabbix',
  :http_password => 'Admin',
  :http_user => 'zabbix',
  #:debug => true
)

template = zbx.templates.get_id(:host => "Template.Virt.Libvirt")
puts template

host = zbx.hosts.get_id(:host => "febe")
puts host
puts host.to_s.empty?

host = zbx.hosts.get_id(:host => "pepito")
puts host
puts host.to_s.empty?

hostgroup = zbx.hostgroups.get_id(:name => "vms")
puts hostgroup

if hostgroup.nil?
  hostgroup = zbx.hostgroups.create(:name => "vms")
  puts hostgroup
end

template = zbx.templates.get_id(:host => "Template.Virt.Libvirt")
puts template

if template.nil? 
  template = zbx.templates.create(
                  :host => "Template.Virt.Libvirt",
                  :groups => [:groupid => zbx.hostgroups.get_id(:name => "vms")]
                )
  puts "Template: #{template}"
end

templates = zbx.templates.get_ids_by_host( :hostids => [zbx.hosts.get_id(:host => "febe")] )

puts templates

application = zbx.applications.get_id(
                              :name => "CPU",
                            :hostid => zbx.templates.get_id(:host => "Template.Virt.Libvirt")
                          )

puts "Application #{application}"

if application.nil?
    zbx.applications.create(
                    :name => "CPU",
                    :hostid => zbx.templates.get_id(:host => "Template.Virt.Libvirt")
                   )
    puts "Application Created: #{application}"
end

item = zbx.items.get_id(
                    :name => "cpu_time",
                    :hostid => zbx.templates.get_id(:host => "Template.Virt.Libvirt")
                  )

puts "Item CPU_TIME: #{item}"

if item.nil?
  item = zbx.items.create(
              :name => "cpu_time",
              :description => "Libvirt CPU time for the VM",
              :key_ => "cpu.time",
              :type => 0,
              :value_type => 3,
              :hostid => zbx.templates.get_id(:host => "Template.Virt.Libvirt"),
              :applications => [zbx.applications.get_id(:name => "CPU")]
            )
  puts "Item CPU_TIME created: #{item}"
end

host = zbx.hosts.get_id( :host => "textX2" )
puts "HOST GET: #{host}"

if host.nil?
  host = zbx.hosts.create(
          :host => "textX2",
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
          :groups => [ :groupid => zbx.hostgroups.get_id(:name => "vms") ]
        )
  puts "HOST CREATE: #{host}"
end

template = zbx.templates.get_id(:host => "Template.Virt.Libvirt")
puts "Template id: #{template}"

host = zbx.hosts.update(
  :hostid => zbx.hosts.get_id( :host => "textX2" ),
  :templates => [ :templateid => zbx.templates.get_id(:host => "Template.Virt.Libvirt")],
)

#zbx.query(
#  :method => "host.update",
#  :params => {
#      :hostid => zbx.hosts.get_id( :host => "textX2" ),
#      :templates => [ :templateid => zbx.templates.get_id(:host => "Template.Virt.Libvirt")]
#  }
#)

item = zbx.items.create(
       :name => "user_time",
       :description => "Libvirt CPU User time for the VM",
       :key_ => "user.time",
       :delay => 10,
       :type => 0,
       :value_type => 3,
       :hostid => zbx.templates.get_id(:host => "Template.Virt.Libvirt"),
       :applications => [application]
)

puts "Template id: #{item}"


