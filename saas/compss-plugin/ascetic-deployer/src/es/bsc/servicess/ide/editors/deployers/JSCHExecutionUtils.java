/*
 *  Copyright 2013-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package es.bsc.servicess.ide.editors.deployers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

public class JSCHExecutionUtils {
	public static final String EXECUTION_FILENAME = "runCommand.sh";
	public static final String CANCEL_FILENAME = "stopCommand.sh";
	public static final int START = 0;
	public static final int STOP = 1;
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	
	public static class MyProgressMonitor implements SftpProgressMonitor{

		long count=0;
	    long max=1;
	    long percent=-1;
		
	    public MyProgressMonitor() {
		
	    }
	    
	    @Override
		public boolean count(long count){
		    this.count+=count;
	    	if(percent>=this.count*100/max){ 
		    	System.out.println("Completed");
		    	return true; 
		    }
		    percent=this.count*100/max;
		    System.out.print("..."+percent+"%");
			return false;
		}

		@Override
		public void end() {
			System.out.println("Operation Finished");
			
		}

		@Override
		public void init(int op, String src, String dest, long max) {
			System.out.println("Starting "+((op==SftpProgressMonitor.PUT)? 
                    "put" : "get")+": "+src );
			this.max= max;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 2){
			System.err.println("Incorrect number of arguments"+ args.length);
			System.exit(-1);
		}else{
			try{
				int type = Integer.parseInt(args[0]);
				switch (type) {
				case START:
					mainStart(args);
					break;
				case STOP:
					mainStop(args);
					break;
				default:
					System.err.println("Incorrect type ("+type+")");
					System.exit(-1);
				}
			}catch(Exception e){
				System.err.println("Error executing application...");
				e.printStackTrace();
			}
		}
			
	}
		
	
	public static void mainStart(String[] args) throws Exception {
		HashMap<String,String> stageIns = new HashMap<String,String>();
		HashMap<String,String> stageOuts = new HashMap<String,String>();
		String hostname, username, privateKey, executionID;
		int numStgIn, numStgOut;
		if (args.length >= 9){
			username = args[1];
			hostname = args[2];
			privateKey = args[3];
			executionID = args[4];
			numStgIn = Integer.parseInt(args[5]);
			numStgOut = Integer.parseInt(args[6]);
			
			if (args.length < (9 +(2*(numStgIn+numStgOut)))){
				throw new Exception("Number of arguments should be bigger than " + (9 +(2*(numStgIn+numStgOut))));
			}
		}else
			throw new Exception("Illegal arguments");
		int start = 7;
		
		for (int i=0; i<numStgIn;i++){
			stageIns.put(args[start], args[start+1]);
			start = start + 2;
		}
		for (int i=0; i<numStgOut;i++){
			stageOuts.put(args[start], args[start+1]);
			start = start + 2;
		}
		execute(hostname, username, privateKey, executionID, stageIns, stageOuts, start, args);
	}
	
	public static void execute(String hostname, String username, String privateKey, String executionID,
			HashMap<String, String> stageIns, HashMap<String, String> stageOuts, int start,  String[] args) throws Exception{
		System.out.println("Connecting to server " + hostname +" with user "+ username + "(key loacation: "+ privateKey+")");
		JSch jsch = new JSch();
	    Session session = jsch.getSession(username, hostname, 22);
        jsch.addIdentity(privateKey);

	    // Avoid asking for key confirmation
	    Properties prop = new Properties();
	    prop.put("StrictHostKeyChecking", "no");
	    session.setConfig(prop);
	    session.connect();
	    stageInFiles(session, stageIns);
	    String installDir = args[start];
	    String executionFile = stageInExecutionFile(session, installDir, 
	    		generateExecutionFileContent(args, start, installDir), START);
	    executeRemoteCommand(session, "sh " + executionFile + " "+ executionID);
	    stageOutFiles(session, stageOuts);
	    session.disconnect();
	}
	
	public static void mainStop(String[] args) throws Exception {
		String hostname, username, privateKey, executionID, installDir;
		if (args.length >= 9){
			username = args[1];
			hostname = args[2];
			privateKey = args[3];
			executionID = args[4];
			installDir = args[5];
		}else
			throw new Exception("Illegal arguments");
		stop(hostname, username, privateKey, executionID, installDir);
	}

	public static void stop(String hostname, String username,
			String privateKey, String executionID, String installDir) throws Exception {
		System.out.println("Connecting to server " + hostname +" with user "+ username + "(key loacation: "+ privateKey+")");
		JSch jsch = new JSch();
	    Session session = jsch.getSession(username, hostname, 22);
        jsch.addIdentity(privateKey);

	    // Avoid asking for key confirmation
	    Properties prop = new Properties();
	    prop.put("StrictHostKeyChecking", "no");
	    session.setConfig(prop);
	    session.connect();
	    System.out.println("*** Cancelling Execution ...");
	    String executionFile = stageInExecutionFile(session, installDir, 
	    		generateCancelFileContent(installDir), STOP);
	    executeRemoteCommand(session, "sh " + executionFile + " "+ executionID);
	    session.disconnect();
	}


	private static void executeRemoteCommand(Session session, String command) throws JSchException, IOException {
		System.out.println("*** Executing application...");
		System.out.println("*** Running command " + command);
		Channel channel = session.openChannel("exec");
		((ChannelExec)channel).setPty(true);
	    ((ChannelExec)channel).setCommand(command);
	    channel.setInputStream(null);
	    
	    channel.setOutputStream(System.out);
	   
	    ((ChannelExec)channel).setErrStream(System.err);
	   
	    InputStream in=channel.getInputStream();
	    channel.connect();
	    byte[] tmp=new byte[1024];
	    while(true){
	    	while(in.available()>0){
	    		int i=in.read(tmp, 0, 1024);
	    		if(i<0)break;
	    			System.out.print(new String(tmp, 0, i));
	    	}
	    	if(channel.isClosed()){
	    		System.out.println("exit-status: "+channel.getExitStatus());
	    		break;
	    	}
	    	try{Thread.sleep(1000);}catch(Exception ee){}
	    }
	    channel.disconnect();
		
	}

	private static void stageInFiles(Session session,
			HashMap<String, String> stageIns) throws JSchException, SftpException {
		Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
		for (Entry<String, String> e:stageIns.entrySet()){
	    	System.out.println("Stage-In file" +e.getKey() +" to "+e.getValue());
	    	SftpProgressMonitor pm =  new MyProgressMonitor();
	    	sftpChannel.put(e.getKey(),e.getValue(),pm);
	    	
	    }
		channel.disconnect();
	}
	
	private static String stageInExecutionFile(Session session, String installDir, 
			String content, int type)	throws Exception {
		System.out.println("*** Generate execution command");
		Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        String file;
        switch (type) {
			case START:
				file = EXECUTION_FILENAME;
				break;
			case STOP:
				file = CANCEL_FILENAME;
				break;
			default:
				throw new Exception("Incorrect type ("+type+")");
		}
        String filename = installDir+File.separator+ file;
        sftpChannel.put(new ByteArrayInputStream(content.getBytes()), filename);
		channel.disconnect();
		return filename;
	}
	
	private static void stageOutFiles(Session session,
			HashMap<String, String> stageOuts) throws Exception{
		System.out.println("*** Stage-Out...");
		Channel channel = session.openChannel("sftp");
		
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			for (Entry<String, String> e:stageOuts.entrySet()){
				try{
					System.out.println("Stage-Out file" +e.getKey() +" to "+e.getValue());
					SftpProgressMonitor pm =  new MyProgressMonitor();
					sftpChannel.get(e.getKey(),e.getValue(), pm);
				}catch (Exception ex){
					channel.disconnect();
					throw new Exception("Error: Staging out file" +e.getKey() +" to "+e.getValue(), ex);
				}
			}
			channel.disconnect();
		
		
	}

	
	private static String generateCancelFileContent(String installDir) {	
		String command = new String("#!/bin/bash -e\n " +
	        "execID=$1\n" +
	        "current_dir=`dirname $0` \n" +
	        "\n" +
	        "pid=`cat $current_dir/compss_$execID.pid` \n" +
	        "kill $pid");
		return command;
	}
	
	private static String generateExecutionFileContent(String[] args, int start, String installDir) {
		String mainClass = args[start +1];		
		String command = new String("#!/bin/bash -e\n " +
			"add_to_classpath () {\n " +
	        "\t\t DIRLIBS=${1}/*.jar \n" +
	        "\t\t for i in ${DIRLIBS}; do \n" +
	        "\t\t\t  if [ \"$i\" != \"${DIRLIBS}\" ] ; then \n" +
	        "\t\t\t\t    CLASSPATH=$CLASSPATH:\"$i\" \n" +
	        "\t\t\t  fi \n" +
	        "\t\t done \n" +
	        "} \n" +
	        "execID=$1\n" +
	        "current_dir=`dirname $0` \n" +
	        "cd $current_dir \n" +
	        "add_to_classpath \"$current_dir\" \n" +
	        "add_to_classpath \"$current_dir/lib\" \n" +
	        "set -m\n" +
	        "java -Xms128m -Xmx2048m -classpath $current_dir:$CLASSPATH "+ mainClass);
			
		if (args.length >= start+2){
			for (int i=start+2; i<args.length;i++){
				command = command.concat(" "+args[i]);
			}
		}
		command = command.concat(" &\n"+
				"pid=$!\n" +
				"echo $pid > compss_$execID.pid\n" +
				"fg %1");
		return command;
	}


	public static boolean checkVMsBoot(Map<String, Map<String, String>> vms,
			String username, String privateKey) {
		for (Map<String, String> prov: vms.values()){

			for (Entry<String,String> e: prov.entrySet()){
				System.out.println("Connecting to vm "+ e.getKey());
				try{
					String hostname = e.getKey();
					JSch jsch = new JSch();
					Session session = jsch.getSession(username, hostname, 22);
					jsch.addIdentity(privateKey);

					// Avoid asking for key confirmation
					Properties prop = new Properties();
					prop.put("StrictHostKeyChecking", "no");
					session.setConfig(prop);
					session.connect(60*1000);
					session.disconnect();
				}catch (Exception ex){
					System.out.println(ex.getMessage());
					return false;
				}
			}
		}
		return true;
	}

}
