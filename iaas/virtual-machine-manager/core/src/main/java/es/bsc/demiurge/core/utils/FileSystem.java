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

package es.bsc.demiurge.core.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.configuration.Configuration;

/**
 * This helper class contains auxiliary methods to work with the file system.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class FileSystem {

    // Suppress default constructor for non-instantiability
    private FileSystem() {
        throw new AssertionError();
    }

    public static void deleteFile(String filePath) {
        Path path = FileSystems.getDefault().getPath(filePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Error while deleting a file.");
        }
    }
    
    /**
     * Delivers a string with the contents of a file given a path
     * @param fileConfPath
     * @return String
     */
    public static String readConfigurationFile(String fileConfPath) {
        StringBuilder text = new StringBuilder();
        URL urlConfPath = Configuration.class.getResource(fileConfPath);

        try{
            
            BufferedReader in = new BufferedReader(
                new InputStreamReader(urlConfPath.openStream(), "UTF8"));

            String str;
            while ((str = in.readLine()) != null) {
                text.append(str);
                text.append("\n");
            }

            in.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return text.toString();
    }
    
    /**
     * Delivers a string with the contents of a file given a path
     * @param filePath
     * @return String
     */
    public static String readFile(String filePath) {
        StringBuilder text = new StringBuilder();

        try{
            
            BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF8"));

            String str;
            while ((str = in.readLine()) != null) {
                text.append(str);
                text.append("\n");
            }

            in.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return text.toString();
    }
}