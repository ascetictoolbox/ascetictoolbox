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
package wattsup.jsdk.core.data.command;

public enum WattsUpCommand
{
    /**
     * Chosen fields request.
     */
    REQUEST_CHOSEN_FIELDS("#C,R", "Chosen fields request.", "Chosen fields now set to be logged. Each argument is either 0 or 1."),

    /***
     * Request all data logged in the WattsUp data memory.
     */
    REQUEST_ALL_DATA_LOGGED("#D,R", 0, true, true, "Request all data logged in the WattsUp data memory.",
            "Data Preamble Record. This preamble record is sent before the main data records. " + 
            "The non-chosen fields are reported as \"_\" (underscore). Last record of data transfer."),

    /**
     * Request WattsUp calibration factors.
     */
    REQUEST_CALIBRATION_FACTORS("#F,R", "Request WattsUp calibration factors.", "All calibration factor values."),

    /**
     * Request the header record.
     */
    REQUEST_HEADER_RECORD("#H,R", "Request the header record.", " Header record with the chosen field."),

    /**
     * Request the record count limit. THE limit is an {@link Integer} that may be approximately 1,700 - 262,000 depending upon choices and model
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
     * Configure the list of fields to be logged from now on. Clears logging memory, even if change was "no change"
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
     * Configure the sampling interval in seconds. It clears the logging memory, even if change was "no change".
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
     * Flag to indicate if the execution of the command clean the memory of the meter.
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
     * @param commands
     *            Valid commands to be executed in the meter. The commands must be separated by comma. Might not be <code>null</code> of empty.
     * @param numOfArgs
     *            The number of arguments required by the command. Might be greater than zero.
     * @param cleanMemory
     *            A flag to indicate if the execution of the command implies in clear the memory.
     * @param waitResp A flag to indicate if there is specific response for this command.
     * @param desc
     *            A description for the command.
     * @param replyDesc
     *            A description about the command reply.
     */
    private WattsUpCommand(String commands, int numOfArgs, boolean cleanMemory, boolean waitResp, String desc, String replyDesc)
    {
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
     * @param commands
     *            Valid commands to be executed in the meter. The commands must be separated by comma. Might not be <code>null</code> of empty.
     * @param description
     *            A description for the command.
     * @param replyDescription
     *            A description about the command reply.
     */
    private WattsUpCommand(String commands, String description, String replyDescription)
    {
        this(commands, 0, false, false, description, replyDescription);
    }

    /**
     * Creates an instance of this enumeration.
     * 
     * @param commands
     *            Valid commands to be executed in the meter. The commands must be separated by comma. Might not be <code>null</code> of empty.
     * @param numOfArguments
     *            The number of arguments required by the command. Might be greater than zero.
     * @param description
     *            A description for the command.
     * @param replyDescription
     *            A description about the command reply.
     */
    private WattsUpCommand(String commands, int numOfArguments, String description, String replyDescription)
    {
        this(commands, numOfArguments, false, false, description, replyDescription);
    }

    /**
     * @return the commands
     */
    public String getCommands()
    {
        return commands_;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description_;
    }

    /**
     * @return the replyDescription
     */
    public String getReplyDescription()
    {
        return replyDescription_;
    }

    /**
     * @return the cleanMemory
     */
    public boolean clearMemory()
    {
        return cleanMemory_;
    }

    /**
     * @return the waitResponse_
     */
    public boolean waitResponse()
    {
        return waitResponse_;
    }

    /**
     * @return the numberOfArguments
     */
    public int getNumberOfArguments()
    {
        return numberOfArguments_;
    }
}
