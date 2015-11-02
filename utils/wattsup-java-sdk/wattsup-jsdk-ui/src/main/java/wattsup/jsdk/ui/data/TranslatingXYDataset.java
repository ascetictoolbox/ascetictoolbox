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
package wattsup.jsdk.ui.data;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

public final class TranslatingXYDataset extends AbstractXYDataset implements XYDataset, DatasetChangeListener
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = 4267888346871596411L;

    /**
     * The reference to the dataset.
     */
    private final XYDataset dataset_;

    /**
     * The translate value to be used by this {@link TranslatingXYDataset}.
     */
    private volatile double translate_;

    /**
     * Creates an instance this {@link TranslatingXYDataset} using the given {@code dataset}.
     * @param dataset The reference for the dataset to be used by {@link TranslatingXYDataset}. Might not be <code>null</code>.
     */
    public TranslatingXYDataset(XYDataset dataset)
    {
        this.dataset_ = dataset;
        this.dataset_.addChangeListener(this);
        this.translate_ = 0.0D;
    }

    /**
     * Returns the current translate value of this {@link TranslatingXYDataset}.
     * @return The current translate value of this {@link TranslatingXYDataset}.
     */
    public double getTranslate()
    {
        return this.translate_;
    }

    /**
     * Sets a new {@code translate} and notifies all registered listeners that the dataset has changed.
     * 
     * @param translate The new translate value.
     */
    public void setTranslate(double translate)
    {
        this.translate_ = translate;
        fireDatasetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount(int series)
    {
        return this.dataset_.getItemCount(series);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getXValue(int series, int item)
    {
        return this.dataset_.getXValue(series, item) + this.translate_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number getX(int series, int item)
    {
        return new Double(getXValue(series, item));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number getY(int series, int item)
    {
        return new Double(getYValue(series, item));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getYValue(int series, int item)
    {
        return this.dataset_.getYValue(series, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeriesCount()
    {
        return this.dataset_.getSeriesCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getSeriesKey(int series)
    {
        return this.dataset_.getSeriesKey(series);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void datasetChanged(DatasetChangeEvent paramDatasetChangeEvent)
    {
        fireDatasetChanged();
    }
}
