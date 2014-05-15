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
package wattsup.jsdk.core.data.command;

public enum WattsUpCommand {

    /**
     * Chosen fields request.
     */
    REQUEST_CHOSEN_FIELDS("#C,R", "Chosen fields request.", "Chosen fields now set to be logged. Each argument is either 0 or 1."),
    /**
     * *
     * Request all data logged in the WattsUp data memory.
     */
    REQUEST_ALL_DATA_LOGGED("#D,R", 0, true, true, "Request all data logged in the WattsUp data memory.",
    "Data Preamble Record. This preamble record is sent before the main data records. "
    + "The non-chosen fields are reported as \"_\" (underscore). Last record of data transfer."),
    /**
     * Request WattsUp calibration factors.
     */
    REQUEST_CALIBRATION_FACTORS("#F,R", "Request WattsUp calibration factors.", "All calibration factor values."),
    /**
     * Request the header record.
     */
    REQUEST_HEADER_RECORD("#H,R", "Request the header record.", " Header record with the chosen field."),
    /**
     * Request the record count limit. THE limit is an {@link Integer} that may
     * be approximately 1,700 - 262,000 depending upon choices and model
     * features.
     */
    REQUEST_RECORD_COUNT_LIMIT("#N,R", "Request record count limit", "Record count limit using fields chosen in #C,W command."),
    /**
     * Request current sampling interval.
     */
    REQUEST_CURRENT_SAMPLING_INTERVAL("#S,R", "Request current sampling interval.", "The current sampling interval."),
    /**
     * Request the current meter version.
     */
    REQUEST_VERSION("#V,R", "Request the meter version.", "The current meter version."),
    /**
     * Configure the list of fields to be logged from now on. Clears logging
     * memory, even if change was "no change"
     */
    CONFIGURE_CHOSEN_FIELDS_TO_BE_LOGGED("#C,W", 18, true, true, "Choose list of fields to be logged from now on.",
    "Record count limit using these chosen fields. The limit is between 1,700 and 262,000."),
    /**
     * Set the external logging with a given interval.
     */
    CONFIGURE_EXTERNAL_LOGGING_INTERVAL("#L,W", 3, true, true, "Set the WattsUp to external Logging with a given interval", "Logging output records"),
    /**
     * Configure the internal logging interval of the WattsUp.
     */
    CONFIGURE_INTERNAL_LOGGING_INTERVAL("#L,W", 3, false, true, "Set the WattsUp to internal Logging with a given interval",
    "Interval used for logging."),
    /**
     * ASCETIC CHANGE FROM BASIC SDK Command to change to sending logging data
     * to: Serial continuously: "#L,W,3,E,<Reserved>,<Interval>", Mode: Serial:
     * #L,W,3,E,0,1; TCP: #L,W,3,T,0,1; Internal: #L,W,3,I,0,1;
     */
    CHANGE_LOGGING_MODE("#L,W,3", 3, false, true, "Changes the WattsUp meter mode of logging data.", ""),
   
    /**
     * A. Query Basic Network/Internet Configuration:
     * #i,q,7,<IP Address>,<Gateway>,<Name Server1>,<Name Server2>,
     *      <Net Mask>,<Do DHCP>,<MAC Address>;
     *
     * 1. <IP Address> - IP address in octet format.
     * 2. <Gateway> - IP address in octet format.
     * 3. <Name Server1> - IP address in octet format.
     * 4. <Name Server2> - IP address in octet format.
     * 5. <Net Mask> - Mask as IP address in octet format.
     * 6. <Do DHCP> - Boolean - character '0' = False, character '1' = True
     * 7. <MAC Address> - Address as 48-bit, unsigned integer, READ-ONLY
     */
    GET_BASIC_NETWORK_CONFIG("#I,Q,7", 7, false, true, "Gets the WattsUP .NET meter's Network Settings.", ""),
    /**
     * B. Set Basic Network/Internet Configuration: 
     * #I,S,6,<IP Address>,<Gateway>,<Name Server1>,
     *      <Name Server2>,<Net Mask>,<Do DHCP>;
     *
     * 1. <IP Address> - IP address in octet format. 
     * 2. <Gateway> - IP address
     * in octet format. 
     * 3. <Name Server1> - IP address in octet format. 
     * 4. <Name Server2> - IP address in octet format. 
     * 5. <Net Mask> - Mask as IP address
     * in octet format. 
     * 6. <Do DHCP> - Boolean - character '0' = False, character '1' = True
     */
    SET_BASIC_NETWORK_CONFIG("#I,S,6", 6, false, true, "Sets the WattsUP .NET meter's Network Settings.", ""),
    /**
     * C. Query Extended Network/Internet Configuration:
     * #i,e,5,<Post Host>,<Post Port><Post File>,
     *              <User Agent>,<Post Interval>;
     * 
     * <Post Host> - URL/IP Address as String, MAX LENGTH 40
     * <Post Port> - Unsigned 16-bit Integer
     * <Post File> - URI as String, MAX LENGTH 80
     * <User Agent> - URL/IP Address as String, DEFAULT: 'WattsUp.NET'
     * <Post Interval> - Time in seconds as 32-bit signed integer
     */
    GET_EXTENDED_NETWORK_CONFIG("#I,E,5", 5, false, true, "Gets the WattsUP .NET meter's Extended Network Settings.", ""),
    /**
     * D. Set Extended Network/Internet Configuration: 
     * #I,X,5,<Post Host>,<Post Port>,<Post Address>,<Post File>,
     *      <User Agent>,<Post Interval>;
     * 
     * <Post Host> - URL/IP Address as String, MAX LENGTH 40
     * <Post Port> - Unsigned 16-bit Integer
     * <Post File> - URI as String, MAX LENGTH 80
     * <User Agent> - HTTP Client name as String;
     * <Post Interval> - Time in seconds as 32-bit signed integer
     */
    SET_EXTENDED_NETWORK_CONFIG("#I,X,5", 5, false, true, "Sets the WattsUP .NET meter's Extended Network Settings.", ""),
    /**
     * Configure the sampling interval in seconds. It clears the logging memory,
     * even if change was "no change".
     */
    CONFIGURE_SAMPLING_INTERVAL("#S,W", 2, true, false, "Configure the sampling interval", ""),
    /**
     * Configure the request record count limit.
     */
    CLEAR_MEMORY("#R,W", "Reset (clear) the WattsUp data memory.", ""),
    /**
     * Stop logging and read time stamp.
     */
    STOP_LOGGING("#L,R", "Stop logging and read time stamp", "");
    /**
     * The meter commands (command, sub-command).
     */
    private final String commands_;
    /**
     * The command's description.
     */
    private final String description_;
    /**
     * The command's reply description.
     */
    private final String replyDescription_;
    /**
     * Flag to indicate if the execution of the command clean the memory of the
     * meter.
     */
    private final boolean cleanMemory_;
    /**
     * Flag to indicate if there is a response for the command execution.
     */
    private final boolean waitResponse_;
    /**
     * The number of arguments required by the command.
     */
    private final int numberOfArguments_;

    /**
     * Creates an instance of this enumeration.
     *
     * @param commands Valid commands to be executed in the meter. The commands
     * must be separated by comma. Might not be <code>null</code> of empty.
     * @param numOfArgs The number of arguments required by the command. Might
     * be greater than zero.
     * @param cleanMemory A flag to indicate if the execution of the command
     * implies in clear the memory.
     * @param waitResp A flag to indicate if there is specific response for this
     * command.
     * @param desc A description for the command.
     * @param replyDesc A description about the command reply.
     */
    private WattsUpCommand(String commands, int numOfArgs, boolean cleanMemory, boolean waitResp, String desc, String replyDesc) {
        this.commands_ = commands;
        this.numberOfArguments_ = numOfArgs;
        this.cleanMemory_ = cleanMemory;
        this.waitResponse_ = waitResp;
        this.description_ = desc;
        this.replyDescription_ = replyDesc;
    }

    /**
     * Creates an instance of this enumeration.
     *
     * @param commands Valid commands to be executed in the meter. The commands
     * must be separated by comma. Might not be <code>null</code> of empty.
     * @param description A description for the command.
     * @param replyDescription A description about the command reply.
     */
    private WattsUpCommand(String commands, String description, String replyDescription) {
        this(commands, 0, false, false, description, replyDescription);
    }

    /**
     * Creates an instance of this enumeration.
     *
     * @param commands Valid commands to be executed in the meter. The commands
     * must be separated by comma. Might not be <code>null</code> of empty.
     * @param numOfArguments The number of arguments required by the command.
     * Might be greater than zero.
     * @param description A description for the command.
     * @param replyDescription A description about the command reply.
     */
    private WattsUpCommand(String commands, int numOfArguments, String description, String replyDescription) {
        this(commands, numOfArguments, false, false, description, replyDescription);
    }

    /**
     * @return the commands
     */
    public String getCommands() {
        return commands_;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description_;
    }

    /**
     * @return the replyDescription
     */
    public String getReplyDescription() {
        return replyDescription_;
    }

    /**
     * @return the cleanMemory
     */
    public boolean clearMemory() {
        return cleanMemory_;
    }

    /**
     * @return the waitResponse_
     */
    public boolean waitResponse() {
        return waitResponse_;
    }

    /**
     * @return the numberOfArguments
     */
    public int getNumberOfArguments() {
        return numberOfArguments_;
    }
}
