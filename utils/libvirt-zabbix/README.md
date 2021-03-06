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

Check this metrics of Richard

/Power and energy
    public static final String POWER_KPI_NAME = "power";
    public static final String ESTIMATED_POWER_KPI_NAME = "power-estimated";
    public static final String ENERGY_KPI_NAME = "energy";
    //CPU based metrics
    public static final String CPU_COUNT_KPI_NAME = "system.cpu.num";
    public static final String CPU_IDLE_KPI_NAME=  "system.cpu.util[,idle]";
    public static final String CPU_INTERUPT_KPI_NAME = "system.cpu.util[,interrupt]";
    public static final String CPU_IO_WAIT_KPI_NAME = "system.cpu.util[,iowait]";
    public static final String CPU_NICE_KPI_NAME = "system.cpu.util[,nice]";
    public static final String CPU_SOFT_IRQ_KPI_NAME = "system.cpu.util[,softirq]";
    public static final String CPU_STEAL_KPI_NAME = "system.cpu.util[,steal]";
    public static final String CPU_SYSTEM_KPI_NAME = "system.cpu.util[,system]";
    public static final String CPU_USER_KPI_NAME = "system.cpu.util[,user]";
    public static final String CPU_SPOT_USAGE_KPI_NAME = "cpu-measured";
    public static final String CPU_LOAD_LAST_1_MIN_KPI_NAME = "system.cpu.load[percpu,avg1]";
    public static final String CPU_LOAD_LAST_5_MIN_KPI_NAME = "system.cpu.load[percpu,avg5]";
    public static final String CPU_LOAD_LAST_15_MIN_KPI_NAME = "system.cpu.load[percpu,avg15]";    
    //memory metrics   
    public static final String MEMORY_AVAILABLE_KPI_NAME = "vm.memory.size[available]";     
    public static final String MEMORY_TOTAL_KPI_NAME = "vm.memory.size[total]";
    //swap space
    public static final String SWAP_SPACE_FREE_KPI_NAME = "system.swap.size[,free]";     
    public static final String SWAP_SPACE_FREE_PERC_KPI_NAME = "system.swap.size[,pfree]";     
    public static final String SWAP_SPACE_TOTAL_KPI_NAME = "system.swap.size[,total]";     
    //disk metrics
    public static final String DISK_FREE_KPI_NAME = "vfs.fs.size[/,free]"; 
    public static final String DISK_FREE_PERC_KPI_NAME = "vfs.fs.size[/,pfree]"; 
    public static final String DISK_USED_KPI_NAME = "vfs.fs.size[/,used]"; 
    public static final String DISK_TOTAL_KPI_NAME = "vfs.fs.size[/,total]";
    //Network
    public static final String NETWORK_OUT_STARTS_WITH_KPI_NAME = "net.if.in[eth";    
    public static final String NETWORK_IN_STARTS_WITH_KPI_NAME = "net.if.out[eth";    
    //boot time
    public static final String BOOT_TIME_KPI_NAME = "system.boottime";
    //physical host mapping
    public static final String VM_PHYSICAL_HOST_NAME = "physical_host";