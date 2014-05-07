/**
 *     Copyright (C) 2013 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package wattsup.jsdk.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleInsets;

public abstract class ChartPanelSupport<T> extends JPanel
{

    /**
     * The default delay to update the chart.
     */
    protected static final int DEFAULT_DELAY = 1;

    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 7110133162302283596L;

    /**
     * The title of the chart.
     */
    private String title_;

    /**
     * The chart's axis label.
     */
    private String axisLabel_;

    /**
     * The default color for the line. The default is {@link Color#GREEN}.
     */
    private Color defaultLineColor_ = Color.GREEN;

    /**
     * The reference to panel with of the chart.
     */
    private final org.jfree.chart.ChartPanel chartPanel_;

    /**
     * The reference to the chart. It's defined by the method {@link #createChart()}.
     */
    private JFreeChart chart_;

    /**
     * 
     */
    private BasicStroke defaultLineStroke_ = new BasicStroke(1.5F);

    /**
     * The interval in seconds to update the chart.
     */
    private int updateDelayInSeconds_;

    /**
     * The data repository to be plot in the chart. Might not be <code>null</code>.
     */
    private final T data_;

    /**
     * The executor to update the chart every period determined by {@link #getUpdateDelayInSeconds()}.
     */
    private final ScheduledExecutorService updateChartExecutorService_;

    /**
     * 
     * @param title
     *            The title of the chart.
     * @param axisLabel
     *            The axis label.
     * @param data
     *            The data repository for the chart. Might not be <code>null</code>.
     */
    public ChartPanelSupport(String title, String axisLabel, T data)
    {
        this(title, axisLabel, data, DEFAULT_DELAY);
    }

    /**
     * 
     * @param title
     *            The title of the chart.
     * @param axisLabel
     *            The axis label.
     * @param data
     *            The data repository for the chart. Might not be <code>null</code>.
     * @param updateIntervalInSeconds
     *            The interval in seconds to update the chart.
     */
    public ChartPanelSupport(String title, String axisLabel, T data, int updateIntervalInSeconds)
    {
        this.title_ = title;
        this.axisLabel_ = axisLabel;
        this.data_ = data;
        this.updateDelayInSeconds_ = updateIntervalInSeconds;

        chart_ = this.createChart();
        this.chartPanel_ = new org.jfree.chart.ChartPanel(chart_);
        this.chartPanel_.setDomainZoomable(true);
        this.chartPanel_.setRangeZoomable(true);

        setLayout(new BoxLayout(this, 2));
        this.add(this.chartPanel_);

        updateChartExecutorService_ = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Start the schedule to update the view with a given delay.
     */
    public void start()
    {
        this.updateChartExecutorService_.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                update();
            }
        }, DEFAULT_DELAY, this.getUpdateDelayInSeconds(),
                TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);
        this.chartPanel_.setPreferredSize(new Dimension(width, height));
    }

    /**
     * Clean the values of the chart.
     * 
     * @see #createChart()
     */
    public void cleanChart()
    {
        this.chart_.removeLegend();
        this.chartPanel_.setBorder(null);

        cleanLabels();

        DateAxis localDateAxis = (DateAxis) ((XYPlot) this.chart_.getPlot()).getDomainAxis();
        localDateAxis.setTickLabelsVisible(false);
        localDateAxis.setTickMarksVisible(false);
        localDateAxis.setAxisLineVisible(false);

        ValueAxis localValueAxis = ((XYPlot) this.chart_.getPlot()).getRangeAxis();
        localValueAxis.setTickLabelsVisible(false);
        localValueAxis.setTickMarksVisible(false);
        localValueAxis.setAxisLineVisible(false);

        XYPlot localXYPlot = (XYPlot) this.chart_.getPlot();
        localXYPlot.setDomainGridlineStroke(this.defaultLineStroke_);
        localXYPlot.setDomainGridlinesVisible(true);
        localXYPlot.setRangeGridlineStroke(this.defaultLineStroke_);
        localXYPlot.setRangeGridlinesVisible(true);

        localXYPlot.setAxisOffset(new RectangleInsets(0.0D, 0.0D, 0.0D, 0.0D));
        localXYPlot.setInsets(new RectangleInsets(0.0D, 0.0D, 0.0D, 0.0D), true);
    }

    /**
     * Sets empty the chart's labels.
     */
    public void cleanLabels()
    {
        this.getChart().setTitle("");
        DateAxis localDateAxis = (DateAxis) ((XYPlot) this.chart_.getPlot()).getDomainAxis();
        localDateAxis.setLabel("");
        ValueAxis localValueAxis = ((XYPlot) this.chart_.getPlot()).getRangeAxis();
        localValueAxis.setLabel("");
    }

    /**
     * Update the chart.
     */
    public abstract void update();

    /**
     * Returns an instance of the {@link JFreeChart} to be rendered.
     * 
     * @return An instance of the {@link JFreeChart} to be rendered.
     */
    protected abstract JFreeChart createChart();

    /**
     * Returns the chart instance of this {@link JPanel}.
     * 
     * @return The chart instance of this {@link JPanel}.
     * @see #createChart()
     */
    public JFreeChart getChart()
    {
        return this.chart_;
    }

    /**
     * Returns the data repository for the chart.
     * 
     * @return the data repository for the chart.
     */
    public T getData()
    {
        return data_;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title_;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title)
    {
        this.title_ = title;
    }

    /**
     * @return the axisLabel
     */
    public String getAxisLabel()
    {
        return axisLabel_;
    }

    /**
     * @param axisLabel
     *            the axisLabel to set
     */
    public void setAxisLabel(String axisLabel)
    {
        this.axisLabel_ = axisLabel;
    }

    /**
     * @return the defaultLineColor
     */
    public Color getDefaultLineColor()
    {
        return defaultLineColor_;
    }

    /**
     * @param defaultLineColor
     *            the defaultLineColor to set
     */
    public void setDefaultLineColor(Color defaultLineColor)
    {
        this.defaultLineColor_ = defaultLineColor;
    }

    /**
     * @return the chartPanel
     */
    public org.jfree.chart.ChartPanel getChartPanel()
    {
        return chartPanel_;
    }

    /**
     * @return the updateInterval
     */
    public int getUpdateDelayInSeconds()
    {
        return updateDelayInSeconds_;
    }

    /**
     * @param updateInterval
     *            the updateInterval to set
     */
    public void updateDelayInSeconds(int updateInterval)
    {
        this.updateDelayInSeconds_ = updateInterval;
    }

    /**
     * @return the defaultLineStroke
     */
    public BasicStroke getDefaultLineStroke()
    {
        return defaultLineStroke_;
    }

    /**
     * @param defaultLineStroke
     *            the defaultLineStroke to set
     */
    public void setDefaultLineStroke(BasicStroke defaultLineStroke)
    {
        this.defaultLineStroke_ = defaultLineStroke;
    }
}
