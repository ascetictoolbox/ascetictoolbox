/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.bsc.vmmclient.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.mime.TypedInput;

/**
 *
 * @author raimon
 */
public class VmmErrorHandler implements ErrorHandler{
    
   private static VmmErrorHandler instance = null;
   public static VmmErrorHandler getInstance() {
      if(instance == null) {
         instance = new VmmErrorHandler();
      }
      return instance;
   }
   
   private String readServerResponse(TypedInput body){
       StringBuilder out = new StringBuilder();
       
      try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
          String newLine = System.getProperty("line.separator");
          String line;
          while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      
      return out.toString();
   }

    @Override
    public Throwable handleError(RetrofitError cause){
        if(cause.getResponse() != null && cause.getResponse().getBody() != null){
            cause.printStackTrace();
            String serverResponse = this.readServerResponse(cause.getResponse().getBody());
            throw new RuntimeException(serverResponse);
        }
        
        cause.printStackTrace();
        throw new RuntimeException(cause.getCause());
    }
    
}
