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
import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This classes main purpose is to write the details of some entity to disk. It
 * is however important that this doesn't slow anything else down or otherwise
 * interrupt it.
 *
 * @author Richard Kavanagh
 * @param <T> This type is expected to be specified in any implementation of
 * this abstract class. A print header and print body method is expected to be
 * implemented thus completing the ability to write the specified object to
 * disk.
 */
public abstract class GenericLogger<T> implements Runnable {

    private final LinkedBlockingDeque<T> queue = new LinkedBlockingDeque<>();
    private boolean stop = false;
    protected ResultsStore saveFile = null;

    private GenericLogger() {
    }

    /**
     * This creates a generic reporting mechanism that records items out to
     * disk, asynchronously with the main thread that has requested the report.
     *
     * @param file The file to write the item/s to
     * @param overwrite If the file should be overwritten on the first write.
     */
    public GenericLogger(File file, boolean overwrite) {
        saveFile = new ResultsStore(file);
        if (overwrite && file.exists()) {
            file.delete();
        }
    }

    /**
     * This prints a items value to file in an asynchronously fashion. i.e. the
     * item's data is sent to a queue for printing to disk and then does not
     * further interrupt the operations of the data logger tool.
     *
     * @param item The item to print to file.
     */
    public void printToFile(T item) {
        queue.add(item);
    }

    /**
     * This starts the reporting process going. It is the main process that
     * waits for item to arrive in a queue ready for printing.
     */
    @Override
    public void run() {
        Logger.getLogger(GenericLogger.class.getName()).log(Level.FINER, "The logger for the file {0} started.", saveFile.getResultsFile().getName());
        while (!stop || !queue.isEmpty()) {
            try {
                T currentItem = queue.poll(30, TimeUnit.SECONDS);
                if (currentItem != null) {
                    Logger.getLogger(GenericLogger.class.getName()).log(Level.FINER, "The logger for the file {0} wrote to disk.", saveFile.getResultsFile().getName());
                    ArrayList<T> items = new ArrayList<>();
                    items.add(currentItem);
                    queue.drainTo(items);
                    saveToDisk(saveFile, items);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(GenericLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logger.getLogger(GenericLogger.class.getName()).log(Level.INFO, "The logger for the file {0} has now stopped.", saveFile.getResultsFile().getName());
    }

    /**
     * This writes an item out to disk for the purpose of auditing what is going
     * on. It by default overwrites the previous file.
     *
     * @param file The file to save the item to
     * @param item The item to write to file
     */
    public void saveToDisk(File file, T item) {
        ResultsStore toDisk = new ResultsStore(file);
        saveToDisk(toDisk, item);
    }

    /**
     * This writes an item out to disk for the purpose of auditing what is going
     * on.
     *
     * @param store The results store to save data to
     * @param item The item to write to file
     */
    public void saveToDisk(ResultsStore store, T item) {
        try {
            if (!store.getResultsFile().exists()) {
                /**
                 * Write out the header if the file does not exist or is to be
                 * overwritten
                 */
                writeHeader(store);
            }
            writebody(item, store);
            store.saveMemoryConservative();
        } catch (Exception ex) {
            //logging is important but should not stop the main thread from running!
            Logger.getLogger(GenericLogger.class.getName()).log(Level.SEVERE, "An error occurred when saving an item to disk", ex);
        }
    }

    /**
     * This writes a collection of items out to disk for the purpose of auditing 
     * what is going on.
     *
     * @param store The results store to save data to
     * @param items The item to write to file
     */
    public void saveToDisk(ResultsStore store, Collection<T> items) {
        try {
            if (!store.getResultsFile().exists()) {
                /**
                 * Write out the header if the file does not exist or is to be
                 * overwritten
                 */
                writeHeader(store);
            }
            for (T item : items) {
                writebody(item, store);
            }
            store.saveMemoryConservative();
        } catch (Exception ex) {
            //logging is important but should not stop the main thread from running!
            Logger.getLogger(GenericLogger.class.getName()).log(Level.SEVERE, "An error occurred when saving an item to disk", ex);
        }
    }

    /**
     * This is to be overwritten. Use store.add for the first data item in the
     * header and the store.append for each subsequent item. i.e.
     * store.add("Time"); store.append("Temperature");
     *
     * @param store The ResultsStore to write out to disk with.
     */
    public abstract void writeHeader(ResultsStore store);

    /**
     * This is to be overwritten. Use store.add for the first data item in the
     * header and the store.append for each subsequent item. i.e.
     * store.add("Time"); store.append("Temperature");
     *
     * @param store The ResultsStore to write out to disk with.
     */
    /**
     * This is to be overwritten. Use store.add for the first data item in the
     * body and the store.append for each subsequent item. i.e.
     * store.add(item.getValue()); store.append(item.getValue2());
     *
     * @param item The item to write out to disk.
     * @param store The file writing mechanism to write out to disk with.
     */
    public abstract void writebody(T item, ResultsStore store);

    /**
     * This permanently stops the reporter. It will however report all queued
     * work, before quitting.
     */
    public void stop() {
        this.stop = true;
    }
    
    /**
     * This indicates if the logger is still in a working state. It is considered
     * to be still working if it hasn't been told to stop and the queue is not
     * empty.
     * @return If the logger is busy or not. 
     */
    public boolean stillWorking() {
        return (isStopped() == false && !queue.isEmpty());
    }
    
    /**
     * This indicates if the stop method has been called.
     * @return If the stop method has been called or not
     */
    public boolean isStopped() {
        return stop;
    }
    
    /**
     * This indicates if this logger has any further work to perform.
     * @return if the queue of items to write to file is empty or not
     */
    public boolean isPrintQueueEmpty() {
        return queue.isEmpty();
    }
    
}
