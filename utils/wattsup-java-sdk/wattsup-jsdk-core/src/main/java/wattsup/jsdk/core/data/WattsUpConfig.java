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

public final class WattsUpConfig
{
    /**
     * The default logging intervals.
     */
    private static final int ONE_SECOND = 1;

    /**
     * The fields' delimiter. The default is a comma (,).
     */
    private Delimiter delimiter_ = Delimiter.COMMA;

    /**
     * The external logging interval in seconds. The default is one second.
     */
    private int externalLoggingInterval_ = ONE_SECOND;

    /**
     * The internal logging interval in seconds. The default is one second.
     */
    private int internalLoggingInterval_ = ONE_SECOND;

    /**
     * The serial port where is connected the meter.
     */
    private String port_;

    /**
     * The duration to collect the data.
     */
    private long scheduleDurationInSeconds_;

    /**
     * Assign a {@link Delimiter} to be used by the power meter.
     * 
     * @param delimiter
     *            An instance for the {@link Delimiter} to be used.
     * @return The same instance but configured with the given {@link Delimiter}.
     */
    public WattsUpConfig withDelimiter(Delimiter delimiter)
    {
        this.delimiter_ = delimiter;
        return this;
    }

    /**
     * Configure this {@link WattsUpConfig} instance to use {@code port}.
     * 
     * @param port
     *            The path for the serial port. Might not be <code>null</code> or empty.
     * @return The same instance but configured to use the {@code port}.
     */
    public WattsUpConfig withPort(String port)
    {
        this.port_ = port;
        return this;
    }

    /**
     * Configure the sampling internal logging interval.
     * 
     * @param intervalInSeconds
     *            The interval in seconds. Must be greater than zero.
     * @return The same instance but configured the sampling interval logging with the given value.
     */
    public WattsUpConfig withInternalLoggingInterval(int intervalInSeconds)
    {
        this.internalLoggingInterval_ = intervalInSeconds;
        return this;
    }

    /**
     * Configure the sampling external logging interval.
     * 
     * @param intervalInSeconds
     *            The interval in seconds. Must be greater than zero.
     * @return The same instance but configured the sampling external logging with the given value.
     */
    public WattsUpConfig withExternalLoggingInterval(int intervalInSeconds)
    {
        this.externalLoggingInterval_ = intervalInSeconds;
        return this;
    }

    /**
     * Configure the duration to collect data from the meter.
     * 
     * @param intervalInSeconds
     *            The interval in seconds to collect the data.
     * @return The same instance but configured the {@code scheduleDurationInSeconds} with the given value.
     */
    public WattsUpConfig scheduleDuration(long intervalInSeconds)
    {
        this.scheduleDurationInSeconds_ = intervalInSeconds;
        return this;
    }

    /**
     * @return the delimiter
     */
    public Delimiter getDelimiter()
    {
        return delimiter_;
    }

    /**
     * @return the externalLoggingInterval
     */
    public int getExternalLoggingInterval()
    {
        return externalLoggingInterval_;
    }

    /**
     * @return the port_
     */
    public String getPort()
    {
        return port_;
    }

    /**
     * @return the internalLoggingInterval
     */
    public int getInternalLoggingInterval()
    {
        return internalLoggingInterval_;
    }

    /**
     * @return the scheduleDurationInSeconds
     */
    public long getScheduleDurationInSeconds()
    {
        return scheduleDurationInSeconds_;
    }

    public static enum Delimiter
    {
        /**
         * The comma delimiter.
         */
        COMMA(","), 
        
        /**
         * The semicolon delimiter.
         */
        SEMICOLON(";");

        /**
         * The delimiter symbol.
         */
        private final String symbol_;

        /**
         * @param symb The delimiter symbol for the enum.
         */
        private Delimiter(String symb)
        {
            this.symbol_ = symb;
        }

        /**
         * @return the symbol
         */
        public String getSymbol()
        {
            return symbol_;
        }

        /**
         * Factory method 
         * @param sym
         *            The delimiter.
         * @return The enumeration's instance. 
         * @throws IllegalArgumentException
         *             If the given symbol is unknown by this enumeration.
         */
        public static Delimiter valueOfFrom(String sym)
        {
            for (Delimiter dem : Delimiter.values())
            {
                if (dem.getSymbol().equals(sym))
                {
                    return dem;
                }
            }
            throw new IllegalArgumentException("Unknown delimiter " + sym);
        }
    }
}
