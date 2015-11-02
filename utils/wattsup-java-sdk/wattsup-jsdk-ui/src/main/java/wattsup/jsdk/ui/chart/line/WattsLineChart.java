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
package wattsup.jsdk.ui.chart.line;

import java.util.List;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.ui.ChartPanelSupport;
import wattsup.jsdk.ui.LineChartPanelSupport;
import wattsup.jsdk.ui.data.TranslatingXYDataset;

public class WattsLineChart extends LineChartPanelSupport<List<WattsUpPacket>>
{

    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -705459046389814891L;

    /**
     * @param data
     *            The data to be plotted.
     */
    public WattsLineChart(List<WattsUpPacket> data)
    {
        super("Watts", "", data, ChartPanelSupport.DEFAULT_DELAY);
        setRangeAxisRange(0, 80);
    }

    @Override
    protected void createSeries()
    {
        if (this.getSeries().size() < 1)
        {
            this.getTimeSeries().addSeries(new TimeSeries("Watts"));
            this.setDataset(new TranslatingXYDataset(this.getTimeSeries()));
        }
    }

    @Override
    public void update()
    {
        if (this.getSeries().size() < 1)
        {
            return;
        }
        
        if (getData() != null && !getData().isEmpty())
        {
            ((TimeSeries) this.getSeries().get(0)).add(new Millisecond(), Double.parseDouble(getData().get(0).getFields()[0].getValue()) / 10);
        }
    }
}
