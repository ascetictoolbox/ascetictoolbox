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
package org.clamshellcli.wattsup;

import java.io.File;
import java.util.Map;

import org.clamshellcli.api.Configurator;
import org.clamshellcli.api.Context;
import org.clamshellcli.api.Shell;
import org.clamshellcli.core.Clamshell;
import org.clamshellcli.core.ShellContext;

public final class Main
{
    /**
     * Constructor.
     */
    private Main()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @param args The command line arguments. This application does not need any argument.
     * @throws Exception If is not possible to load the plugins classes.
     */
    public static void main(String[] args) throws Exception 
    {
        // create/configure the context
        Context context = ShellContext.createInstance();
        Configurator config = context.getConfigurator();
        Map<String, String> propsMap = config.getPropertiesMap();
        String libDirName = propsMap.get(Configurator.KEY_CONFIG_LIBIDR);
        String pluginsDirName = propsMap.get(Configurator.KEY_CONFIG_PLUGINSDIR);

        // only continue if plugins are found
        File libDir = new File(libDirName);
        if (!libDir.exists())
        {
            System.out.printf("%nLib directory [%s] not found. This application will exit.%n%n", libDir.getCanonicalPath());
            System.exit(1);
        }
        context.putValue(Configurator.KEY_CONFIG_LIBIDR, libDir);

        // modify the the thread's class loader
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = Clamshell.Runtime.createClassLoaderForPath(new File[] {libDir}, parent);
        Thread.currentThread().setContextClassLoader(cl);

        File pluginsDir = new File(pluginsDirName);
        if (!pluginsDir.exists())
        {
            System.out.printf("%nPugins directory [%s] not found. This application will exit.%n%n", pluginsDir.getCanonicalPath());
            System.exit(1);
        }
        context.putValue(Configurator.KEY_CONFIG_PLUGINSDIR, pluginsDir);

        context.putValue(Context.KEY_INPUT_STREAM, System.in);
        context.putValue(Context.KEY_OUTPUT_STREAM, System.out);

        // validate plugins. Look for default Shell.
        if (context.getPlugins().size() > 0)
        {
            Shell shell = context.getShell();
            if (context.getShell() != null)
            {
                shell.plug(context);
            }
            else
            {
                System.out.printf("%nNo Shell component found in plugins directory [%s]." + " This application will exit now.%n",
                        pluginsDir.getCanonicalPath());
                System.exit(1);
            }
        }
        else
        {
            System.out.printf("%nNo plugins found in [%s]. This application will exit now.%n%n", pluginsDir.getCanonicalPath());
            System.exit(1);
        }
    }
}
