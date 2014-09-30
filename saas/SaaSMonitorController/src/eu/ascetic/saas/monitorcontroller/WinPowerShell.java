package eu.ascetic.saas.monitorcontroller;

/**
 * Copyright (C) 2014 CETIC (Centre d'Excellence en Technologies de l'Information et de la Communication)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("WinPowerShell")
public class WinPowerShell {
	
	private ProcessBuilder powershell;
	private Process ps_process;
	private IncrementalStreamToString out;
	private IncrementalStreamToString err;
	private PrintWriter in;
	private int cmd_id;

	private void checkPowerShell() {
		if (powershell==null)
			powershell=new ProcessBuilder("powershell.exe", "-NoProfile", "-NoExit", "-OutputFormat", "XML", "-Command", "-");
			try {
				ps_process=powershell.start();
				out = new IncrementalStreamToString(ps_process.getInputStream(),true);
				err = new IncrementalStreamToString(ps_process.getErrorStream(),true);
				new Thread(out).start();
				new Thread(err).start();
		        in = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(ps_process.getOutputStream())),true);
		        cmd_id=0;
			} catch (IOException e) {
				e.printStackTrace();
				powershell=null;
			}
	}

	@GET
	@Path("/start/{ps}")
	@Produces(MediaType.TEXT_XML)
	// @PathParam injects the value of URI parameter that defined in @Path
	// expression, into the method.
	public String start(@PathParam("ps") String ps) {
		String command0="start-job -scriptblock {  get-counter \"\\processor(0)\\% Processor Time\" -continuous}";
		String command="Enter-PSSession -ComputerName na-testvm-1\n"+
						"$job=start-job -scriptblock {  get-counter \"\\processor(0)\\% Processor Time\" -continuous}\n"+
						"echo $job\n"+
						"Exit-PSSession";
		return runCommandXML(command);
	}

	@GET
	@Path("/stop/{job}")
	@Produces(MediaType.TEXT_XML)
	// @PathParam injects the value of URI parameter that defined in @Path
	// expression, into the method.
	public String stop(@PathParam("job") String job) {
		String command="Enter-PSSession -ComputerName na-testvm-1\n"+
				"Get-Job\n"+
				"Exit-PSSession";
		return runCommandXML(command);
	}

	@GET
	@Path("/maxsample/{job}")
	@Produces(MediaType.TEXT_XML)
	// @PathParam injects the value of URI parameter that defined in @Path
	// expression, into the method.
	public String maxsample(@PathParam("job") String job) {
		String command="Get-Counter '\\Process(ASSET2008.*)\\% Processor Time' -computername na-testvm-1 -sampleinterval 1 -maxsamples 30 ";
		return runCommandXML(command);
	}
	
	@GET
	@Path("/retrieve/{job}")
	@Produces(MediaType.TEXT_XML)
	// @PathParam injects the value of URI parameter that defined in @Path
	// expression, into the method.
	public String retrieve(@PathParam("job") String job) {
		String command="get-job -id "+job;
		return runCommandXML(command);
	}

	@GET
	@Path("/json-test")
	@Produces(MediaType.APPLICATION_JSON)
	// @PathParam injects the value of URI parameter that defined in @Path
	// expression, into the method.
	public String jsonTest(@PathParam("job") String job) {
		return runCommand("Get-UICulture | ConvertTo-JSON");
	}
	
	@GET
	@Path("/test/{job}")
	@Produces(MediaType.TEXT_XML)
	// @PathParam injects the value of URI parameter that defined in @Path
	// expression, into the method.
	public String test(@PathParam("job") String job) {
		String start_job="start-job -scriptblock {  get-counter \"\\processor(0)\\% Processor Time\" -continuous}";
		String get_job="Get-UICulture";
		String res="<Bundle><Command>"+runSharedCommand(get_job)+"</Command></Bundle>";
		System.out.println(res);
		return res;
	}
	
	private String runSharedCommand(String command) {
//		if (1==1) throw new RuntimeException("Not here !");
		checkPowerShell(); 
		System.out.println("CHECK "+powershell+" "+ps_process);
//		in.println("echo command);
//		in.flush();
		return "<Status><Out>" + out.getResult()+ "</Out></Status>";
	}
	
	// XML command
	private String runCommand(String command) {
		return runCommand(command,false);
	}

	private String runCommandXML(String command) {
		return runCommand(command,true);
	}
	
	private String runCommand(String command, boolean xml) {		
        ProcessBuilder pb = new ProcessBuilder("powershell",command);
		try {
			Process p = pb.start();
	        StreamToString out = new StreamToString(p.getInputStream(),xml);
	        StreamToString err = new StreamToString(p.getErrorStream(),xml);
	        new Thread(out).start();
	        new Thread(err).start();
	        p.waitFor();	  
	        if (xml)
	        	return "<Status><Out>" + out.getResult()+ "</Out><Err>"+err.getResult()+"</Err></Status>";
	        else
	        	return out.getResult();
		} catch (IOException | InterruptedException e) {
			if (xml)
				return "<Status><Exception>"+e.getMessage()+"</Exception></Status>";
			else
				return e.getLocalizedMessage(); // not JSON
		}
	}

	class IncrementalStreamToString implements Runnable {

	    private final InputStream inputStream;
	    private boolean xml;
	    private Queue<String> fifo;
	    private boolean closed;

	    IncrementalStreamToString(InputStream inputStream) {
	        this(inputStream,true);	        
	    }

	    IncrementalStreamToString(InputStream inputStream, boolean xml) {
	        this.inputStream = inputStream;
	        this.xml=xml;
	        this.fifo=new LinkedList<String>();
	        this.closed=false;
	    }
	    
	    @Override
	    public void run() {
	        System.out.println("STARTING");
	    	BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
	        String line;
	        StringBuffer sb=new StringBuffer();	        
	        int ne=0;
	        try {
	        	while(!closed) {
	        		line = br.readLine();
	        		System.out.println("LINE: "+line);
	        		if (line==null) {
	        			closed=true;
	        			continue;
	        		}
	        		sb.append(line+"\n");
	        		if (line.trim().equals("</Objs>")) {
		        		fifo.add(sb.toString());
		        		System.out.println("ADD "+sb.toString());
		        		sb=new StringBuffer();
	        		}
	        		if (line.trim().isEmpty()) {
	        			ne++;
	        			if (ne==2) {
			        		fifo.add(sb.toString());
			        		System.out.println("ADD "+sb.toString());
			        		sb=new StringBuffer();
	        			}
	        		} else {
	        			ne=0;
	        		}
	        	}
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
	        System.out.println("CLOSED");
	    }
	    
	    public String getResult() {
	    	while (fifo.isEmpty()){
	    		try {
					Thread.sleep(1000);
					System.out.println("WAITING");
				} catch (InterruptedException e) {
				}
	    	}
	    	System.out.println("GOT IT");
	    	return fifo.poll();
	    }
	}

	class StreamToString implements Runnable {

	    private final InputStream inputStream;
	    private StringBuffer sb;
	    private boolean xml;

	    StreamToString(InputStream inputStream) {
	        this.inputStream = inputStream;
	        this.sb=new StringBuffer();
	        xml=true;
	    }

	    StreamToString(InputStream inputStream, boolean xml) {
	        this.inputStream = inputStream;
	        this.sb=new StringBuffer();
	        this.xml=xml;
	    }
	    
	    @Override
	    public void run() {
	    	System.out.println("HERE");
	        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
	        String line = "";
	        try {
	            while ((line = br.readLine()) != null) {
	            	if (xml) sb.append("<line>");
	                sb.append(line);
	            	if (xml) sb.append("</line>");
	            }
	        } catch (IOException e) {
	            sb.append("<Exception>"+e.getMessage()+"</Exception>");
	        }
	    }
	    
	    public String getResult() {
	    	return sb.toString();
	    }
	}

}
