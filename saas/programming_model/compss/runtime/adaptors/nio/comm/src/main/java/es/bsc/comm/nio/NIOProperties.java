package es.bsc.comm.nio;

import es.bsc.comm.TransferManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class NIOProperties {

    private static final Logger LOGGER = LogManager.getLogger(TransferManager.LOGGER_NAME);

    public static final String MAX_BUFFERED_PACKETS_NAME = "MAX_BUFFERED_PACKETS";
    private static final int DEFAULT_MAX_BUFFERED_PACKETS = 10;
    private static int MAX_BUFFERED_PACKETS;

    public static final String MIN_BUFFERED_PACKETS_NAME = "MIN_BUFFERED_PACKETS";
    private static final int DEFAULT_MIN_BUFFERED_PACKETS = 5;
    private static int MIN_BUFFERED_PACKETS;

    public static final String PACKET_SIZE_NAME = "PACKET_SIZE";
    private static final int DEFAULT_PACKET_SIZE = 8192;
    private static int PACKET_SIZE;

    public static final String NETWORK_BUFFER_SIZE_NAME = "NETWORK_BUFFER_SIZE";
    private static final int DEFAULT_NETWORK_BUFFER_SIZE = 81920;
    private static int NETWORK_BUFFER_SIZE;

    public static final String MAX_SENDS_NAME = "MAX_SEND";
    private static final int DEFAULT_MAX_SENDS = Integer.MAX_VALUE;
    private static int MAX_SENDS;

    public static final String MAX_RECEIVES_NAME = "MAX_RECEIVES";
    private static final int DEFAULT_MAX_RECEIVES = Integer.MAX_VALUE;
    private static int MAX_RECEIVES;

    public static final String MAX_RETRIES_NAME = "MAX_RETRIES";
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static int MAX_RETRIES;

    private static final String PROP_FILE_NAME = "nio.cfg";


    private NIOProperties() {
        throw new NonInstantiableException("NIOProperties should not be instantiated");
    }

    public static void importProperties(String loc) {
        if (loc == null) {
            // Default parameters
            MAX_BUFFERED_PACKETS = DEFAULT_MAX_BUFFERED_PACKETS;
            PACKET_SIZE = DEFAULT_PACKET_SIZE;
            MIN_BUFFERED_PACKETS = DEFAULT_MIN_BUFFERED_PACKETS;
            NETWORK_BUFFER_SIZE = DEFAULT_NETWORK_BUFFER_SIZE;

            MAX_SENDS = DEFAULT_MAX_SENDS;
            MAX_RECEIVES = DEFAULT_MAX_RECEIVES;
            MAX_RETRIES = DEFAULT_MAX_RETRIES;
        } else {
            // File parameters
            FileInputStream inputStream;
            String path = loc + File.separator + PROP_FILE_NAME;
            try {
                inputStream = new FileInputStream(path);
            } catch (FileNotFoundException fnfe) {
                LOGGER.error("ERROR: Cannot find properties file " + path, fnfe);
                return;
            }

            try {
                Properties prop = new Properties();
                prop.load(inputStream);
                MAX_BUFFERED_PACKETS = Integer.parseInt(prop.getProperty(MAX_BUFFERED_PACKETS_NAME));
                MIN_BUFFERED_PACKETS = Integer.parseInt(prop.getProperty(MIN_BUFFERED_PACKETS_NAME));
                PACKET_SIZE = Integer.parseInt(prop.getProperty(PACKET_SIZE_NAME));
                NETWORK_BUFFER_SIZE = Integer.parseInt(prop.getProperty(NETWORK_BUFFER_SIZE_NAME));

                MAX_SENDS = Integer.parseInt(prop.getProperty(MAX_SENDS_NAME));
                MAX_RECEIVES = Integer.parseInt(prop.getProperty(MAX_RECEIVES_NAME));
                MAX_RETRIES = Integer.parseInt(prop.getProperty(MAX_RETRIES_NAME));
            } catch (IOException e) {
                LOGGER.error("Error loading properties", e);
            }
            try {
                inputStream.close();
            } catch (IOException ie) {
                LOGGER.error("Error closing stream", ie);
            }
        }
    }

    public static void printProperties() {
        LOGGER.info("MAX_BUFFERED_PACKETS = " + MAX_BUFFERED_PACKETS);
        LOGGER.info("MIN_BUFFERED_PACKETS = " + MIN_BUFFERED_PACKETS);
        LOGGER.info("PACKET_SIZE = " + PACKET_SIZE);
        LOGGER.info("NETWORK_BUFFER_SIZE = " + NETWORK_BUFFER_SIZE);
        LOGGER.info(MAX_BUFFERED_PACKETS_NAME + " = " + MAX_BUFFERED_PACKETS);
        LOGGER.info(MIN_BUFFERED_PACKETS_NAME + " = " + MIN_BUFFERED_PACKETS);
        LOGGER.info(PACKET_SIZE_NAME + " = " + PACKET_SIZE);
        LOGGER.info(NETWORK_BUFFER_SIZE_NAME + " = " + NETWORK_BUFFER_SIZE);

        LOGGER.info(MAX_SENDS_NAME + " = " + MAX_SENDS);
        LOGGER.info(MAX_RECEIVES_NAME + " = " + MAX_RECEIVES);
        LOGGER.info(MAX_RETRIES_NAME + " = " + MAX_RETRIES);
    }

    public static int getMaxBufferedPackets() {
        return MAX_BUFFERED_PACKETS;
    }

    public static int getMinBufferedPackets() {
        return MIN_BUFFERED_PACKETS;
    }

    public static int getPacketSize() {
        return PACKET_SIZE;
    }

    public static int getNetworkBufferSize() {
        return NETWORK_BUFFER_SIZE;
    }

    public static int getMaxSends() {
        return MAX_SENDS;
    }

    public static int getMaxReceives() {
        return MAX_RECEIVES;
    }

    public static int getMaxRetries() {
        return MAX_RETRIES;
    }

}
