/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmmclient.config;

import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private static final String CONF_FILE_LOCATION = "config.properties";

    public final String restUrl;

    private Configuration() {
        Properties prop = new Properties();
        try {
            prop.load(Configuration.class.getClassLoader().getResourceAsStream(CONF_FILE_LOCATION));
        } catch (IOException e) {
            e.printStackTrace();
        }
        restUrl = prop.getProperty("restUrl");
    }

    private static class SingletonHolder {
        private static final Configuration CONF_INSTANCE = new Configuration();
    }

    public static Configuration getInstance() {
        return SingletonHolder.CONF_INSTANCE;
    }

}
