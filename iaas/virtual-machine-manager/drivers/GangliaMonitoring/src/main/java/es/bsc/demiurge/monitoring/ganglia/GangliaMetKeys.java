/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.monitoring.ganglia;

/**
 * Definitions of Ganglia keys
 *
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 */
interface GangliaMetKeys {

    public static final String QUERY_SUMMARY = "?filter=summary";
    
    public static final String CLUSTER = "CLUSTER";
    public static final String HOST = "HOST";
    public static final String HOSTS = "HOSTS";

    public static final String METRIC = "METRIC";
    public static final String METRICS = "METRICS";
    
    public static final String NAME = "NAME";
    public static final String VAL = "VAL";
    public static final String TN = "TN";
    public static final String TMAX = "TMAX";
    public static final String DMAX = "DMAX";

    /*Metric specific keys*/
    public static final String TYPE = "TYPE";
    public static final String UNITS = "UNITS";
    public static final String SLOPE = "SLOPE";
    public static final String SOURCE = "SOURCE";
    public static final String EXTRA_ELEMENT = "EXTRA_ELEMENT";
    public static final String GROUP = "GROUP";
    public static final String DESC = "DESC";
    public static final String TITLE = "TITLE";

    /*Host specific keys*/
    public static final String IP = "IP";
    public static final String REPORTED = "REPORTED";
    public static final String LOCATION = "LOCATION";
    public static final String GMOND_STARTED = "GMOND_STARTED";

    /*Hosts specific key (summary query) */
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String SOURCE_HOST = "SOURCE";
    
    /*Cluster specific keys*/
    public static final String LOCALTIME = "LOCALTIME";
    public static final String OWNER = "OWNER";
    public static final String LATLONG = "LATLONG";
    public static final String URL = "URL";


    /* Host metrics ganglia+sflow */    
    /* System*/
    public static final String OS_NAME = "os_name";
    public static final String MACHINE_TYPE = "machine_type";
    public static final String OS_RELEASE = "os_release";
    public static final String UUID = "uuid";
    public static final String HEARTBEAT = "heartbeat";
    
    /* Load */
    public static final String LOAD_ONE = "load_one";
    public static final String LOAD_FIVE = "load_five";
    public static final String LOAD_FIFTEEN = "load_fifteen";
    
    /* Process*/
    public static final String PROC_RUN = "proc_run";
    public static final String PROC_TOTAL = "proc_total";
    
    /* CPU */
    public static final String CPU_NUM = "cpu_num";
    public static final String CPU_SPEED = "cpu_speed";
    public static final String BOOTTIME = "boottime";
    public static final String CPU_USER = "cpu_user";
    public static final String CPU_NICE = "cpu_nice";
    public static final String CPU_SYSTEM = "cpu_system";
    public static final String CPU_IDLE = "cpu_idle";
    public static final String CPU_WIO = "cpu_wio";
    public static final String CPU_INTR = "cpu_intr";
    public static final String CPU_SINTR = "cpu_sintr";
    public static final String INTERRUPTS = "interrupts";
    public static final String CONTEXTS = "contexts";
    
    /* Memory */
    public static final String MEM_TOTAL = "mem_total";
    public static final String MEM_FREE = "mem_free";
    public static final String MEM_SHARED = "mem_shared";
    public static final String MEM_BUFFERS = "mem_buffers";
    public static final String MEM_CACHED = "mem_cached";
    public static final String SWAP_TOTAL = "swap_total";
    public static final String SWAP_FREE = "swap_free";
    public static final String PAGE_IN = "page_in";
    public static final String PAGE_OUT = "page_out";
    public static final String SWAP_IN = "swap_in";
    public static final String SWAP_OUT = "swap_out";
    
    /* Disk */
    public static final String DISK_TOTAL = "disk_total";
    public static final String DISK_FREE = "disk_free";
    public static final String PART_MAX_USED = "part_max_used";
    public static final String READS = "reads";
    public static final String BYTES_READ = "bytes_read";
    public static final String READ_TIME = "read_time";
    public static final String WRITES = "writes";
    public static final String BYTES_WRITTEN = "bytes_written";
    public static final String WRITE_TIME = "write_time";
    
    /* Network */
    public static final String BYTES_IN = "bytes_in";
    public static final String PKTS_IN = "pkts_in";
    public static final String ERRS_IN = "errs_in";
    public static final String DROPS_IN = "drops_in";
    public static final String BYTES_OUT = "bytes_out";
    public static final String PKTS_OUT = "pkts_out";
    public static final String ERRS_OUT = "errs_out";
    public static final String DROPS_OUT = "drops_out";
    
    
    /* VM metrics */
    /* VM cpu */
    public static final String VCPU_STATE = "vcpu_state";
    public static final String VCPU_UTIL = "vcpu_util";
    public static final String VCPU_NUM = "vcpu_num";
       
    /* VM memory */
    public static final String VMEM_TOTAL = "vmem_total";
    public static final String VMEM_UTIL = "vmem_util";
    
    /* VM disk */
    public static final String VDISK_CAPACITY = "vdisk_capacity";
    public static final String VDISK_TOTAL = "vdisk_total";
    public static final String VDISK_FREE = "vdisk_free";
    public static final String VDISK_READS = "vdisk_reads";  
    public static final String VDISK_BYTES_READ = "vdisk_bytes_read";
    public static final String VDISK_WRITES = "vdisk_writes";
    public static final String VDISK_BYTES_WRITTEN = "vdisk_bytes_written";
    public static final String VDISK_ERRS = "vdisk_errs";
    
    /* VM network */
    public static final String VBYTES_IN = "vbytes_in";
    public static final String VPKTS_IN = "vpkts_in";
    public static final String VERRS_IN = "verrs_in";
    public static final String VDROPS_IN = "vdrops_in";
    public static final String VBYTES_OUT = "vbytes_out";    
    public static final String VPKTS_OUT = "vpkts_out";
    public static final String VERRS_OUT = "verrs_out";
    public static final String VDROPS_OUT = "vdrops_out";

    /* Power */
    public static final String POWER_WATTS = "powerWatts";

}

