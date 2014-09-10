/**
 * Copyright (C) 2013 Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package eu.ascetic.energy.modeller.display.tool;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.graphs.ChartPanelSupport;
import eu.ascetic.graphs.LineChartPanelSupport;
import eu.ascetic.graphs.data.TranslatingXYDataset;
import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.List;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

//import java.util.Date;
public class CurrentLineChart extends LineChartPanelSupport<HashMap<String, List<CurrentUsageRecord>>> {

    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -705459046389814891L;
    private String host = null;

    /**
     * @param data The data to be plotted.
     */
    public CurrentLineChart(HashMap<String, List<CurrentUsageRecord>> data) {
        super("Energy Consumed on the Testbed", "Energy Consumption (W)", data, ChartPanelSupport.DEFAULT_DELAY);
        setRangeAxisRange(250, 500);
    }

    /**
     * @param data The data to be plotted.
     * @param host The hosts name
     */
    public CurrentLineChart(HashMap<String, List<CurrentUsageRecord>> data, String host) {
        super("Energy Consumed on the Testbed", "Energy Consumption (W)", data, ChartPanelSupport.DEFAULT_DELAY);
        this.host = host;
        if (host == null) {
            setRangeAxisRange(250, 500);
        } else {
            setRangeAxisRange(0, 500);
        }
        this.setName(host);
        this.setTitle(host);
    }

    /**
     * @param data The data to be plotted.
     * @param maxEnergy The maximum amount of energy to be plotted.
     */
    public CurrentLineChart(HashMap<String, List<CurrentUsageRecord>> data, int maxEnergy) {
        super("Energy Consumed on the Testbed", "Energy Consumption (W)", data, ChartPanelSupport.DEFAULT_DELAY);
        setRangeAxisRange(0, maxEnergy);
    }

    @Override
    protected void createSeries() {
        this.setDefaultLineStroke(new BasicStroke(15));
        if (this.getSeries().size() < 1) {
            this.getTimeSeries().addSeries(new TimeSeries("asok09"));
            this.getTimeSeries().addSeries(new TimeSeries("asok10"));
            this.getTimeSeries().addSeries(new TimeSeries("asok11"));
            this.getTimeSeries().addSeries(new TimeSeries("asok12"));
            this.setDataset(new TranslatingXYDataset(this.getTimeSeries()));
        }
    }

    /**
     * This adds a series to the graph.
     *
     * @param name The name of the time series to add
     */
    public void addSeries(String name) {
        this.getTimeSeries().addSeries(new TimeSeries(name));
    }

    @Override
    public void update() {
        if (host != null) {
            this.getTimeSeries().removeAllSeries();
            for (String name : getData().keySet()) {
                if (name.contains(host)) {
                    this.getTimeSeries().addSeries(new TimeSeries(name));
                }
            }
        }

        if (this.getSeries().size() < 1) {
            return;
        }

        if (getData() != null && !getData().isEmpty()) {
            for (int i = 0; i < this.getSeries().size(); i++) {
                List<CurrentUsageRecord> records = getData().get(this.getSeries().get(i).getKey().toString());
                if (records != null) {
                    for (CurrentUsageRecord current : records) {
                        ((TimeSeries) this.getSeries().get(i)).addOrUpdate(new Second(current.getTime().getTime()), current.getPower());
                    }
                }
//                DoubleTimePair newVal = getLastValue(this.getSeries().get(i).getKey().toString(), getData());
//                if (newVal != null) {
//                    ((TimeSeries) this.getSeries().get(i)).addOrUpdate(new Second(newVal.getDate()), newVal.getPower());
//                }
            }

//            ((TimeSeries) this.getSeries().get(0)).addOrUpdate(new Second(getData().get(getData().size() - 1).getTime().getTime()), getData().get(getData().size() - 1).getPower());
//            ((TimeSeries) this.getSeries().get(2)).addOrUpdate(new Second(getData().get(getData().size() - 1).getTime().getTime()), getData().get(getData().size() - 1).getPower());
//            ((TimeSeries) this.getSeries().get(3)).addOrUpdate(new Second(getData().get(getData().size() - 1).getTime().getTime()), getData().get(getData().size() - 1).getPower());
        }
    }

//
//    private DoubleTimePair getLastValue(String host, List<CurrentUsageRecord> data) {
//        for (int i = data.size() - 1; i >= 0; i--) {
//            CurrentUsageRecord current = data.get(i);
//            if (((Host) current.getEnergyUser().toArray()[0]).getHostName().equals(host)) {
//                return new DoubleTimePair(current.getTime().getTime(), current.getPower());
//            }
//        }
//        return null;
//    }
//    private DoubleTimePair getLastValue(String host, HashMap<String, List<CurrentUsageRecord>> data) {
//        List<CurrentUsageRecord> list = data.get(host);
//        if (list.isEmpty()) {
//            return null;
//        }
//        CurrentUsageRecord current = list.get(list.size() - 1);
//        return new DoubleTimePair(current.getTime().getTime(), current.getPower());
//    }
//   
//    private class DoubleTimePair {
//
//        private Date date;
//        private double power;
//
//        public DoubleTimePair(Date date, double power) {
//            this.date = date;
//            this.power = power;
//        }
//
//        public double getPower() {
//            return power;
//        }
//
//        public Date getDate() {
//            return date;
//        }
//
//    }
    @Override
    public String toString() {
        if (host == null) {
            return this.getClass().getSimpleName();
        }
        return host;
    }

}
