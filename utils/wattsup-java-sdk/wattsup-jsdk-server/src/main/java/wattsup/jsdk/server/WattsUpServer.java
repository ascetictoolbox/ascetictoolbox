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
package wattsup.jsdk.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import wattsup.jsdk.core.meter.WattsUp;

public final class WattsUpServer implements Runnable
{
    /**
     * The port number, or 0 to use a port number that is automatically allocated.
     */
    private final int port_;

    /**
     * A flag to indicate if the server was started.
     */
    private volatile boolean started_;

    /**
     * 
     */
    private ServerSocket server_;

    /**
     * 
     */
    private final RequestHandler requestHandler_;

    /**
     * @param port
     *            The port to initialize the server. A port number of 0 means that the port number is automatically allocated.
     * @param wattsUp
     *            The {@link WattsUp} reference to read the power measurements.
     */
    public WattsUpServer(int port, WattsUp wattsUp)
    {
        this.port_ = port;
        this.requestHandler_ = new RequestHandler(wattsUp);
    }

    @Override
    public void run()
    {
        openConnection();

        while (started_)
        {
            try
            {
                Socket client = this.server_.accept();
                requestHandler_.handle(client);
            }
            catch (IOException e)
            {
                if (this.isStopped())
                {
                    return;
                }
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        
        this.requestHandler_.shutdown();
    }

    

    /**
     * Stop this server.
     */
    public synchronized void stop()
    {
        this.started_ = false;
        try
        {
            this.server_.close();
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * Returns <code>true</code> if the server socket is closed.
     * 
     * @return <code>true</code> if the server socket is closed.
     */
    public synchronized boolean isStopped()
    {
        return !this.started_;
    }

    /**
     * Creates a server socket, bound to a given port.
     * 
     * @throws RuntimeException
     *             If it's impossible to create the server socket.
     */
    private void openConnection()
    {
        try
        {
            this.server_ = new ServerSocket(port_);
            this.started_ = true;
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }
}
