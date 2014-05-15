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
package wattsup.jsdk.core.meter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.event.WattsUpConnectedEvent;
import wattsup.jsdk.core.event.WattsUpDataAvailableEvent;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.event.WattsUpEvent;
import wattsup.jsdk.core.event.WattsUpMemoryCleanEvent;
import wattsup.jsdk.core.event.WattsUpStopLoggingEvent;
import wattsup.jsdk.core.exception.WattsUpException;
import wattsup.jsdk.core.listener.WattsUpListener;

import static wattsup.jsdk.core.data.command.WattsUpCommand.CLEAR_MEMORY;
import static wattsup.jsdk.core.data.command.WattsUpCommand.CHANGE_LOGGING_MODE;
import static wattsup.jsdk.core.data.command.WattsUpCommand.SET_BASIC_NETWORK_CONFIG;
import static wattsup.jsdk.core.data.command.WattsUpCommand.SET_EXTENDED_NETWORK_CONFIG;
import static wattsup.jsdk.core.data.command.WattsUpCommand.CONFIGURE_EXTERNAL_LOGGING_INTERVAL;
import static wattsup.jsdk.core.data.command.WattsUpCommand.CONFIGURE_INTERNAL_LOGGING_INTERVAL;
import static wattsup.jsdk.core.data.command.WattsUpCommand.REQUEST_ALL_DATA_LOGGED;
import static wattsup.jsdk.core.data.command.WattsUpCommand.STOP_LOGGING;

/**
 * Class to interact with the Watts Up? power meter. To use it, it's necessary:
 *
 * <ul>
 * <li>Creates an instance. Call the constructor
 * {@link #WattsUp(WattsUpConfig)};</li>
 * <li>Register a
 * {@link wattsup.jsdk.core.listener.WattsUpDataAvailableListener} listener to
 * be notified when data (measure) are available. Call the method
 * {@link #registerListener(WattsUpListener)}</li>
 * <li>Connect to the meter. Call method {@link #connect()};</li>
 * <li>Disconnect after you finish the work/experiment. Call the method
 * {@link #disconnect()}.</li>
 * </ul>
 *
 * <br />
 * <strong>Usage Example</strong>: Here is a class that connect to the power
 * meter during three minutes and print the measures to the console.
 *
 * <pre>
 * {@code
 * public class WattsUpTest
 * {
 *
 * private static final long THREE_MINUTES = 3 * 60;
 *
 * public static void main(String[] args) throws IOException
 * {
 *    final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
 *    final WattsUp meter = new WattsUp(new WattsUpConfig().withPort(args[0]).scheduleDuration(THREE_MINUTES));
 *
 *    meter.registerListener(new WattsUpDataAvailableListener()
 *    {
 *      {@literal @}Override
 *      public void processDataAvailable(final WattsUpDataAvailableEvent event)
 *      {
 *            WattsUpPacket[] values = event.getValue();
 *            System.out.printf("[%s] %s\n", format.format(new Date()), Arrays.toString(values));
 *      }
 *    });
 *    meter.connect();
 *   }
 * }
 * </pre>
 */
public final class WattsUp {

    /**
     * The logging strategy of this class.
     */
    private static final Logger LOG = Logger.getLogger(WattsUp.class.getName());
    /**
     * The listeners registered for this {@link WattsUp} meter.
     */
    private final List<WattsUpListener> listeners_ = new LinkedList<>();
    /**
     * Cache with the mapping of event, listener and its method to be executed
     * when a given event happens.
     */
    private final Map<Class<WattsUpEvent<?>>, WattsUpEventInfo> eventListenerMap_ = new WeakHashMap<>();
    /**
     * The configuration to be used by the meter.
     */
    private final WattsUpConfig config_;
    /**
     * The scheduler used to notify the clients about data available. The
     * interval of the notification is determined through {@link WattsUpConfig}
     * or the one given by the method
     * {@link #configureExternalLoggingInterval(int)}.
     */
    private ScheduledExecutorService scheduler_;
    /**
     * The executor to notify the listener about the events.
     */
    private ScheduledExecutorService listenerNotifyExecutor_ = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    /**
     * The reference for the {@link WattsUpConnection} to execute the commands.
     */
    private WattsUpConnection connection_;
    /**
     * A flag to indicate if the meter is ready.
     */
    private volatile boolean configured_;
    /**
     * Flag to indicate if this meter is connected.
     */
    private volatile boolean connected_;

    /**
     * Creates an instance of this {@link WattsUp} meter.
     *
     * @param config The configuration of the meter.
     */
    public WattsUp(final WattsUpConfig config) {
        this.config_ = config;
    }

    /**
     * Register a {@link WattsUpListener} to be notified when the event that it
     * is interesting happened.
     *
     * @param listener The instance for the {@link WattsUpListener} to be
     * registered. Might not be <code>null</code>.
     * @throws NullPointerException If the listener is <code>null</code>.
     */
    public void registerListener(WattsUpListener listener) {
        if (listener == null) {
            throw new NullPointerException("The listener might not be null!");
        }
        this.listeners_.add(listener);
    }

    /**
     * Returns
     * <code>true</code> if the {@link WattsUpListener} reference was removed of
     * <code>false</code> otherwise.
     *
     * @param listener The reference to the {@link WattsUpListener} to be
     * removed.
     * @return <code>true</code> if the {@link WattsUpListener} reference was
     * removed of <code>false</code> otherwise.
     */
    public boolean unregisterListener(WattsUpListener listener) {
        boolean removed = false;

        if (listener != null) {
            removed = this.listeners_.remove(listener);
        }

        return removed;
    }

    /**
     * Connect to the meter. After connect the meter is reseted.
     *
     * @throws IOException If the power meter is not available.
     * @see #connect(boolean)
     * @see #reset()
     */
    public void connect() throws IOException {
        connect(true);
    }

    /**
     * Connect to the power meter and reset the memory if configured.
     *
     * @param reset Flag to indicates if the meter should be reseted.
     * @throws IOException If the power meter is not available.
     * @see #reset()
     */
    public void connect(boolean reset) throws IOException {
        connection_ = new WattsUpConnection(this.config_);

        if (connection_.connect()) {
            configure();

            if (reset) {
                this.reset();
            }

            notify(new WattsUpConnectedEvent(this));
            this.connected_ = true;
        }

        start();
    }

    /**
     * Configure the device parameters.
     *
     * @throws IOException If the device is not connected.
     */
    private void configure() throws IOException {
        configureExternalLoggingInterval(this.config_.getExternalLoggingInterval());
        configured_ = true;
    }

    /**
     * Returns
     * <code>true</code> if the meter is online (connected).
     *
     * @return <code>true</code> if the meter is online (connected).
     */
    public boolean isConnected() {
        return this.connected_ && this.connection_.isConnected();
    }

    /**
     * Disconnect from the power meter.
     *
     * @throws IOException If the meter is disconnected.
     */
    public void disconnect() throws IOException {
        if (connection_ != null) {
            this.scheduler_.shutdownNow();

            this.stop();
            this.connection_.disconnect();

            this.configured_ = false;
            this.connected_ = false;

            notify(new WattsUpDisconnectEvent(this, System.currentTimeMillis()));

            List<Runnable> waitingTasks = null;

            try {
                if (!listenerNotifyExecutor_.awaitTermination(5, SECONDS)) {
                    waitingTasks = listenerNotifyExecutor_.shutdownNow();
                }
            } catch (InterruptedException ie) {
                waitingTasks = listenerNotifyExecutor_.shutdownNow();
            }

            if (LOG.isLoggable(Level.INFO) && waitingTasks != null && !waitingTasks.isEmpty()) {
                LOG.info(String.format("There was/were %s events(s) waiting to be notified!", waitingTasks.size()));
            }
        }
    }

    /**
     * This sets the WattsUp meter to log data out continously to the serial
     * port. This sends the command #L,W,3,E,0,interval; to the WattsUp meter.
     * 
     * Note: The command connect should have been called first.
     * 
     * This method was added as part of the ASCETiC project.
     *
     * @param interval The time interval between logging data values.
     * @throws IOException If the communication with the meter is not possible.
     */
    public void setLoggingModeSerial(int interval) throws IOException {
        this.connection_.execute(CHANGE_LOGGING_MODE, "E", "0", String.valueOf(interval));
    }

    /**
     * This sets the WattsUp meter to log data out in TCP mode. This sends the
     * command #L,W,3,T,0,interval; to the WattsUp meter.
     * 
     * Note: The command connect should have been called first.
     * 
     * This method was added as part of the ASCETiC project.
     * 
     * @param interval The time interval between logging data values.
     * @throws IOException If the communication with the meter is not possible.
     */
    public void setLoggingModeTCP(int interval) throws IOException {
        this.connection_.execute(CHANGE_LOGGING_MODE, "T", "0", String.valueOf(interval));
    }

    /**
     * This sets the WattsUp meter to log data to its internal data store. This
     * sends the command #L,W,3,I,0,interval; to the WattsUp meter.
     * 
     * Note: The command connect should have been called first.
     * 
     * This method was added as part of the ASCETiC project.
     *
     * @param interval The time interval between logging data values.
     * @throws IOException If the communication with the meter is not possible.
     */
    public void setLoggingModeInternal(int interval) throws IOException {
        this.connection_.execute(CHANGE_LOGGING_MODE, "I", "0", String.valueOf(interval));
    }

    /**
     * Sets the Basic Network/Internet Configuration: 
     * Sends the command #I,S,6,
     * , , , , , ; to the WattsUp meter.
     * 
     * Full Example of Command:
     * #I,S,6,
     * 192.168.0.112,
     * 192.168.0.1,
     * 192.168.0.1,
     * 0.0.0.0,
     * 255.255.255.0,
     * 1;
     * 
     * Note: The command connect should have been called first.
     * 
     * This method was added as part of the ASCETiC project.
     *
     * @param ipAddress IP address in octet format.
     * @param gateway IP address in octet format.
     * @param nameServer IP address in octet format.
     * @param nameServer2 IP address in octet format.
     * @param netmask Mask as IP address in octet format.
     * @param useDHCP If the WattsUp Meter should use DHCP or not.
     * @throws IOException If the communication with the meter is not possible.
     */
    public void setNetworkConfig(String ipAddress, String gateway,
            String nameServer, String nameServer2,
            String netmask, boolean useDHCP) throws IOException {
        String doDHCPStr = (useDHCP ? "1" : "0");
        this.connection_.execute(SET_BASIC_NETWORK_CONFIG, ipAddress, gateway, nameServer, nameServer2, netmask, doDHCPStr);
    }

    /**
     * Sets the Extended Network/Internet Configuration:
     * Sends the command 
     *  #I,X,5,<Post Host>,<Post Port>,<Post Address>,<Post File>,
     * <User Agent>,<Post Interval>; to the WattsUp meter.
     * 
     * Note: The command connect should have been called first.
     * 
     * This method was added as part of the ASCETiC project.
     *
     * @param host URL/IP Address as String, MAX LENGTH 40
     * @param port Unsigned 16-bit Integer
     * @param postFile URI as String, MAX LENGTH 80
     * @param userAgent URL/IP Address as String, DEFAULT: 'WattsUp.NET'
     * @param interval Time in seconds as 32-bit signed integer
     * @throws IOException 
     */
    public void setExtendedNetworkConfig(String host, int port,
            String postFile, String userAgent, int interval) throws IOException {
        this.connection_.execute(SET_EXTENDED_NETWORK_CONFIG, host, String.valueOf(port), 
                postFile, userAgent, String.valueOf(interval));
    }

    /**
     * Clear the memory of the meter.
     *
     * @throws IOException If the communication with the meter is not possible.
     */
    public void reset() throws IOException {
        this.connection_.execute(CLEAR_MEMORY);
        notify(new WattsUpMemoryCleanEvent(this, System.currentTimeMillis()));
    }

    /**
     * Retrieve and returns all data available in the meter.
     *
     * @return A non-null array with the all data available in the meter.
     * @throws IOException If is not possible to retrieve the data.
     */
    public WattsUpPacket[] records() throws IOException {
        return this.connection_.execute(REQUEST_ALL_DATA_LOGGED);
    }

    /**
     * Configure the internal logging interval to this meter.
     *
     * @param interval The interval in seconds to be set. Might be greater than
     * zero.
     * @throws IOException If the communication with the meter is not possible.
     */
    public void configureInternalLoggingInterval(int interval) throws IOException {
        this.connection_.execute(CONFIGURE_INTERNAL_LOGGING_INTERVAL, "I", String.valueOf(interval), String.valueOf(interval));
    }

    /**
     * Start up logging with the given {@code interval}.
     *
     * @param interval The interval in seconds to configure the external
     * logging.
     * @throws IOException If it is not possible communicating with the meter.
     */
    public void configureExternalLoggingInterval(int interval) throws IOException {
        this.connection_.execute(CONFIGURE_EXTERNAL_LOGGING_INTERVAL, "E", String.valueOf(interval), String.valueOf(interval));
    }

    /**
     * Stop logging this meter events.
     *
     * @throws IOException If it is not possible communicating with the meter.
     */
    public void stop() throws IOException {
        this.connection_.execute(STOP_LOGGING);
        notify(new WattsUpStopLoggingEvent(this, System.currentTimeMillis()));
    }

    /**
     * Start up logging with the given {@code interval}.
     */
    protected void start() {
        scheduler_ = Executors.newScheduledThreadPool(1);
        final ScheduledFuture<?> handler = scheduler_
                .scheduleAtFixedRate(new WattsUpStreamReader(), 1, config_.getExternalLoggingInterval(), SECONDS);

        if (config_.getScheduleDurationInSeconds() > 0) {
            scheduler_.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        handler.cancel(true);
                        disconnect();
                    } catch (IOException ignore) {
                        LOG.log(Level.WARNING, ignore.getMessage(), ignore);
                    }
                }
            }, config_.getScheduleDurationInSeconds(), SECONDS);
        }
    }

    /**
     * Notifies the correspondent listener about an {@link WattsUpEvent} event.
     *
     * @param event The reference to the {@link WattsUpEvent} to be notified.
     * Might not be <code>null</code>.
     * @param <T> The event's data type.
     */
    @SuppressWarnings("unchecked")
    private <T> void notify(final WattsUpEvent<T> event) {
        listenerNotifyExecutor_.execute(new Runnable() {
            @Override
            public void run() {
                for (WattsUpListener listener : listeners_) {
                    if (event.isAppropriateListener(listener)) {
                        WattsUpEventInfo eventInfo = eventListenerMap_.get(event.getClass());

                        if (eventInfo == null) {
                            eventInfo = new WattsUpEventInfo();
                            eventListenerMap_.put((Class<WattsUpEvent<?>>) event.getClass(), eventInfo);
                        }

                        try {
                            Method method = eventInfo.getEventMethodFor(event, listener);
                            Objects.requireNonNull(method).invoke(listener, event);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                            throw new WattsUpException(exception.getMessage(), exception);
                        }
                    }
                }
            }
        });
    }

    private static final class WattsUpEventInfo {

        /**
         * The with the reference to method of the listener to be executed for a
         * given event.
         */
        private final Map<Class<?>, Method> eventListenerMap_ = new WeakHashMap<>();

        /**
         *
         * @param event The reference for the {@link WattsUpEvent}. Might not          * be <code>null</code>.
         * @param listener The reference to the {@link WattsUpListener} to find
         * the method to be executed for the {@code event}. Might not be
         * <code>null</code>.
         * @param <T> The event's data type.
         * @return The {@link Method} of {@code listener} that has just
         * {@code event} as parameter. The method is configured to be
         * accessible.
         */
        private <T> Method getEventMethodFor(final WattsUpEvent<T> event, final WattsUpListener listener) {
            Method method = eventListenerMap_.get(listener.getClass());

            if (method == null) {
                for (Method meth : listener.getClass().getDeclaredMethods()) {
                    if (meth.getParameterTypes() != null && meth.getParameterTypes().length == 1
                            && meth.getParameterTypes()[0].equals(event.getClass())) {
                        meth.setAccessible(true);
                        eventListenerMap_.put(listener.getClass(), meth);
                        method = meth;
                        break;
                    }
                }
            }
            return method;
        }
    }

    private class WattsUpStreamReader implements Runnable {

        @Override
        public void run() {
            try {
                if (isConnected() && configured_) {
                    final WattsUpPacket[] records = WattsUpPacket.parser(connection_.read(), config_.getDelimiter(), System.currentTimeMillis());

                    if (records.length > 0) {
                        WattsUp.this.notify(new WattsUpDataAvailableEvent(this, records));
                    }
                }
            } catch (IOException exception) {
                LOG.log(Level.INFO, exception.getMessage(), exception);
            }
        }
    }
}
