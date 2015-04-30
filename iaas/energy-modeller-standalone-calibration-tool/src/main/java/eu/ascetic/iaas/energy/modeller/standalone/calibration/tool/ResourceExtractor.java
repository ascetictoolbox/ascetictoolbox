package eu.ascetic.iaas.energy.modeller.standalone.calibration.tool;

/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The aim of this class is to extract resources that are required for running,
 * JNI based applications from inside the jar.
 *
 * @author Richard Kavanagh
 */
public class ResourceExtractor {

    public static void extractSigar() {
        Properties props = System.getProperties();
        props.setProperty("java.library.path", ".");
        extractFile("libsigar-amd64-linux.so");
        extractFile("libsigar-ia64-linux.so");
        extractFile("libsigar-amd64-solaris.so");
        extractFile("libsigar-universal64-macosx.dylib");
        extractFile("libsigar-x86-linux.so");
    }

    /**
     * This extracts files from inside the jar file.
     *
     * @param name The name of the file to extract
     */
    public static void extractFile(String name) {
        try {
            /**
             * This code was derived from:
             * http://stackoverflow.com/questions/7168747/
             * java-creating-self-extracting-jar-that-can-extract-parts-of-itself-out-of-the-a
             */
            ClassLoader classLoader = ResourceExtractor.class.getClassLoader();
            File target = new File(name);
            if (target.exists()) {
                return;
            }
            InputStream inputStream;
            try (FileOutputStream outputStream = new FileOutputStream(target)) {
                inputStream = classLoader.getResourceAsStream(name);
                byte[] buffer = new byte[8 * 1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }
            inputStream.close();
        } catch (IOException ex) {
//            Logger.getLogger(ResourceExtractor.class.getName()).log(Level.SEVERE,
//                    "Failed to extract the sigar files from inside the jar", ex);
        }
    }

}
