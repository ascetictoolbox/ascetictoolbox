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
package integratedtoolkit.util;

import integratedtoolkit.ITConstants;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class RuntimeConfigManager {
    /*
     * it.appName=tareador.Tareador
     it.project.file=/home/tareador/conf/servicess/project.xml
     it.project.schema=/home/tareador/conf/servicess/project_schema.xsd      
     it.resources.file=/home/tareador/conf/servicess/resources.xml
     it.resources.schema=/home/tareador/conf/servicess/resource_schema.xsd
     it.tracing=false
     it.presched=false
     it.graph=true
     it.monitor=2000
     it.lang=java 
     it.worker.cp=:/home/user/apps/tareador.jar:/opt/COMPSs/Runtime/rt/compss-rt.jar

     log4j.configuration=/home/tareador/conf/servicess/it-log4j

     it.gat.broker.adaptor=sshtrilead 
     it.gat.file.adaptor=sshtrilead

     gat.adaptor.path=/home/tareador/conf/servicess/adaptors
     gat.debug=false
     */

    private PropertiesConfiguration config;

    public RuntimeConfigManager(String pathToConfigFile) throws ConfigurationException {
        config = new PropertiesConfiguration(pathToConfigFile);
    }

    public RuntimeConfigManager(URL pathToConfigFile) throws ConfigurationException {
        config = new PropertiesConfiguration(pathToConfigFile);
    }
    
    public RuntimeConfigManager(InputStream stream) throws ConfigurationException {
        config = new PropertiesConfiguration();
        config.load(stream);
    }
    
    public RuntimeConfigManager(File file) throws ConfigurationException {
        config = new PropertiesConfiguration(file);
    }

    public String getAppName() {
        return config.getString(ITConstants.IT_APP_NAME);
    }

    public void setAppName(String name) {
        config.setProperty(ITConstants.IT_APP_NAME, name);
    }

    public String getProjectFile() {
        return config.getString(ITConstants.IT_PROJ_FILE);
    }

    public void setProjectFile(String location) {
        config.setProperty(ITConstants.IT_PROJ_FILE, location);
    }

    public String getProjectSchema() {
        return config.getString(ITConstants.IT_PROJ_SCHEMA);
    }

    public void setProjectSchema(String location) {
        config.setProperty(ITConstants.IT_PROJ_SCHEMA, location);
    }

    public String getResourcesFile() {
        return config.getString(ITConstants.IT_RES_FILE);
    }

    public void setResourcesFile(String location) {
        config.setProperty(ITConstants.IT_RES_FILE, location);
    }

    public String getResourcesSchema() {
        return config.getString(ITConstants.IT_RES_SCHEMA);
    }

    public void setResourcesSchema(String location) {
        config.setProperty(ITConstants.IT_RES_SCHEMA, location);
    }

    public String getScheduler() {
        return config.getString(ITConstants.IT_SCHEDULER);
    }

    public void setScheduler(String implementingClass) {
        config.setProperty(ITConstants.IT_SCHEDULER, implementingClass);
    }

    public String getLog4jConfiguration() {
        return config.getString(ITConstants.LOG4J);
    }

    public void setLog4jConfiguration(String location) {
        config.setProperty(ITConstants.LOG4J, location);
    }

    public String getGATBrokerAdaptor() {
        return config.getString(ITConstants.GAT_BROKER_ADAPTOR);
    }

    public void setGATBrokerAdaptor(String adaptor) {
        config.setProperty(ITConstants.GAT_BROKER_ADAPTOR, adaptor);
    }

    public String getGATFileAdaptor() {
        return config.getString(ITConstants.GAT_FILE_ADAPTOR);
    }

    public void setGATFileAdaptor(String adaptor) {
        config.setProperty(ITConstants.GAT_FILE_ADAPTOR, adaptor);
    }

    public void setGraph(boolean graph) {
        config.setProperty(ITConstants.IT_GRAPH, graph);
    }

    public boolean isGraph() {
        return config.getBoolean(ITConstants.IT_GRAPH, false);
    }

    public void setTracing(boolean tracing) {
        config.setProperty(ITConstants.IT_TRACING, tracing);
    }

    public boolean isTracing() {
        return config.getBoolean(ITConstants.IT_TRACING, false);
    }

    public boolean isPresched() {
        return config.getBoolean(ITConstants.IT_PRESCHED, false);
    }

    public void setPresched(boolean presched) {
        config.setProperty(ITConstants.IT_PRESCHED, presched);
    }

    public void setMonitorInterval(long seconds) {
        config.setProperty(ITConstants.IT_MONITOR, seconds);
    }

    public long getMonitorInterval() {
        return config.getLong(ITConstants.IT_MONITOR);
    }

    public void save() throws ConfigurationException {
        config.save();
    }

    public String getLang() {
        return config.getString(ITConstants.IT_LANG, "java");
    }

    public void setLang(String lang) {
        config.setProperty(ITConstants.IT_LANG, lang);
    }

    public String getWorkerCP() {
        return config.getString(ITConstants.IT_WORKER_CP);
    }

    public void setWorkerCP(String classpath) {
        config.setProperty(ITConstants.IT_WORKER_CP, classpath);
    }

    public String getContext() {
        return config.getString(ITConstants.IT_CONTEXT);
    }

    public void setContext(String context) {
        config.setProperty(ITConstants.IT_CONTEXT, context);
    }

    public String getGATAdaptor() {
        return config.getString(ITConstants.GAT_ADAPTOR, System.getenv("GAT_LOCATION") + "/lib/adaptors");
    }

    public void setGATAdaptor(String adaptorPath) {
        config.setProperty(ITConstants.GAT_ADAPTOR, adaptorPath);
    }

    public boolean isGATDebug() {
        return config.getBoolean(ITConstants.GAT_DEBUG, false);
    }

    public void setGATDebug(boolean debug) {
        config.setProperty(ITConstants.GAT_DEBUG, debug);
    }

    /*public static void main(String[] args) {
     try {
     RuntimeConfigManager config = new RuntimeConfigManager("/home/jorgee/it.properties");
     config.setProjectFile("/home/jorgee/project.xml");
     config.setResourcesFile("/home/jorgee/resources.xml");
     config.setGraph(true);
     config.setTracing(false);
     config.setLog4jConfiguration("/home/jorgee/log4j.properties");
     config.setGATBrokerAdaptor("sshtrilled");
     config.setGATFileAdaptor("sshtrilled");

     config.save();

     config = new RuntimeConfigManager("/home/jorgee/it.properties");
     System.out.println(ITConstants.IT_PROJ_FILE + "=" + config.getProjectFile());
     System.out.println(ITConstants.IT_RES_FILE + "=" + config.getResourcesFile());
     System.out.println(ITConstants.LOG4J + "=" + config.getLog4jConfiguration());
     System.out.println(ITConstants.GAT_BROKER_ADAPTOR + config.getGATBrokerAdaptor());
     System.out.println(ITConstants.GAT_FILE_ADAPTOR + "=" + config.getGATFileAdaptor());
     System.out.println(ITConstants.IT_GRAPH + "=" + config.isGraph());
     System.out.println(ITConstants.IT_TRACING + "=" + config.isTracing());
     System.out.println(ITConstants.IT_TO_FILE + "=" + config.isToFile());
     System.out.println(ITConstants.IT_LIB + "=" + config.getITLib());
     System.out.println(ITConstants.IT_LANG + "=" + config.getLang());
     System.out.println(ITConstants.IT_MONITOR + "=" + config.getMonitorInterval());



     } catch (ConfigurationException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }
     }
     */
    public boolean isToFile() {
        return config.getBoolean(ITConstants.IT_TO_FILE, false);
    }
}
