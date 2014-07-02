
package es.bsc.monitoring.ganglia.infrastructure;

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
public class Metric {

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