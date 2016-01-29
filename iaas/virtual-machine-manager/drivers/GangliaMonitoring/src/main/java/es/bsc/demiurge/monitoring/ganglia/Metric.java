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

import java.util.HashMap;

/**
 *
 * Configuration element for the Ganglia Metric mapping.
 *
 *  <METRIC NAME="" VAL="" TYPE="" UNITS="" TN="" TMAX="" DMAX="" SLOPE="" SOURCE="">
 *      <EXTRA_DATA>
 *          <EXTRA_ELEMENT NAME="" VAL=""/>
 *          <EXTRA_ELEMENT NAME="" VAL=""/>
 *          ........
 *      </EXTRA_DATA>
 *  </METRIC>
 * 
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 */
class Metric {

    private String name;
    private String value;
    private String type;
    private String units;
    private String tn;
    private String tmax;
    private String dmax;
    private String slope;
    private String source;
    private HashMap<String, String> extraData;

    public Metric(String name, String value, String valueType, String units,
                  String tn, String tmax, String dmax, String slope, String source) {
        this.name = name;
        this.value = value;
        this.type = valueType;
        this.units = units;
        this.tn = tn;
        this.tmax = tmax;
        this.dmax = dmax;
        this.slope = slope;
        this.source = source;
    }


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getUnits() {
        return units;
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

    public String getSlope() {
        return slope;
    }

    public String getSource() {
        return source;
    }

    public HashMap<String, String> getExtraData() {
        return extraData;
    }

    public void setExtraData(HashMap<String, String> extraData) {
        this.extraData = extraData;
    }
}