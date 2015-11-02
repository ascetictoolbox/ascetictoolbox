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

package eu.ascetic.ioutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This is a storage mechanism for saving results, without necessarily looping
 * around an array of array of strings.
 *
 * @author Richard Kavanagh
 */
public class ResultsStore {

    /**
     * This stores the results
     */
    private ArrayList<ArrayList<String>> results = new ArrayList<>();
    private String delimeter = ",";
    private File resultsFile;

    /**
     * The no-args constructor
     */
    public ResultsStore() {
    }

    /**
     * The default constructor
     *
     * @param file The file to use as the results file
     */
    public ResultsStore(File file) {
        this.resultsFile = file;
    }

    /**
     * The second default constructor
     *
     * @param filename The file to use as the results file
     */
    public ResultsStore(String filename) {
        this.resultsFile = new File(filename);
    }

    /**
     * This clears the results.
     */
    public void clear() {
        results.clear();
    }

    /**
     * This adds a new row to the end of the results.
     *
     * @param row The row to add
     */
    public void addObjectRow(ArrayList<Object> row) {
        ArrayList<String> stringRow = new ArrayList<>();
        for (Object elem : row) {
            if (elem.getClass().equals(String.class)) {
                stringRow.add((String) elem);
            } else {
                stringRow.add(elem.toString());
            }
        }
        results.add(stringRow);
    }

    /**
     * This adds a new row to the end of the results.
     *
     * @param row The row to add
     */
    public void addRow(ArrayList<String> row) {
        results.add(row);
    }

    /**
     * This adds a new row to the end of the results, with a single element
     * inside.
     *
     * @param value The value to add to the new row.
     */
    public void add(String value) {
        ArrayList<String> newRow = new ArrayList<>();
        newRow.add(value);
        results.add(newRow);
    }

    /**
     * This adds a new row to the end of the results, with a single element
     * inside.
     *
     * @param value The value to add to the new row.
     */
    public void add(Object value) {
        add(value.toString());
    }

    /**
     * This adds a string value to the end of the last row. If no rows currently
     * exist it adds a new row with the single element specified.
     *
     * @param value The string to add
     */
    public void append(String value) {
        if (results.isEmpty()) {
            ArrayList<String> newRow = new ArrayList<>();
            newRow.add(value);
            results.add(newRow);
        }
        results.get(results.size() - 1).add(value);
    }

    /**
     * This adds a value to the end of the last row. If no rows currently exist
     * it adds a new row with the single element specified.
     *
     * @param value The element to add
     */
    public void append(Object value) {
        append(value.toString());
    }

    /**
     * This returns a row of the results.
     *
     * @param row The row to return
     * @return The contents of the row specified.
     */
    public ArrayList<String> getRow(int row) {
        return results.get(row);
    }

    /**
     * This reverse a given row of the results store.
     *
     * @param row The row to reverse the order of each element.
     */
    public void reverseRow(int row) {
        Collections.reverse(results.get(row));
    }

    /**
     * This systematically reverses all rows within the results set.
     */
    public void reverseRows() {
        for (ArrayList<String> row : results) {
            Collections.reverse(row);
        }
    }

    /**
     * Sort a row within the result set
     * @param row The row to store
     */
    public void sortRow(int row) {
        Collections.sort(results.get(row));
    }

    /**
     * Sorts all rows within the result store
     */
    public void sortRows() {
        for (ArrayList<String> row : results) {
            Collections.sort(row);
        }
    }

    /**
     * This gets the element of a given row and column.
     *
     * @param row The row to get
     * @param column The column entry to get
     * @return The string value for this position
     */
    public String getElement(int row, int column) {
        return results.get(row).get(column);
    }

    /**
     * This shows how many rows are present in the results store.
     *
     * @return The amount of rows present.
     */
    public int size() {
        return results.size();
    }

    /**
     * This returns the size of the longest row.
     *
     * @return The size of the longest row.
     */
    public int maxRowSize() {
        int result = 0;
        for (ArrayList<String> row : results) {
            if (row.size() > result) {
                result = row.size();
            }
        }
        return result;
    }

    /**
     * This gets the size of a given row.
     *
     * @param row The row to get the size of
     * @return The size of the row specified.
     */
    public int getRowSize(int row) {
        return results.get(row).size();
    }

    /**
     * This removes a row from the result store.
     *
     * @param row The row to remove
     * @return The row that was removed
     */
    public ArrayList<String> removeRow(int row) {
        return results.remove(row);
    }

    /**
     * This loads takes the current contents from file and loads them into
     * memory. In doing so it overwrites the current contents.
     */
    public void load() {
        results = ResultsIO.readResults(resultsFile);
    }

    /**
     * This saves the current contents of memory to disk.
     */
    public void save() {
        ResultsIO.writeResults(results, resultsFile, delimeter);
    }

    /**
     * This saves the current contents of memory to disk and then removes the
     * current elements from main memory. This means not all content can read
     * from at once but is very useful when logging out to disk.
     */
    public void saveMemoryConservative() {
        ResultsIO.writeResults(results, resultsFile, delimeter, true);
        results.clear();
    }

    /**
     * This returns the delimeter currently in use. By default it is a comma.
     *
     * @return the delimeter
     */
    public String getDelimeter() {
        return delimeter;
    }

    /**
     * This sets the delimeter that is currently in use. By default it is a
     * comma.
     *
     * @param delimeter the delimeter to set
     */
    public void setDelimeter(String delimeter) {
        if (!delimeter.equals("")) {
            this.delimeter = delimeter;
        }
    }

    /**
     * This returns the current results file in use.
     *
     * @return the file to save and load results from.
     */
    public File getResultsFile() {
        return resultsFile;
    }

    /**
     * This sets the current results file in use.
     *
     * @param resultsFile The file to save and load results from.
     */
    public void setResultsFile(File resultsFile) {
        this.resultsFile = resultsFile;
    }

    /**
     * This sets the current results file in use.
     *
     * @param filename The filename to save and load results from.
     */
    public void setResultsFile(String filename) {
        this.resultsFile = new File(filename);
    }

    /**
     * This checks to see if within the results file that the value string is
     * present.
     *
     * @param value The value to search for.
     * @return true if the value string is found in the results store. This uses
     * the equality based upon the String class.
     */
    public boolean contains(String value) {
        for (ArrayList<String> arrayList : results) {
            for (String element : arrayList) {
                if (value.equals(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This provides the position of a value that is been searched for within a
     * row in the results store.
     *
     * @param row The row to search
     * @param value The value to search for
     * @return The column of the value should it be found. -1 is returned if the
     * value is not found.
     */
    public int containsInRow(int row, String value) {
        //if the row is not present then return -1 straight away.
        if (this.size() < row) {
            return -1;
        }
        ArrayList<String> arrayList = results.get(row);
        int position = 0;
        for (String element : arrayList) {
            if (value.equals(element)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /**
     * This provides the row of a value that is been searched for within a
     * column in the results store.
     *
     * @param column The column to search
     * @param value The value to search for
     * @return The row of the value should it be found. -1 is returned if the
     * value is not found.
     */
    public int containsInColumn(int column, String value) {
        //if the column is not present at all then return -1 straight away.
        if (this.maxRowSize() < column) {
            return -1;
        }
        int position = 0;
        for (ArrayList<String> row : results) {
            try {
                if (value.equals(row.get(column))) {
                    return position;
                }

            } catch (IndexOutOfBoundsException ioobe) {
                //do nothing
            } finally {
                position++;
            }
        }
        return -1;
    }
}
