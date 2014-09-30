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
package test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTest
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket server = new ServerSocket(9090);

        while (true)
        {
            Socket client = server.accept();

            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
//            OutputStream output = client.getOutputStream();

            StringBuilder msg = new StringBuilder();
            int i;
            while ((i = in.read()) != -1)
            {
                msg.append((char) i);
            }
            System.err.println(msg.toString());
            msg.setLength(0);
        }
    }
}
