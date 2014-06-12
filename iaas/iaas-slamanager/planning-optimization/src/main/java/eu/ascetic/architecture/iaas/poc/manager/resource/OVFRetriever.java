/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.architecture.iaas.poc.manager.resource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class OVFRetriever {

	private URL url;

	private String filename = null;

	protected String configurationFilesPath = System.getenv("SLASOI_HOME") + System.getProperty("file.separator") + "contrail-slamanager" + System.getProperty("file.separator")
			+ "provisioning-adjustment" + System.getProperty("file.separator");

	protected String propertiesFile = "provisioning_adjustment.properties";

	protected Properties properties = new Properties();

	public OVFRetriever() {
		try {
			properties.load(new FileInputStream(configurationFilesPath + propertiesFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void retrieveOvf(String url) {
		if (url.startsWith("http")) {
			try {
				this.url = new URL(url);
				String name = url.substring(url.lastIndexOf("/") + 1);
				HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept", "application/xml");
				InputStream isr = connection.getInputStream();
				FileOutputStream fos = new FileOutputStream(properties.getProperty("ovf_repo_path") + name);
				int count;
				while ((count = isr.read()) != -1) {
					fos.write(count);
				}
				isr.close();
				fos.close();
				setFilename(properties.getProperty("ovf_repo_path") + name);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			setFilename(properties.getProperty("ovf_repo_path") + url);
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
