/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This reads and writes delimited files from disk. It is aimed at reading and
 * writing results to and from disk though may be used to read and write to any
 * delimited file. The default is for a comma separated file. When writing out
 * to disk it will take the current operating systems default end of line
 * marker.
 *
 * @author Richard Kavanagh
 */
public class ResultsIO {

    /**
     * This is the default end of line marker that is in use by the current OS.
     * Note: on Unix like OSs it is "\n" and for windows the end of line marker
     * is "\r\n";
     */
    public static final String NEW_LINE = System.getProperty("line.separator");
    private static final String DEFAULT_DELIMETER = ",";

    /**
     * This writes out a comma separated file to disk. This overwrites the file
     * specified.
     *
     * @param results The set of values to be written out to file.
     * @param filename The file to written to disk
     */
    public static void writeResults(ArrayList<ArrayList<String>> results,
            String filename) {
        writeResults(results, new File(filename), DEFAULT_DELIMETER);
    }

    /**
     * This writes out a comma separated file to disk. This overwrites the file
     * specified.
     *
     * @param results The set of values to be written out to file.
     * @param file The file to written to disk
     */
    public static void writeResults(ArrayList<ArrayList<String>> results,
            File file) {
        writeResults(results, file, DEFAULT_DELIMETER);
    }

    /**
     * This writes a file out to disk. This overwrites the filename specified.
     *
     * @param results The set of values to be written out to file.
     * @param filename The file to written to disk
     * @param delimeter The delimeter to use when parsing the file.
     */
    public static void writeResults(ArrayList<ArrayList<String>> results,
            String filename, String delimeter) {
        writeResults(results, new File(filename), delimeter);
    }

    /**
     * This writes a file out to disk. This overwrites the filename specified.
     *
     * @param results The set of values to be written out to file.
     * @param file The file to written to disk
     * @param delimeter The delimeter to use when parsing the file.
     */
    public static void writeResults(ArrayList<ArrayList<String>> results,
            File file, String delimeter) {
        writeResults(results, file, delimeter, false);
    }

    /**
     * This writes a file out to disk. This overwrites the filename specified.
     *
     * @param results The set of values to be written out to file.
     * @param file The file to written to disk
     * @param append If the results file should be appended to instead of
     * overwriting.
     */
    public static void writeResults(ArrayList<ArrayList<String>> results,
            File file, boolean append) {
        writeResults(results, file, DEFAULT_DELIMETER, append);
    }

    /**
     * This writes a file out to disk. This overwrites the filename specified.
     *
     * @param results The set of values to be written out to file.
     * @param file The file to written to disk
     * @param delimeter The delimeter to use when parsing the file.
     * @param append If the results file should be appended to instead of
     * overwriting.
     */
    public static void writeResults(ArrayList<ArrayList<String>> results,
            File file, String delimeter, boolean append) {

        try {
            FileWriter fileWriter = new FileWriter(file, append);
            try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                String element;
                for (int k = 0; k < results.size(); k++) {
                    ArrayList<String> currentRow = results.get(k);
                    for (int i = 0; i < currentRow.size(); i++) {
                        element = currentRow.get(i);
                        if (i != currentRow.size() - 1) {
                            bufferedWriter.write(element + delimeter);
                        } else {
                            /**
                             * The test below ensures the end of file does not finish with a new line marker
                             * but only when not appending to the file.
                             */
                            if (k != results.size() - 1 || append) {
                                bufferedWriter.write(element + NEW_LINE);
                            } else {
                                bufferedWriter.write(element);
                            }
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            Logger.getLogger(ResultsIO.class.getName()).log(Level.SEVERE, "Unable to write to results file", ioe);
        }
    }

    /**
     * This reads in a comma separated file from disk.
     *
     * @param filename The file to read in from disk
     * @return The array list of values from this file
     */
    public static ArrayList<ArrayList<String>> readResults(String filename) {
        return readResults(new File(filename), DEFAULT_DELIMETER);
    }

    /**
     * This reads in a comma separated file from disk.
     *
     * @param file The file to read in from disk
     * @return The array list of values from this file
     */
    public static ArrayList<ArrayList<String>> readResults(File file) {
        return readResults(file, DEFAULT_DELIMETER);
    }

    /**
     * This reads in a file from disk.
     *
     * @param filename The file to read in from disk
     * @param delimeter The delimeter to use when parsing the file.
     * @return The array list of values from this file
     */
    public static ArrayList<ArrayList<String>> readResults(String filename, String delimeter) {
        return readResults(new File(filename), delimeter);
    }

    /**
     * This reads in a file from disk.
     *
     * @param file The file to read in from disk
     * @param delimeter The delimeter to use when parsing the file.
     * @return The array list of values from this file, if no file exists then
     * it provides an empty but none null result.
     */
    public static ArrayList<ArrayList<String>> readResults(File file, String delimeter) {

        ArrayList<ArrayList<String>> results = new ArrayList<>();

        if (!file.exists()) {
            return results;
        }

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringTokenizer tokenizer;
                ArrayList<String> arrayListLine;
                String currentLine = reader.readLine();
                //read delimeted file line by line and add to array list
                while (currentLine != null) {
                    tokenizer = new StringTokenizer(currentLine, delimeter);
                    arrayListLine = new ArrayList<>();
                    while (tokenizer.hasMoreTokens()) {
                        arrayListLine.add(tokenizer.nextToken());
                    }
                    results.add(arrayListLine);
                    currentLine = reader.readLine();
                }
            }
        } catch (IOException ioe) {
            Logger.getLogger(ResultsIO.class.getName()).log(Level.SEVERE, "Unable to read from results file", ioe);
        }
        return results;

    }
}
