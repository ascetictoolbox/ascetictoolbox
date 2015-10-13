/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.loader.total;

import java.io.*;
import java.nio.charset.*;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import integratedtoolkit.api.IntegratedToolkit.OpenMode;
import integratedtoolkit.loader.LoaderAPI;
import integratedtoolkit.log.Loggers;


public class StreamRegistry {

	private static enum StreamDirection {
		IN,
		OUT,
		INOUT;
	}
	
	// Api object used to invoke calls on the Integrated Toolkit
	private LoaderAPI itApi;
	
	/* Map: file absolute path -> list of opened streams of the file
	 * Only local files are accepted, since a java stream cannot be opened on a remote file 
	 */
	private TreeMap<String,StreamList> fileToStreams;
	
	private boolean onWindows;

	private String tempDirPath;
	
	private static final Logger logger = Logger.getLogger(Loggers.LOADER);
	private static final boolean debug = logger.isDebugEnabled();
	private static final String lineSep = System.getProperty("line.separator");
	
	
	public StreamRegistry(LoaderAPI api) {
		this.itApi = api;
		this.fileToStreams = new TreeMap<String,StreamList>();
		this.onWindows = File.separatorChar == '\\';
		this.tempDirPath = api.getTempDir();
	}
	
	
	// FileInputStream
	public FileInputStream newFileInputStream(File file) throws FileNotFoundException {
		StreamDirection direction = StreamDirection.IN;
		StreamList list = obtainList(file, direction);
		
		/* Create the stream on the renaming of the obtained list for the file, then add it to the list
		 * The possible exception is thrown for the application to handle it
		 */
		FileInputStream fis = new FileInputStream(list.getRenaming());
		list.addStream(fis);
		try {
			list.addFD(fis.getFD());
		}
		catch (IOException e) {
			// We must go up as a FileNotFoundException, since it is the one that the application deals with
			throw new FileNotFoundException("Loader - Error creating FileInputStream for file " + file + lineSep + e.getMessage());
		}
		
		return fis;
	}
	
	public FileInputStream newFileInputStream(String fileName) throws FileNotFoundException {
		return newFileInputStream(new File(fileName));
	}
	
	public FileInputStream newFileInputStream(FileDescriptor fd) {
		StreamList list = obtainList(fd);
		FileInputStream fis = new FileInputStream(fd);
		if (list != null) // Should always be not null
			list.addStream(fis);
		
		return fis;
	}
	
	
	// FileOutputStream
	
	public FileOutputStream newFileOutputStream(File file, boolean append) throws FileNotFoundException {
		StreamDirection direction = append ? StreamDirection.INOUT : StreamDirection.OUT;
		StreamList list = obtainList(file, direction);
		
		FileOutputStream fos = new FileOutputStream(list.getRenaming(), append);
		list.addStream(fos);
		try {
			list.addFD(fos.getFD());
		}
		catch (IOException e) {
			// We must go up as a FileNotFoundException, since it is the one that the application deals with
			throw new FileNotFoundException("Loader - Error creating FileOutputStream for file " + file + lineSep + e.getMessage());
		}
		
		return fos;
	}
	
	public FileOutputStream newFileOutputStream(File file) throws FileNotFoundException {
		return newFileOutputStream(file, false);
	}
	
	public FileOutputStream newFileOutputStream(String fileName, boolean append) throws FileNotFoundException {
		return newFileOutputStream(new File(fileName), append);
	}
	
	public FileOutputStream newFileOutputStream(String fileName) throws FileNotFoundException {
		return newFileOutputStream(new File(fileName), false);
	}
	
	public FileOutputStream newFileOutputStream(FileDescriptor fd) {
		StreamList list = obtainList(fd);
		FileOutputStream fos = new FileOutputStream(fd);
		if (list != null) // Should always be not null
			list.addStream(fos);
		
		return fos;
	}
	
	
	/* FilterInputStream (e.g. BufferedInputStream, DataInputStream)
	 * FilterOutputStream (e.g. BufferedOutputStream, DataOutputStream; not PrintStream)
	 */
	public void newFilterStream(Object stream, Object filter) {
		/* We have to replace the stream in its list by the wrapper filter,
		 * since the close will be done on the wrapper
		 */
		replaceStream(stream, filter);
	}
	
	
	// RandomAccessFile
	public RandomAccessFile newRandomAccessFile(File file, String mode) throws FileNotFoundException {
		StreamDirection direction;
		if (mode.length() == 1) // mode == "r"
			direction = StreamDirection.IN;
		else 					// mode == "rw?"
			direction = StreamDirection.INOUT;
		
		StreamList list = obtainList(file, direction);
		
		RandomAccessFile raf = new RandomAccessFile(list.getRenaming(), mode);
		list.addStream(raf);
		try {
			list.addFD(raf.getFD());
		}
		catch (IOException e) {
			// We must go up as a FileNotFoundException, since it is the one that the application deals with
			throw new FileNotFoundException("Loader - Error creating RandomAccessFile for file " + file + lineSep + e.getMessage());
		}
		
		return raf;
	}
	
	public RandomAccessFile newRandomAccessFile(String fileName, String mode) throws FileNotFoundException {
		return newRandomAccessFile(new File(fileName), mode);
	}
	
	
	// FileReader
	public FileReader newFileReader(File file) throws FileNotFoundException {
		StreamDirection direction = StreamDirection.IN;
		StreamList list = obtainList(file, direction);
		
		FileReader fr = new FileReader(list.getRenaming());
		list.addStream(fr);
		
		return fr;
	}
	
	public FileReader newFileReader(String fileName) throws FileNotFoundException {
		return newFileReader(new File(fileName));
	}
	
	public FileReader newFileReader(FileDescriptor fd) {
		StreamList list = obtainList(fd);
		FileReader fr = new FileReader(fd);
		if (list != null) // Should always be not null
			list.addStream(fr);
		
		return fr;
	}
	
	
	// InputStreamReader
	public InputStreamReader newInputStreamReader(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		/* We have to replace the old stream in its list because the new one wraps it,
		 * and the close will be done on the wrapper
		 * It is possible that the stream doesn't correspond to a file and the method
		 * replace doesn't find it, but we cannot know if it is opened on a file because
		 * certain subclasses of InputStream (i.e. FilterInputStream) can be associated
		 * to a file or not. So, we call the method anyway.
		 */
		replaceStream(is, isr);
		
		return isr;
	}
	
	public InputStreamReader newInputStreamReader(InputStream is, Charset cs) {
		InputStreamReader isr = new InputStreamReader(is, cs);
		replaceStream(is, isr);
		
		return isr;
	}
	
	public InputStreamReader newInputStreamReader(InputStream is, CharsetDecoder dec) {
		InputStreamReader isr = new InputStreamReader(is, dec);
		replaceStream(is, isr);
		
		return isr;
	}
	
	public InputStreamReader newInputStreamReader(InputStream is, String charsetName) throws UnsupportedEncodingException {
		InputStreamReader isr = new InputStreamReader(is, charsetName);
		replaceStream(is, isr);
		
		return isr;
	}
	
	
	// BufferedReader
	public BufferedReader newBufferedReader(Reader r) {
		BufferedReader br = new BufferedReader(r);
		/* We have to replace the old stream in its list because the new one wraps it,
		 * and the close will be done on the wrapper
		 * It is possible that the stream doesn't correspond to a file and the method
		 * replace doesn't find it, but we cannot know if it is opened on a file because
		 * certain subclasses of Reader (e.g. InputStreamReader) can be associated
		 * to a file or not. So, we call the method anyway.
		 */
		replaceStream(r, br);
		
		return br;
	}
	
	public BufferedReader newBufferedReader(Reader r, int size) {
		BufferedReader br = new BufferedReader(r, size);
		replaceStream(r, br);
		
		return br;
	}
	
	
	// FileWriter
	public FileWriter newFileWriter(File file, boolean append) throws IOException {
		StreamDirection direction = append ? StreamDirection.INOUT : StreamDirection.OUT;
		StreamList list = obtainList(file, direction);
		
		FileWriter fw = new FileWriter(list.getRenaming(), append);
		list.addStream(fw);
		
		return fw;
	}
	
	public FileWriter newFileWriter(File file) throws IOException {
		return newFileWriter(file, false);
	}
	
	public FileWriter newFileWriter(String fileName, boolean append) throws IOException {
		return newFileWriter(new File(fileName), append);
	}
	
	public FileWriter newFileWriter(String fileName) throws IOException {
		return newFileWriter(new File(fileName), false);
	}
	
	public FileWriter newFileWriter(FileDescriptor fd) {
		StreamList list = obtainList(fd);
		FileWriter fw = new FileWriter(fd);
		if (list != null) // Should always be not null
			list.addStream(fw);
		
		return fw;
	}
	
	
	// OutputStreamWriter
	public OutputStreamWriter newOutputStreamWriter(OutputStream os) {
		OutputStreamWriter osw = new OutputStreamWriter(os);
		/* We have to replace the old stream in its list because the new one wraps it,
		 * and the close will be done on the wrapper
		 * It is possible that the stream doesn't correspond to a file and the method
		 * replace doesn't find it, but we cannot know if it is opened on a file because
		 * certain subclasses of OutputStream (i.e. FilterOutputStream) can be associated
		 * to a file or not. So, we call the method anyway.
		 */
		replaceStream(os, osw);
		
		return osw;
	}
	
	public OutputStreamWriter newOutputStreamWriter(OutputStream os, Charset cs) {
		OutputStreamWriter osw = new OutputStreamWriter(os, cs);
		replaceStream(os, osw);
		
		return osw;
	}
	
	public OutputStreamWriter newOutputStreamWriter(OutputStream os, CharsetEncoder dec) {
		OutputStreamWriter osw = new OutputStreamWriter(os, dec);
		replaceStream(os, osw);
		
		return osw;
	}
	
	public OutputStreamWriter newOutputStreamWriter(OutputStream os, String charsetName) throws UnsupportedEncodingException {
		OutputStreamWriter osw = new OutputStreamWriter(os, charsetName);
		replaceStream(os, osw);
		
		return osw;
	}
	
	
	// BufferedWriter
	public BufferedWriter newBufferedWriter(Writer w) {
		BufferedWriter bw = new BufferedWriter(w);
		/* We have to replace the old stream in its list because the new one wraps it,
		 * and the close will be done on the wrapper
		 * It is possible that the stream doesn't correspond to a file and the method
		 * replace doesn't find it, but we cannot know if it is opened on a file because
		 * certain subclasses of Writer (e.g. OutputStreamWriter) can be associated
		 * to a file or not. So, we call the method anyway.
		 */
		replaceStream(w, bw);
		
		return bw;
	}
	
	public BufferedWriter newBufferedWriter(Writer w, int size) {
		BufferedWriter bw = new BufferedWriter(w, size);
		replaceStream(w, bw);
		
		return bw;
	}
	
	
	// PrintStream
	public PrintStream newPrintStream(File file) throws FileNotFoundException {
		StreamDirection direction = StreamDirection.OUT;
		StreamList list = obtainList(file, direction);
		
		PrintStream ps = new PrintStream(list.getRenaming());
		list.addStream(ps);
		
		return ps;
	}
	
	public PrintStream newPrintStream(File file, String csn)
	  throws FileNotFoundException, UnsupportedEncodingException {
		StreamDirection direction = StreamDirection.OUT;
		StreamList list = obtainList(file, direction);
		
		PrintStream ps = new PrintStream(list.getRenaming(), csn);
		list.addStream(ps);
		
		return ps;
	}
	
	public PrintStream newPrintStream(String fileName) throws FileNotFoundException {
		return newPrintStream(new File(fileName));
	}
	
	public PrintStream newPrintStream(String fileName, String csn)
	  throws FileNotFoundException, UnsupportedEncodingException {
		return newPrintStream(new File(fileName), csn);
	}
	
	public PrintStream newPrintStream(OutputStream os) {
		PrintStream ps = new PrintStream(os);
		replaceStream(os, ps);
		
		return ps;
	}
	
	public PrintStream newPrintStream(OutputStream os, boolean autoFlush) {
		PrintStream ps = new PrintStream(os, autoFlush);
		replaceStream(os, ps);
		
		return ps;
	}
	
	public PrintStream newPrintStream(OutputStream os, boolean autoFlush, String encoding)
	  throws UnsupportedEncodingException {
		PrintStream ps = new PrintStream(os, autoFlush, encoding);
		replaceStream(os, ps);
		
		return ps;
	}
	
	
	// PrintWriter
	public PrintWriter newPrintWriter(File file) throws FileNotFoundException {
		StreamDirection direction = StreamDirection.OUT;
		StreamList list = obtainList(file, direction);
		
		PrintWriter pw = new PrintWriter(list.getRenaming());
		list.addStream(pw);
		
		return pw;
	}
	
	public PrintWriter newPrintWriter(File file, String csn)
	  throws FileNotFoundException, UnsupportedEncodingException {
		StreamDirection direction = StreamDirection.OUT;
		StreamList list = obtainList(file, direction);
		
		PrintWriter pw = new PrintWriter(list.getRenaming(), csn);
		list.addStream(pw);
		
		return pw;
	}
	
	public PrintWriter newPrintWriter(String fileName) throws FileNotFoundException {
		return newPrintWriter(new File(fileName));
	}
	
	public PrintWriter newPrintWriter(String fileName, String csn)
	  throws FileNotFoundException, UnsupportedEncodingException {
		return newPrintWriter(new File(fileName), csn);
	}
	
	public PrintWriter newPrintWriter(OutputStream os) {
		PrintWriter pw = new PrintWriter(os);
		replaceStream(os, pw);
		
		return pw;
	}
	
	public PrintWriter newPrintWriter(OutputStream os, boolean autoFlush) {
		PrintWriter pw = new PrintWriter(os, autoFlush);
		replaceStream(os, pw);
		
		return pw;
	}
	
	public PrintWriter newPrintWriter(Writer w) {
		PrintWriter pw = new PrintWriter(w);
		replaceStream(w, pw);
		
		return pw;
	}
	
	public PrintWriter newPrintWriter(Writer w, boolean autoFlush) {
		PrintWriter pw = new PrintWriter(w, autoFlush);
		replaceStream(w, pw);
		
		return pw;
	}
	
	
	// Returns the list of streams to which the newly created stream belongs (creating it if necessary)
	private StreamList obtainList(File file, StreamDirection direction) {
		String path = null;
		try {
			// Get the absolute and canonical path of the file
			path = file.getCanonicalPath();
		}
		catch (IOException e) {
			// The Integrated Toolkit must finish
			logger.fatal("Cannot create stream for file " + file.getAbsolutePath()
						 + " with direction " + direction, e);
			System.exit(1);
		}
		
		if (onWindows) {
			// Let's make sure that we have no ambiguities on Windows
			path = path.toLowerCase();
		}
		
		StreamList list = fileToStreams.get(path);
		if (list == null) {
			// First stream opened for this file
			if (debug) {
    			logger.debug("First stream on the list for file " + path + " with direction " + direction);
			}
			
			// Obtain the renaming
			String renaming = null;
			switch (direction) {
				case IN:
					/* The last version of the file must be transferred to a temp directory without
					 * the Integrated Toolkit keeping track of this operation.
					 * Forthcoming streams on the same file will use this copy in the temp dir
					 */
					renaming = itApi.getFile(path, tempDirPath);
					break;
				case OUT:
					// Must ask the IT to open the file in W mode
					renaming = itApi.openFile(path, OpenMode.WRITE);
					break;
				case INOUT:
					// Must ask the IT to open the file in RW mode
					renaming = itApi.openFile(path, OpenMode.APPEND);
					break;
			}
			
			// Create the list of streams
			list = new StreamList(renaming, direction);
			fileToStreams.put(path, list);
		}
		
		// Set the written attribute of the list if the new stream writes the file
		if (direction != StreamDirection.IN)
			list.setWritten(true);
		
		if (debug) {
			logger.debug("New stream for file " + path
						 + " with renaming " + list.getRenaming()
						 + " and direction " + direction);
		}
		
		return list;
	}
	
	
	// Returns the list of streams to which the newly created stream belongs
	private StreamList obtainList(FileDescriptor fd) {
		for (StreamList list : fileToStreams.values()) {
			if (list.containsFD(fd)) {
				if (debug) {
					logger.debug("Found list for file descriptor " + fd + ": file " + list.getRenaming());
				}
				
				return list;
			}
		}
		return null;
	}
	
	
	// Replace a given stream by another in its list (if present)
	private void replaceStream(Object oldStream, Object newStream) {
		Iterator<Entry<String,StreamList>> entryIt = fileToStreams.entrySet().iterator();
		while (entryIt.hasNext()) {
			Entry<String,StreamList> e = entryIt.next();
			StreamList list = e.getValue();
			ListIterator<Object> listIt = list.getIterator();
			while (listIt.hasNext()) {
				Object listStream = listIt.next();
				if (listStream.equals(oldStream)) {
					listIt.set(newStream);
					
					if (debug) {
						logger.debug("Replaced stream of " + oldStream.getClass() + " by another of " + newStream.getClass());
					}
					
					continue;
				}
			}
		}
	}
	
	
	public void streamClosed(Object stream) {
		// Remove the stream from its list
		Iterator<Entry<String,StreamList>> entryIt = fileToStreams.entrySet().iterator();
		boolean found = false;
		while (entryIt.hasNext()) {
			Entry<String,StreamList> e = entryIt.next();
			StreamList list = e.getValue();
			ListIterator<Object> listIt = list.getIterator();
			while (listIt.hasNext()) {
				Object listStream = listIt.next();
				if (listStream.equals(stream)) {
					listIt.remove();
					found = true;
					continue;
				}
			}
			
			if (found) {
				if (debug) {
					logger.debug("Found closed stream of " + stream.getClass());
				}
				
				// Check if it was the last stream to be closed
				if (list.isEmpty()) {
					/* If the stream list began with a input stream and it had at least one output
					 * stream, we must obtain the renaming from the Integrated Toolkit for the new
					 * version generated, and rename and move the file to the application's working
					 * directory
					 */
					
					if (debug) {
						logger.debug("Empty stream list");
					}
					
					if (list.isFirstStreamInput() && list.getWritten()) {
						String filePath = e.getKey();
						String oldRen = list.getRenaming();
						String newRen = itApi.openFile(filePath, OpenMode.WRITE);
						File f = new File(oldRen);
						f.renameTo(new File(newRen));
						
						if (debug) {
							logger.debug("Renamed and moved file from " + oldRen + " to " + newRen);
						}
					}
					entryIt.remove();
				}
				break;
			}
		}	
	}
	

	private class StreamList {
		private String fileRenaming;
		private boolean firstIsInputStream;
		private boolean written;
		private List<Object> list;
		private List<FileDescriptor> fds;
		
		public StreamList(String renaming, StreamDirection direction) {
			this.fileRenaming = renaming;
			this.firstIsInputStream = direction == StreamDirection.IN;
			this.written = false;
			this.list = new LinkedList<Object>();
			this.fds = new LinkedList<FileDescriptor>();
		}
		
		public void addStream(Object stream) {
			list.add(stream);
		}
		
		public void addFD(FileDescriptor fd) {
			fds.add(fd);
		}
		
		public String getRenaming() {
			return fileRenaming;
		}
		
		public boolean isFirstStreamInput() {
			return firstIsInputStream;
		}
		
		public boolean getWritten() {
			return written;
		}
		
		public ListIterator<Object> getIterator() {
			return (ListIterator<Object>)list.iterator();
		}
		
		public boolean isEmpty() {
			return list.isEmpty();
		}
		
		public boolean containsFD(FileDescriptor fd) {
			return fds.contains(fd);
		}
		
		public void setWritten(boolean b) {
			written = b;
		}
	}
}
