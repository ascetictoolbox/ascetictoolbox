package eu.ascetic.utils;

import java.io.File;
import java.io.IOException;
import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.event.WattsUpMemoryCleanEvent;
import wattsup.jsdk.core.event.WattsUpStopLoggingEvent;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.listener.WattsUpMemoryCleanListener;
import wattsup.jsdk.core.listener.WattsUpStopLoggingListener;
import wattsup.jsdk.core.meter.WattsUp;

public final class WattsUpLanConfigurationTool {

    private static WattsUp meter;
    public static final File CONFIG_FILE = new File("NetworkConfig.ini");
    //Default network configuration values
    private static String ipAddress = "192.168.0.112";
    private static String gateway = "192.168.0.1";
    private static String nameServer = "192.168.0.1";
    private static String nameServer2 = "0.0.0.0";
    private static String netmask = "255.255.255.0";
    private static boolean useDHCP = true;
    //Extended Network configuration
    private static String host = "data.wattsupmeters.com";
    private static int port = 80;
    private static String postFile = "/remote/netlog.php";
    private static String userAgent = "WattsUp.NET";
    private static int interval = 20;

    /**
     * Connects to a WattsUp meter and sets the network configuration
     * information, ready for outputting data across a LAN.
     *
     * @param args The first argument should be the comp port in use. The
     * default is "COM9".
     * @throws IOException If the power meter is not connected.
     */
    public static void main(String[] args) throws IOException {
        //start of establishing settings
        Settings settings = new Settings(CONFIG_FILE);
        String comPort = "COM9";
        if (args.length > 0) {
            comPort = args[0];
            System.out.println("Using the default COM9");
        }
        //Setting default network configuration values from file
        ipAddress = settings.getString("ipAddress", ipAddress);
        gateway = settings.getString("gateway", gateway);
        nameServer = settings.getString("nameServer", nameServer);
        nameServer2 = settings.getString("nameServer2", nameServer2);
        netmask = settings.getString("netmask", netmask);
        useDHCP = settings.getBoolean("useDHCP", useDHCP);
        //Setting extended network configuration values from file
        host = settings.getString("host", host);
        port = settings.getInt("port", port);
        postFile = settings.getString("postFile", postFile);
        userAgent = settings.getString("userAgent", userAgent);
        interval = settings.getInt("interval", interval);
        /**
         * The duration is to last 10 seconds. This should be more than enough
         * time to connect to the meter and set the network settings.
         */
        meter = new WattsUp(new WattsUpConfig().withPort(comPort).scheduleDuration(10).withInternalLoggingInterval(1).withExternalLoggingInterval(1));
        System.out.println("Welcome to the WattsUp Meter Configuration Tool");

        meter.registerListener(new WattsUpMemoryCleanListener() {
            @Override
            public void processWattsUpReset(WattsUpMemoryCleanEvent event) {
                System.out.println("Memory Just Cleaned");
            }
        });

        meter.registerListener(new WattsUpStopLoggingListener() {
            @Override
            public void processStopLogging(WattsUpStopLoggingEvent event) {
                System.out.println("Logging Stopped");
            }
        });

        meter.registerListener(new WattsUpDisconnectListener() {
            @Override
            public void onDisconnect(WattsUpDisconnectEvent event) {
                System.out.println("Application Exiting Due to Disconnect");
                System.exit(0);
            }
        });

        System.out.println("WattsUp Meter Connecting");
        meter.connect(true);
        System.out.println("Changing LAN Configuration Settings");
        meter.setNetworkConfig(ipAddress, gateway, nameServer, nameServer2, netmask, useDHCP);
        meter.setExtendedNetworkConfig(host, port, postFile, userAgent, interval);
        meter.setLoggingModeTCP(1);

        System.out.println("WattsUp Meter Connected: " + meter.isConnected());
        if (settings.isChanged()) {
            settings.save(CONFIG_FILE);
        }
    }
}