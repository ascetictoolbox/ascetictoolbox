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
package wattsup.jsdk.agent;

import java.io.IOException;

import org.skife.jdbi.v2.DBI;

import wattsup.jsdk.core.data.WattsUpConfig;
import wattsup.jsdk.core.data.storage.database.MeasurementDao;
import wattsup.jsdk.core.data.storage.impl.DatabaseMemory;
import wattsup.jsdk.core.event.WattsUpDisconnectEvent;
import wattsup.jsdk.core.listener.WattsUpDisconnectListener;
import wattsup.jsdk.core.listener.impl.DefaultWattsUpDataAvailableListener;
import wattsup.jsdk.core.meter.WattsUp;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Main
{
    private Main()
    {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] commandArgs) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, InterruptedException
    {
        Args args = new Args();
        JCommander commander = new JCommander(args);
        
        try
        {
            commander.parse(commandArgs);
            
            Class.forName(args.getDatabaseDriverClass()).newInstance();
            
            final DBI dbi = new DBI(args.getDatabaseUrl(), args.getDatabaseUser(), args.getDatabasePasswd());
            final MeasurementDao measurementDao = dbi.open(MeasurementDao.class);

            WattsUp meter = new WattsUp(new WattsUpConfig().withPort(args.getPort()).scheduleDuration(
                    Integer.valueOf(System.getProperty("measure.duration", "0"))));

            meter.registerListener(new DefaultWattsUpDataAvailableListener(new DatabaseMemory(measurementDao)));
            meter.registerListener(new WattsUpDisconnectListener()
            {
                @Override
                public void onDisconnect(WattsUpDisconnectEvent event)
                {
                    measurementDao.close();
                    System.exit(0);
                }
            });
            
            meter.connect();
        }
        catch (ParameterException exception)
        {
            commander.usage();
        }
    }

    public static class Args
    {
        @Parameter(names = { "-port", "-p" }, required = true)
        private String port;

        @Parameter(names = { "-driver-class" }, required = true)
        private String databaseDriverClass;

        @Parameter(names = { "-username", "-u" }, required = true)
        private String databaseUser;

        @Parameter(names = { "-passwd", "-password" }, required = true)
        private String databasePasswd;

        @Parameter(names = { "-url" }, required = true)
        private String databaseUrl;

        /**
         * @return the port
         */
        public String getPort()
        {
            return port;
        }

        /**
         * @param port
         *            the port to set
         */
        public void setPort(String port)
        {
            this.port = port;
        }

        /**
         * @return the databaseDriverClass
         */
        public String getDatabaseDriverClass()
        {
            return databaseDriverClass;
        }

        /**
         * @param databaseDriverClass
         *            the databaseDriverClass to set
         */
        public void setDatabaseDriverClass(String databaseDriverClass)
        {
            this.databaseDriverClass = databaseDriverClass;
        }

        /**
         * @return the databaseUser
         */
        public String getDatabaseUser()
        {
            return databaseUser;
        }

        /**
         * @param databaseUser
         *            the databaseUser to set
         */
        public void setDatabaseUser(String databaseUser)
        {
            this.databaseUser = databaseUser;
        }

        /**
         * @return the databasePasswd
         */
        public String getDatabasePasswd()
        {
            return databasePasswd;
        }

        /**
         * @param databasePasswd
         *            the databasePasswd to set
         */
        public void setDatabasePasswd(String databasePasswd)
        {
            this.databasePasswd = databasePasswd;
        }

        /**
         * @return the databaseUrl
         */
        public String getDatabaseUrl()
        {
            return databaseUrl;
        }

        /**
         * @param databaseUrl
         *            the databaseUrl to set
         */
        public void setDatabaseUrl(String databaseUrl)
        {
            this.databaseUrl = databaseUrl;
        }

    }
}
