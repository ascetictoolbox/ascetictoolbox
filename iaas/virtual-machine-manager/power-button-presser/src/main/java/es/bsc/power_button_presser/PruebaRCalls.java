package es.bsc.power_button_presser;


import java.io.*;

public class PruebaRCalls {

    public static void main(String[] args) {
        try {

            File file = new File("power-button-presser/src/main/resources/holtWinters.R");

            PrintWriter writer = new PrintWriter("data.csv", "UTF-8");
            writer.println("4,4,4,4,4,5,5,5,5,3");
            writer.close();
            
            String absolutePath = file.getAbsolutePath();
            
            
            Process p = new ProcessBuilder("Rscript", absolutePath, "data.csv").start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            System.out.println(result);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
