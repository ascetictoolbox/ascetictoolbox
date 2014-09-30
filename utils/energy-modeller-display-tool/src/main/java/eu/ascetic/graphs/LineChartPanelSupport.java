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
package eu.ascetic.graphs;

import eu.ascetic.graphs.data.TranslatingXYDataset;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.Series;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

public abstract class LineChartPanelSupport<T> extends ChartPanelSupport<T> {

    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 1299430162357492697L;

    /**
     * The reference to {@link TimeSeriesCollection} used by this chart.
     */
    private TimeSeriesCollection timeSeries;

    /**
     * The reference to {@link TranslatingXYDataset} used by this chart.
     */
    private TranslatingXYDataset dataset;

    /**
     * *
     * @param title The title to be used by this chart.
     * @param axisLabel The axis label for this chart.
     * @param resource The resource that has the data to be used by this chart.
     * Might not be <code>null</code>.
     */
    public LineChartPanelSupport(final String title, final String axisLabel, final T resource) {
        this(title, axisLabel, resource, DEFAULT_DELAY);
    }

    /**
     * *
     * @param title The title to be used by this chart.
     * @param axisLabel The axis label for this chart.
     * @param resource The resource that has the data to be used by this chart.
     * Might not be <code>null</code>.
     * @param updateIntervalInSeconds The interval in seconds to update the
     * chart.
     */
    public LineChartPanelSupport(final String title, final String axisLabel, final T resource, final int updateIntervalInSeconds) {
        super(title, axisLabel, resource, updateIntervalInSeconds);

        createSeries();
        setMaximumItemAge(30000, true);
        start();
    }

    @Override
    public JFreeChart createChart() {
        this.timeSeries = new TimeSeriesCollection();
        this.dataset = new TranslatingXYDataset(this.timeSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(this.getTitle(), null, this.getAxisLabel(), this.dataset, true, true, false);

        chart.setBackgroundPaint(getBackground());
        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.setOrientation(PlotOrientation.VERTICAL);
        xyPlot.setBackgroundPaint(Color.WHITE);
        xyPlot.setDomainGridlinePaint(Color.BLACK.darker());
        xyPlot.setRangeGridlinePaint(Color.BLACK.darker());
        xyPlot.setAxisOffset(new RectangleInsets(5.0D, 5.0D, 5.0D, 5.0D));
        xyPlot.setDomainCrosshairLockedOnData(true);
        xyPlot.setRangeCrosshairVisible(true);
        chart.setAntiAlias(true);

        return chart;
    }

    /**
     * Creates the series to be used by this chart.
     */
    protected abstract void createSeries();

    /**
     * Sets the default paint used for the series.
     */
    protected void setDefaultSeriesPaint() {
        setSeriesPaint(this.getDefaultLineColor(), this.getDefaultLineStroke());
    }

    /**
     * Sets the axis range.
     *
     * @param lower The lower axis limit. (must be <= upper bound). @param u
     * pper The upper axis limit. (must be >= lower bound).
     */
    protected void setRangeAxisRange(int lower, int upper) {
        XYPlot localXYPlot = (XYPlot) this.getChart().getPlot();
        NumberAxis localNumberAxis = (NumberAxis) localXYPlot.getRangeAxis();
        localNumberAxis.setRange(lower, upper);
    }

    /**
     * Sets the color for a series.
     *
     * @param serie The index of a series. It's zero based.
     * @param color The color of a series. Might not be <code>null</code>.
     */
    protected void setSeriesPaint(int serie, Color color) {
        XYItemRenderer localXYItemRenderer = getPlot().getRenderer();
        localXYItemRenderer.setSeriesPaint(serie, color);
        localXYItemRenderer.setSeriesStroke(serie, this.getDefaultLineStroke());
        getPlot().setRenderer(localXYItemRenderer);
    }

    /**
     * Returns the reference to the {@link XYPlot} of this chart.
     *
     * @return the reference to the {@link XYPlot} of this chart.
     */
    protected XYPlot getPlot() {
        return (XYPlot) this.getChart().getPlot();
    }

    /**
     * Sets the paint and the stroke used for a series.
     *
     * @param colour The colour to be used. Might not be <code>null</code>.
     * @param stroke The {@code stroke} to be used. Might not be
     * <code>null</code>.
     */
    protected void setSeriesPaint(Color colour, BasicStroke stroke) {
        XYItemRenderer localXYItemRenderer = getPlot().getRenderer();

        for (int i = 0; i < this.getTimeSeries().getSeriesCount(); i++) {
            localXYItemRenderer.setSeriesPaint(i, colour);
            localXYItemRenderer.setSeriesStroke(i, stroke);
        }
    }

    /**
     * Returns an unmodified {@link List} of all the series in the collection.
     *
     * @return an unmodified {@link List} of all the series in the collection.
     * @see #getTimeSeries(){@link #getSeries()}
     */
    @SuppressWarnings("unchecked")
    protected List<Series> getSeries() {
        return this.getTimeSeries().getSeries();
    }

    /**
     * Sets the number of time units in the 'history' for the series. This
     * provides one mechanism for automatically dropping old data from the time
     * series.
     *
     * @param periods The number of time periods.
     * @param removeAgedItems Controls whether or not a
     * {@link org.jfree.data.general.SeriesChangeEvent} is sent to registered
     * listeners IF any items are removed.
     */
    protected void setMaximumItemAge(int periods, boolean removeAgedItems) {
        for (int i = 0; i < this.getTimeSeries().getSeriesCount(); i++) {
            this.getTimeSeries().getSeries(i).setMaximumItemAge(periods);
            this.getTimeSeries().getSeries(i).removeAgedItems(removeAgedItems);
        }
    }

    /**
     * @return the timeSeries
     */
    public TimeSeriesCollection getTimeSeries() {
        return timeSeries;
    }

    /**
     * @return the dataset
     */
    public TranslatingXYDataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(TranslatingXYDataset dataset) {
        this.dataset = dataset;
    }
}
