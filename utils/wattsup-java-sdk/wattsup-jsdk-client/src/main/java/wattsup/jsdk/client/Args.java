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
package wattsup.jsdk.client;

import java.io.File;

import wattsup.jsdk.client.jcommander.converter.CommandTypeConverter;
import wattsup.jsdk.client.jcommander.converter.IDConverter;
import wattsup.jsdk.client.jcommander.validator.RemoteCommandNameValidator;
import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.remote.data.CommandType;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

public final class Args
{
    /**
     * 
     */
    @Parameter(names = { "-host", "-server" }, description = "The server name to connect to.", required = true)
    private String host_;

    /**
     * 
     */
    @Parameter(names = { "-p", "-port" }, description = "The server port.", required = true)
    private int port_;

    @Parameter(names = { "-id", "-req-id" }, converter = IDConverter.class)
    private ID id_;

    /**
     * 
     */
    @Parameter(names = { "-method", "-name", "-region" })
    private String name_;

    /**
     * 
     */
    @Parameter(names = { "-c", "-cmd", "-command" }, required = true, converter = CommandTypeConverter.class, validateValueWith = RemoteCommandNameValidator.class)
    private CommandType command_;

    /**
     * 
     */
    @Parameter(names = { "-of", "-output-format" }, description = "The output format. The valid values are:[CSV, JSON, PLAIN].")
    private OutputFormat outputFormat_ = OutputFormat.CSV;

    /**
     * 
     */
    @Parameter(names = { "-file", "-output-file", "-out" }, converter = FileConverter.class)
    private File output_;

    /**
     * 
     */
    @Parameter(names = { "-timeout" }, description = "Timeout in milliseconds to wait for a response. Default is one minute.")
    private int timeout_ = 1 * 60 * 1000; // one minute

    

    /**
     * Returns the {@link OutputFormat}.
     * 
     * @return the {@link OutputFormat}.
     */
    public OutputFormat getOutputFormat()
    {
        return outputFormat_;
    }

    /**
     * Returns the command to execute.
     * 
     * @return the command to execute.
     */
    public CommandType getCommand()
    {
        return command_;
    }

    /**
     * Returns the {@link File} to write the data.
     * 
     * @return The {@link File} to write the data.
     */
    public File getOutputFile()
    {
        return output_;
    }

    /**
     * @return the host_
     */
    public String getHost()
    {
        return host_;
    }

    /**
     * @return the port_
     */
    public int getPort()
    {
        return port_;
    }

    /**
     * @return the id_
     */
    public ID getId()
    {
        return id_;
    }

    /**
     * @return the name_
     */
    public String getName()
    {
        return name_;
    }

    /**
     * @return the timeout_
     */
    public int getTimeout()
    {
        return timeout_;
    }
}
