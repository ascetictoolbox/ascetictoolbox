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

import java.util.List;

/**
 *
 * Configuration element for the Ganglia host mapping. Consists with Metrics information
 * of a particular host
 *
 *  <HOST NAME="" IP="" REPORTED="" TN="" TMAX="" DMAX="" LOCATION="" GMOND_STARTED="">
 *      <METRIC NAME="" VAL="" TYPE="" UNITS="" TN="" TMAX="" DMAX="" SLOPE="" SOURCE="">
 *      ............
 *      <METRIC NAME=....
 *  </HOST>
 * 
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 */
class Host {

    private String name;
    private String ip;
    private String reported;
    private String tn;
    private String tmax;
    private String dmax;
    private String location;
    private String gmondstarted;
    private List<Metric> metrics;

    public Host(String name, String ip, String reported, String tn, String tmax,
            String dmax, String location, String gmondstarted) {
        this.name = name;
        this.ip = ip;
        this.reported = reported;
        this.tn = tn;
        this.tmax = tmax;
        this.dmax = dmax;
        this.location = location;
        this.gmondstarted = gmondstarted;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getReported() {
        return reported;
    }

    public String getTn() {
        return tn;
    }

    public String getTmax() {
        return tmax;
    }

    public String getDmax() {
        return dmax;
    }

    public String getLocation() {
        return location;
    }

    public String getGmondstarted() {
        return gmondstarted;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
    
    public int getCpuNum() {
        int val = 0;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.CPU_NUM)) {
                val = Integer.parseInt(m.getValue());
            }
        }
        return val;
    }

    public Float getLoadOne() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.LOAD_ONE)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getLoadFive() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.LOAD_FIVE)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getLoadFifteen() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.LOAD_FIFTEEN)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getMemTotal() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.MEM_TOTAL)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getMemFree() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.MEM_FREE)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }
    
    public Float getMemBuffers() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.MEM_BUFFERS)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }
    
    public Float getMemCached() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.MEM_CACHED)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }
    
    public Float getMemUsed() {
        return this.getMemTotal() - this.getMemFree();
    }

    public Float getCPUUser() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.CPU_USER)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getCPUNice() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.CPU_NICE)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getCPUSystem() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.CPU_SYSTEM)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getCPUIdle() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.CPU_IDLE)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Float getCPUWio() {
        Float val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.CPU_WIO)) {
                val = Float.parseFloat(m.getValue());
            }
        }
        return val;
    }

    public Double getDiskTotal() {
        Double val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.DISK_TOTAL)) {
                val = Double.parseDouble(m.getValue());
            }
        }
        return val;
    }

    public Double getDiskFree() {
        Double val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.DISK_FREE)) {
                val = Double.parseDouble(m.getValue());
            }
        }
        return val;
    }

    public Double getDiskUsed() {
        return this.getDiskTotal() - this.getDiskFree();
    }

    public Double getPowerWatts() {
        Double val = null;
        for (Metric m : this.metrics) {
            if (m.getName().equals(GangliaMetKeys.POWER_WATTS)) {
                val = Double.parseDouble(m.getValue());
            }
        }
        return val;
    }

}
