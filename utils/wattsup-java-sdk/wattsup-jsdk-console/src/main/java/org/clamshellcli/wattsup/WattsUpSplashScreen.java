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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.clamshellcli.api.Context;
import org.clamshellcli.api.SplashScreen;

import wattsup.jsdk.core.util.TextUtil;

public class WattsUpSplashScreen implements SplashScreen
{
    /**
     * Instance with the message to be displayed.
     */
    private static final StringBuilder SCREEN_MESSAGE = new StringBuilder();
    
    static
    {
        try
        {
            SCREEN_MESSAGE.append(String.format("%n%n"))
            .append(TextUtil.readLines(new File(WattsUpSplashScreen.getDefaultClassLoader().getResource("console.msg").getFile())))
            .append("A command-line tool for Watts Up? Power Meter").append(String.format("%n"))
            .append("Java version: ").append(System.getProperty("java.version")).append(String.format("%n"))
            .append("OS: ").append(System.getProperty("os.name")).append(", Version: ").append(System.getProperty("os.version"));
        }
        catch (IOException exception)
        {
            Logger.getLogger(WattsUpSplashScreen.class.getName()).log(Level.INFO, "Console message file not found.");
        }
    }

    @Override
    public void plug(Context ctx)
    {
    }

    @Override
    public void render(Context ctx)
    {
        new PrintStream((OutputStream) ctx.getValue(Context.KEY_OUTPUT_STREAM)).println(SCREEN_MESSAGE);
    }
    
    
    /**
     * Return the default ClassLoader to use: typically the thread context ClassLoader, if available; the ClassLoader that loaded the ClassUtils class
     * will be used as fallback.
     * 
     * @return the default ClassLoader (never <code>null</code>)
     * @see java.lang.Thread#getContextClassLoader()
     */
    public static ClassLoader getDefaultClassLoader()
    {
        ClassLoader cl = null;
        try
        {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex)
        {
            Logger.getLogger(WattsUpSplashScreen.class.getName()).info(ex.getMessage());
        }
        if (cl == null)
        {
            cl = WattsUpSplashScreen.class.getClassLoader();
        }
        return cl;
    }
}
