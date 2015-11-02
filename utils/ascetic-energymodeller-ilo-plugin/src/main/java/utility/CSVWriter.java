package utility;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

	private static final String NEW_LINE_SEPARATOR = "\n";

	FileWriter fileWriter = null;
	
	private String pathtoCSV;
	private String prefix;
	
	private String headers;

	public CSVWriter(String pathtoCSV, String prefix, String headers) {
		super();
		this.pathtoCSV = pathtoCSV;
		this.prefix = prefix;
		this.headers = headers;
	}
	
	public void initializeFile(String fileName){
		
		try {
			fileWriter = new FileWriter(pathtoCSV+prefix+fileName);
			fileWriter.append(headers);
			fileWriter.append(NEW_LINE_SEPARATOR);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
	            e.printStackTrace();
			}
		}
		
	}
	
	public void writeToFile(String line){

			try {
				fileWriter.append(line);
				fileWriter.append(NEW_LINE_SEPARATOR);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public void closeFile(){
		try {
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Error while flushing/closing fileWriter !!!");
            e.printStackTrace();
		}
	}
	
	
	
}
