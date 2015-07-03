package integratedtoolkit.ws.master;

import integratedtoolkit.api.ITExecution;
import integratedtoolkit.comm.Comm;
import integratedtoolkit.types.data.location.DataLocation;
import integratedtoolkit.types.job.Job;
import integratedtoolkit.types.data.LogicalData;
import integratedtoolkit.types.data.location.URI;
import integratedtoolkit.types.COMPSsWorker;
import integratedtoolkit.types.Implementation;
import integratedtoolkit.types.Task;
import integratedtoolkit.types.data.Transferable;
import integratedtoolkit.types.data.operation.DataOperation.EventListener;
import integratedtoolkit.types.job.Job.JobListener;
import integratedtoolkit.types.resources.Resource;
import integratedtoolkit.types.resources.ShutdownListener;
import java.util.HashMap;

public class ServiceInstance extends COMPSsWorker {

    private String wsdl;
    private String serviceName;
    private String namespace;
    private String port;

    public ServiceInstance(String name, HashMap<String, String> properties) {
        super(name, properties);
        this.wsdl = properties.get("wsdl");
        this.serviceName = properties.get("name");
        this.namespace = properties.get("namespace");
        this.port = properties.get("port");
    }

    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return this.port;
    }

    @Override
    public String getName() {
        return wsdl;
    }

    public boolean isTracingReady() {
        return true;
    }

    public void waitForTracingReady() {
        return;
    }

    @Override
    public void setInternalURI(URI uri) {

    }

    @Override
    public Job newJob(Task task, Implementation impl, Resource res, JobListener listener) {
        return new WSJob(task, impl, res, listener);
    }

    @Override
    public void stop(ShutdownListener sl) {
        //No need to do anything
        sl.notifyEnd();
    }

    @Override
    public void sendData(LogicalData ld, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        //Never sends Data
    }

    @Override
    public void obtainData(LogicalData ld, DataLocation source, DataLocation target, LogicalData tgtData, Transferable reason, EventListener listener) {
        //Delegate on the master to obtain the data value
        DataLocation tgtLoc = DataLocation.getLocation(Comm.appHost, target.getPath());
        Comm.appHost.getNode().obtainData(ld, source, tgtLoc, tgtData, reason, listener);
    }

    @Override
    public void updateTaskCount(int processorCoreCount) {
        // No need to do anything
    }

    @Override
    public void announceDestruction() {
        // No need to do anything
    }

    @Override
    public void announceCreation() {
        // No need to do anything
    }

    @Override
    public String getUser() {
        return "";
    }

    @Override
    public String getCompletePath(ITExecution.ParamType type, String name) {
        switch (type) {
            case FILE_T:
                return Comm.appHost.getTempDirPath() + name;
            case OBJECT_T:
                return name;
            default:
                return null;
        }
    }

    @Override
    public void deleteTemporary() {
    }
}
