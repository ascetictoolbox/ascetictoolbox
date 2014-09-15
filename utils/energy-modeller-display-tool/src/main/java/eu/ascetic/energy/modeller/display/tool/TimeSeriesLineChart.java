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

import eu.ascetic.graphs.LineChartPanelSupport;
import eu.ascetic.graphs.data.TranslatingXYDataset;
import java.awt.BasicStroke;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import org.jfree.data.time.TimeSeries;

public class TimeSeriesLineChart extends LineChartPanelSupport<ConcurrentHashMap<String, TimeSeries>> {

    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -705459046389814891L;
    private String host = null;
    private final HashSet<String> hosts = new HashSet<>();

    /**
     * @param data The data to be plotted.
     * @param hosts The list of host names
     */
    public TimeSeriesLineChart(ConcurrentHashMap<String, TimeSeries> data, Collection<String> hosts) {
        super("Energy Consumed on the Testbed", "Energy Consumption (W)", data, 2);
        setRangeAxisRange(250, 500);
        this.setDefaultLineStroke(new BasicStroke(50));
        this.hosts.addAll(hosts);
    }

    /**
     * @param data The data to be plotted.
     * @param host The hosts name
     */
    public TimeSeriesLineChart(ConcurrentHashMap<String, TimeSeries> data, String host) {
        super("Energy Consumed on the Testbed", "Energy Consumption (W)", data, 2);
        this.host = host;
        if (host == null) {
            setRangeAxisRange(250, 500);
        } else {
            setRangeAxisRange(0, 500);
        }
        this.setName(host);
        this.setTitle(host);
        this.setDefaultLineStroke(new BasicStroke(50));
    }

    /**
     * @param data The data to be plotted.
     * @param maxEnergy The maximum amount of energy to be plotted.
     */
    public TimeSeriesLineChart(ConcurrentHashMap<String, TimeSeries> data, int maxEnergy) {
        super("Energy Consumed on the Testbed", "Energy Consumption (W)", data, 2);
        setRangeAxisRange(0, maxEnergy);
        this.setDefaultLineStroke(new BasicStroke(50));
    }

    @Override
    protected void createSeries() {
        if (this.getSeries().size() < 1) {
//            this.getTimeSeries().addSeries(new TimeSeries("asok09"));
//            this.getTimeSeries().addSeries(new TimeSeries("asok10"));
//            this.getTimeSeries().addSeries(new TimeSeries("asok11"));
//            this.getTimeSeries().addSeries(new TimeSeries("asok12"));
            this.setDataset(new TranslatingXYDataset(this.getTimeSeries()));
        }
    }

    /**
     * This adds a host to the graph.
     *
     * @param name The name of the time series to add
     */
    public void addHost(String name) {
        hosts.add(name);
        this.getTimeSeries().addSeries(new TimeSeries(name));
    }

    @Override
    public void update() {
        if (host != null) {
            this.getTimeSeries().removeAllSeries();
            for (String name : getData().keySet()) {
                if (name.contains(host)) {
                    this.getTimeSeries().addSeries(getData().get(name));
                }
            }
        } else {
            this.getTimeSeries().removeAllSeries();
            if (hosts == null) {
                this.getTimeSeries().addSeries(new TimeSeries("Please Wait, Initialising"));
            } else {
                for (String name : hosts) {
                    if (getData().get(name) == null) {
                        this.getTimeSeries().addSeries(new TimeSeries("Please Wait, Initialising"));
                    } else {
                        this.getTimeSeries().addSeries(getData().get(name));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (host == null) {
            return "All Host Data";
        }
        return host;
    }

}
