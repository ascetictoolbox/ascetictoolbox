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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import wattsup.jsdk.core.data.ID;
import wattsup.jsdk.core.data.WattsUpPacket;
import wattsup.jsdk.core.exception.WattsUpException;
import wattsup.jsdk.core.meter.WattsUp;
import wattsup.jsdk.remote.data.Request;
import wattsup.jsdk.remote.data.Response;
import wattsup.jsdk.server.memory.OffHeapMemory;

public class RequestHandler
{
    /**
     * 
     */
    private static final Logger LOG = Logger.getLogger(RequestHandler.class.getName());
    /**
     * The storage to use when one was not specified.
     */
    private static final String GLOBAL_STORAGE_NAME = "wattsup-measurements";

    /**
     * The thread pool.
     */
    private final ExecutorService executor_ = Executors.newCachedThreadPool();

    /**
     * 
     */
    private final Map<ID, Worker> workers_ = new ConcurrentHashMap<ID, Worker>();

    /**
     * The reference to the {@link WattsUp} to read the data.
     */
    private final WattsUp wattsUp_;

    /**
     * 
     */
    private final OffHeapMemory<WattsUpPacket> memory_ = new OffHeapMemory<WattsUpPacket>();

    /**
     * 
     * @param wattsUp
     *            The {@link WattsUp} reference to read the power measurements.
     */
    public RequestHandler(WattsUp wattsUp)
    {
        this.wattsUp_ = wattsUp;
    }

    /**
     * 
     * @param client
     *            Socket reference. Might not be <code>null</code>.
     * @throws IOException
     *             If the request is invalid.
     */
    public void handle(final Socket client) throws IOException
    {
        final Request request = getClientRequest(client.getInputStream());
        final Worker worker;

        if (request.getName() == null || request.getName().trim().isEmpty())
        {
            request.withName(GLOBAL_STORAGE_NAME);
        }

        switch (request.getCommand())
        {
        case CLEAR:
            memory_.clear();
            reply(client, request.getId(), true);
            break;
        case START:
            worker = new AsyncWorker(request.getId(), this.wattsUp_, memory_.getRegion(request.getName()));
            schedule(worker);
            reply(client, request.getId(), Boolean.TRUE);
            break;

        case END:
            worker = workers_.remove(request.getId());

            final Serializable data;
            if (worker != null)
            {
                worker.finish();
                data = (Serializable) worker.getData();
            }
            else
            {
                data = new HashMap<>();
            }

            this.executor_.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    reply(client, request.getId(), data);
                    memory_.freeRegion(request.getName());
                }
            });

            break;
        case DUMP:
        case GET:

            worker = workers_.get(request.getId());

            if (worker != null)
            {
                data = (Serializable) worker.getData();
            }
            else
            {
                data = (Serializable) this.memory_.values();
            }
            this.executor_.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    reply(client, request.getId(), data);
                }
            });
            break;

        default:
            worker = new SyncWorker(request.getId(), this.wattsUp_, client.getOutputStream());
            schedule(worker);
            break;
        }
    }

    /**
     * Schedules the execution of a non-<code>null</code> {@link Worker}.
     * 
     * @param worker
     *            {@link Worker} to schedule its execution.
     */
    private void schedule(Worker worker)
    {
        this.executor_.execute(worker);
        this.workers_.put(worker.getId(), worker);
        worker.start();
    }

    /**
     * 
     * @param client
     *            The client to reply.
     * @param id
     *            Request id that the response belongs to.
     * @param value
     *            Response's value.
     * @param compress
     *            A flag to indicate if the data must be compressed before the transmission.
     */
    private void reply(Socket client, ID id, Serializable value)
    {
        if (!client.isClosed())
        {
            Response response = Response.newResponse(id).withData(value);

            try (GZIPOutputStream gout = new GZIPOutputStream(client.getOutputStream()); ObjectOutputStream oos = new ObjectOutputStream(gout))
            {
                oos.writeObject(response);
            }
            catch (IOException exception)
            {
                LOG.log(Level.INFO, exception.getMessage(), exception);
            }
        }
    }

    /**
     * Deserialized a {@link Request} instance from the given {@link InputStream}.
     * 
     * @param in
     *            {@link InputStream} to deserialize a {@link Request}.
     * @return A {@link Request} instance.
     * @throws IOException
     *             If the stream is closed or if there wasn't a {@link Request} object on it.
     * @throws WattsUpException
     *             If the classpath is incomplete.
     */
    private Request getClientRequest(InputStream in) throws IOException
    {
        ObjectInputStream ois = new ObjectInputStream(in);
        Request request;
        try
        {
            request = (Request) ois.readObject();
        }
        catch (ClassNotFoundException e)
        {
            throw new WattsUpException(e.getMessage(), e);
        }
        return request;
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no additional
     * effect if already shut down.
     */
    public void shutdown()
    {
        this.executor_.shutdown();
    }
}
