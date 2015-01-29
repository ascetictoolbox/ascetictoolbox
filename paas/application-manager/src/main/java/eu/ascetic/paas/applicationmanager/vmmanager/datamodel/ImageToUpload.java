package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

/**
 /**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class ImageToUpload {

    private String name;
    private String url;

    public ImageToUpload(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageToUpload))
             return false;
         if (obj == this)
             return true;

         ImageToUpload imageToUpload = (ImageToUpload) obj;
         if(this.name.equals(imageToUpload.getName()) && this.url.equals(imageToUpload.getUrl())) {
        	 return true;
         } else {
        	 return false;
         }
     }
}
