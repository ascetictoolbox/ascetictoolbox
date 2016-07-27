package integratedtoolkit.nio.master.configuration;

import java.io.File;

import integratedtoolkit.nio.master.NIOAdaptor;
import integratedtoolkit.types.resources.configuration.MethodConfiguration;

public class NIOConfiguration extends MethodConfiguration {

    private String sandboxWorkingDir;
    private int minPort;
    private int maxPort;

    public NIOConfiguration(String adaptorName) {
        super(adaptorName);
    }

    public NIOConfiguration(NIOConfiguration conf) {
        super(conf);
        this.sandboxWorkingDir = conf.sandboxWorkingDir;
        this.minPort = conf.minPort;
        this.maxPort = conf.maxPort;
    }

    @Override
    public void setHost(String hostName) {
        super.setHost(hostName);
        String sandboxWorkingDir = this.getWorkingDir() + NIOAdaptor.DEPLOYMENT_ID + File.separator + this.getHost() + File.separator;
        this.setSandboxWorkingDir(sandboxWorkingDir);
    }

    @Override
    public void setWorkingDir(String workingDir) {
        super.setWorkingDir(workingDir);
        String sandboxWorkingDir = this.getWorkingDir() + NIOAdaptor.DEPLOYMENT_ID + File.separator + this.getHost() + File.separator;
        this.setSandboxWorkingDir(sandboxWorkingDir);
    }

    public String getSandboxWorkingDir() {
        return sandboxWorkingDir;
    }

    public void setSandboxWorkingDir(String sandboxWorkingDir) {
        this.sandboxWorkingDir = sandboxWorkingDir;
    }

    @Override
    public int getMinPort() {
        return minPort;
    }

    public void setMinPort(int minPort) {
        this.minPort = minPort;
    }

    @Override
    public int getMaxPort() {
        return maxPort;
    }

    public void setMaxPort(int maxPort) {
        this.maxPort = maxPort;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Conf for worker " + this.getHost()).append("\n");
        sb.append("\tUser: ").append(this.getUser()).append("\n");
        sb.append("\tAdaptorName: ").append(this.getAdaptorName()).append("\n");
        sb.append("\tApplication Dir: ").append(this.getAppDir()).append("\n");
        sb.append("\tInstall Dir: ").append(this.getInstallDir()).append("\n");
        sb.append("\tClasspath: ").append(this.getClasspath()).append("\n");
        sb.append("\tWorking Dir: ").append(this.getWorkingDir()).append("\n");
        sb.append("\tLibraryPath: ").append(this.getLibraryPath()).append("\n");
        sb.append("\tSandbox wDir:").append(this.sandboxWorkingDir).append("\n");
        sb.append("\tMin Port:").append(this.minPort).append("\n");
        sb.append("\tMaxPort:").append(this.maxPort).append("\n");
        sb.append("\tLimit of tasks:").append(this.getLimitOfTasks()).append("\n");
        sb.append("\tTotal Computing Units:").append(this.getTotalComputingUnits()).append("\n");
        return sb.toString();
    }

}
