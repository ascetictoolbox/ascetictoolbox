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
package wattsup.jsdk.core.data;

import java.io.Serializable;

public final class Measurement implements Serializable, Comparable<Measurement>
{
    /**
     * Serial code version <code>serialVersionUID</code> for serialization.
     */
    private static final long serialVersionUID = -2328787512863001602L;

    /**
     * Time in milliseconds when the measurement occurred.
     */
    private long time_;

    /**
     * Number of watts.
     */
    private double watts_;

    /**
     * Voltage (V).
     */
    private double volts_;

    /**
     * AC current in Amperes.
     */
    private double amps_;

    /**
     * The watts in KWh.
     */
    private double wattsKWh_;

    /**
     * Maximum watts read.
     */
    private double maxWatts_;

    /**
     * Maximum voltage.
     */
    private double maxVolts_;

    /**
     * Maximum AC current.
     */
    private double maxAmps_;

    /**
     * Minimum watts value.
     */
    private double minWatts_;

    /**
     * Minimum voltage read.
     */
    private double minVolts_;

    /**
     * Minimum AC current read.
     */
    private double minAmps_;

    /**
     * Power factor.
     */
    private double powerFactor_;

    /**
     * Power duty cycle.
     */
    private double dutyCycle_;

    /**
     * Power cycle (Hz).
     */
    private double powerCycle_;

    /**
     * Default constructor.
     */
    public Measurement()
    {
        super();
    }

    /**
     * @return the time
     */
    public long getTime()
    {
        return time_;
    }

    /**
     * @param time
     *            the time to set
     */
    public void setTime(long time)
    {
        this.time_ = time;
    }

    /**
     * @return the watts
     */
    public double getWatts()
    {
        return watts_;
    }

    /**
     * @param watts
     *            the watts to set
     */
    public void setWatts(double watts)
    {
        this.watts_ = watts;
    }

    /**
     * @return the volts
     */
    public double getVolts()
    {
        return volts_;
    }

    /**
     * @param volts
     *            the volts to set
     */
    public void setVolts(double volts)
    {
        this.volts_ = volts;
    }

    /**
     * @return the amps
     */
    public double getAmps()
    {
        return amps_;
    }

    /**
     * @param amps
     *            the amps to set
     */
    public void setAmps(double amps)
    {
        this.amps_ = amps;
    }

    /**
     * @return the wattsKWh
     */
    public double getWattsKWh()
    {
        return wattsKWh_;
    }

    /**
     * @param wattsKWh
     *            the wattsKWh to set
     */
    public void setWattsKWh(double wattsKWh)
    {
        this.wattsKWh_ = wattsKWh;
    }

    /**
     * @return the maxWatts
     */
    public double getMaxWatts()
    {
        return maxWatts_;
    }

    /**
     * @param maxWatts
     *            the maxWatts to set
     */
    public void setMaxWatts(double maxWatts)
    {
        this.maxWatts_ = maxWatts;
    }

    /**
     * @return the maxVolts
     */
    public double getMaxVolts()
    {
        return maxVolts_;
    }

    /**
     * @param maxVolts
     *            the maxVolts to set
     */
    public void setMaxVolts(double maxVolts)
    {
        this.maxVolts_ = maxVolts;
    }

    /**
     * @return the maxAmps
     */
    public double getMaxAmps()
    {
        return maxAmps_;
    }

    /**
     * @param maxAmps
     *            the maxAmps to set
     */
    public void setMaxAmps(double maxAmps)
    {
        this.maxAmps_ = maxAmps;
    }

    /**
     * @return the minWatts
     */
    public double getMinWatts()
    {
        return minWatts_;
    }

    /**
     * @param minWatts
     *            the minWatts to set
     */
    public void setMinWatts(double minWatts)
    {
        this.minWatts_ = minWatts;
    }

    /**
     * @return the minVolts
     */
    public double getMinVolts()
    {
        return minVolts_;
    }

    /**
     * @param minVolts
     *            the minVolts to set
     */
    public void setMinVolts(double minVolts)
    {
        this.minVolts_ = minVolts;
    }

    /**
     * @return the minAmps
     */
    public double getMinAmps()
    {
        return minAmps_;
    }

    /**
     * @param minAmps
     *            the minAmps to set
     */
    public void setMinAmps(double minAmps)
    {
        this.minAmps_ = minAmps;
    }

    /**
     * @return the powerFactor
     */
    public double getPowerFactor()
    {
        return powerFactor_;
    }

    /**
     * @param powerFactor
     *            the powerFactor to set
     */
    public void setPowerFactor(double powerFactor)
    {
        this.powerFactor_ = powerFactor;
    }

    /**
     * @return the dutyCycle
     */
    public double getDutyCycle()
    {
        return dutyCycle_;
    }

    /**
     * @param dutyCycle
     *            the dutyCycle to set
     */
    public void setDutyCycle(double dutyCycle)
    {
        this.dutyCycle_ = dutyCycle;
    }

    /**
     * @return the powerCycle
     */
    public double getPowerCycle()
    {
        return powerCycle_;
    }

    /**
     * @param powerCycle
     *            the powerCycle to set
     */
    public void setPowerCycle(double powerCycle)
    {
        this.powerCycle_ = powerCycle;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (time_ ^ (time_ >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Measurement other = (Measurement) obj;
        return time_ == other.time_;
    }

    @Override
    public int compareTo(Measurement other)
    {
        return Long.compare(this.getTime(), other.getTime());
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
}
