package monitor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class EnergyMonitor { 
	
	private static String scriptEMname = "script.sh";
	private static String scriptEMpath = "/home/ascetic/Documents/Ascetic/monitor/";
	private static String lastValPower = "";
	
	public void runMonitor(){
		
		String commandToRun = scriptEMpath + scriptEMname;
		
		try {
			System.out.println("Called EM");
			String[] cmd = new String[]{commandToRun};
			
            Process energyCollectorprocess = Runtime.getRuntime().exec(commandToRun); 
            System.out.println("I run this"+commandToRun);
            BufferedReader read = new BufferedReader(new InputStreamReader(energyCollectorprocess.getInputStream()));
            try {
            	energyCollectorprocess.waitFor();
            	String line = "";	
            	StringBuffer response = new StringBuffer();;
                while ((line = read.readLine())!= null) {
                	response.append(line + "\n");
                }
                
                System.out.println("I got this"+response);
                lastValPower = response.toString();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            while (read.ready()) {
                System.out.println(read.readLine());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
		
	}

	
	public String lastvalpower(){
		System.out.println("Return power");
		return lastValPower;
	}

}
