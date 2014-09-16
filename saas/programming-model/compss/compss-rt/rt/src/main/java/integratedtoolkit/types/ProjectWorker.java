/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
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

package integratedtoolkit.types;

import integratedtoolkit.ITConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class ProjectWorker implements java.io.Serializable{
        private String name;
        private String type;
        private String user;
        private String installDir;
        private String workingDir;
        private int limitOfTasks;
        private String appDir;
        private String libPath;

        
        public ProjectWorker(String name){
            this.name=name;
            this.type=null;
            this.user="user";
            this.limitOfTasks = Integer.MAX_VALUE;
            this.installDir="/IT_worker/";
            this.workingDir="/home/user/";
            this.appDir = "null";
            this.libPath = "null";
        }

        public ProjectWorker(String name, String type){
            this.name=name;
            this.type=type;
            this.user="user";
            this.limitOfTasks = Integer.MAX_VALUE;
            this.installDir="/IT_worker/";
            this.workingDir="/home/user/";
            this.appDir = "null";
            this.libPath = "null";
        }

        public ProjectWorker(String name, String type, String user){
            this.name=name;
            this.type=type;
            this.user=user;
            this.limitOfTasks = Integer.MAX_VALUE;;
            this.installDir="/IT_worker/";
            this.workingDir="/home/user/";
            this.appDir = "null";
            this.libPath = "null";
        }

        public ProjectWorker(String name, String type, String user, int limitOfTasks){
            this.name=name;
            this.type=type;
            this.user=user;
            this.limitOfTasks = limitOfTasks;
            this.installDir="/IT_worker/";
            this.workingDir="/home/user/";
            this.appDir = "null";
            this.libPath = "null";
        }       
  
        public ProjectWorker(String name, String type, String user, String iDir, String wDir){
            this.name=name;
            this.type=type;
            this.user=user;
            this.limitOfTasks = Integer.MAX_VALUE;
            this.installDir=iDir;
            this.workingDir=wDir;
            this.appDir = "null";
            this.libPath = "null";
        }
        
        public ProjectWorker(String name, String type, String user, int limitOfTasks, String iDir, String wDir){
            this.name=name;
            this.type=type;
            this.user=user;
            this.limitOfTasks = limitOfTasks;
            this.installDir=iDir;
            this.workingDir=wDir;
            this.appDir = "null";
            this.libPath = "null";
        }
        
        public ProjectWorker(String name, String type, String user, int limitOfTasks, String iDir, String wDir, String aDir, String lPath){
        	this.name=name;
            this.type=type;
            this.user=user;
            this.limitOfTasks = limitOfTasks;
            this.installDir=iDir;
            this.workingDir=wDir;
            if (aDir == null)  this.appDir = "null";  else this.appDir = aDir;
            if (lPath == null) this.libPath = "null"; else this.libPath = lPath;
        }


        public void setName(String name){
            this.name=name;
        }
        public void setType(String type){
            this.type=type;
        }
        public void setUser(String user){
            this.user=user;
        }
        public void setInstallDir(String installDir){
            this.installDir=installDir;
        }
        public void setWorkingDir(String workingDir){
            this.workingDir=workingDir;
        }
        public void setLimitOfTasks(int limitOfTasks){
            this.limitOfTasks=limitOfTasks;
        }
        public void setAppDir(String appDir){
            this.appDir=appDir;
        }
        public void setLibPath(String libPath){
            this.libPath=libPath;
        }

        public String getName(){
            return this.name;
        }
        public String getType(){
            return this.type;
        }
        public String getUser(){
            return this.user;
        }
        public String getInstallDir(){
            return this.installDir;
        }
        public String getWorkingDir(){
            return this.workingDir;
        }
        public int getLimitOfTasks(){
            return this.limitOfTasks;
        }
        public String getAppDir(){
            return this.appDir;
        }
        public String getLibPath(){
            return this.libPath;
        }

}
