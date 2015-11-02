package org.gridlab.gat.resources.cpi.lsf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.gridlab.gat.CommandNotFoundException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.CommandRunner;
import org.gridlab.gat.engine.util.ProcessBundle;
import org.gridlab.gat.engine.util.StreamForwarder;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.WrapperJobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.WrapperJobCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this class is used to reserve resources.
 * <p>
 * A resource can either be a hardware resource or a software resource. A
 * software resource is simply an executable it makes little sense to reserve
 * such. Thus an instance of this class can currently only reserve a hardware
 * resource.
 * <p>
 * If one wishes to reserve a hardware resource, one must first describe the
 * hardware resource that one wishes to reserve. This is accomplished by
 * creating an instance of the class HardwareResourceDescription which describes
 * the hardware resource that one wishes to reserve. After creating such an
 * instance of the class HardwareResourceDescription that describes the hardware
 * resource one wishes to reserve, one must specify the time period for which
 * one wishes to reserve the hardware resource. This is accomplished by creating
 * an instance of the class TimePeriod which specifies the time period for which
 * one wishes to reserve the hardware resource. Finally, one must obtain a
 * reservation for the desired hardware resource for the desired time period.
 * This is accomplished by calling the method ReserveHardwareResource() on an
 * instance of the class LsfResourceBrokerAdaptor with the appropriate
 * instance of HardwareResourceDescription and the appropriate instance of
 * TimePeriod.
 * <p>
 * In addition an instance of this class can be used to find hardware resources.
 * This is accomplished using the method FindHardwareResources(). This is
 * accomplished by creating an instance of the class HardwareResourceDescription
 * which describes the hardware resource that one wishes to find. After creating
 * such an instance of the class HardwareResourceDescription that describes the
 * hardware resource one wishes to find, one must find the corresponding
 * hardware resource. This is accomplished by calling the method
 * FindHardwareResources() on an instance of the class
 * LsfResourceBrokerAdaptor with the appropriate instance of
 * HardwareResourceDescription.
 */
public class LsfResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static String getDescription() {
        return "The LSF ResourceBroker Adaptor implements the ResourceBroker using the Java ProcessBuilder facility.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("beginMultiJob", true);
        capabilities.put("endMultiJob", true);
        capabilities.put("submitJob", true);

        return capabilities;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "lsf", ""};
    }

    protected static Logger logger = LoggerFactory
            .getLogger(LsfResourceBrokerAdaptor.class);

    /**
     * This method constructs a LsfResourceBrokerAdaptor instance
     * corresponding to the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     */
    public LsfResourceBrokerAdaptor(GATContext gatContext, URI brokerURI)
            throws GATObjectCreationException {
        super(gatContext, brokerURI);

        // the brokerURI should point to the localhost else throw exception
        /*
        if (!brokerURI.refersToLocalHost()) {
            throw new GATObjectCreationException(
                    "The LsfResourceBrokerAdaptor doesn't refer to localhost, but to a remote host: "
                            + brokerURI.toString());
        }
        */
        
        String path = brokerURI.getUnresolvedPath();
        if (path != null && ! path.equals("")) {
            throw new GATObjectCreationException(
                    "The LsfResourceBrokerAdaptor does not understand the specified path: " + path);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.resources.ResourceBroker#submitJob(org.gridlab.gat.resources.JobDescription)
     */
    public Job submitJob(AbstractJobDescription abstractDescription,
            MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {

        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        SoftwareDescription sd = description.getSoftwareDescription();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        if (description.getProcessCount() < 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: process count < 1: "
                            + description.getProcessCount());
        }

        if (description.getResourceCount() != 1) {
            throw new GATInvocationException(
                    "Adaptor cannot handle: resource count > 1: "
                            + description.getResourceCount());
        }

        String home = System.getProperty("user.home");
        if (home == null) {
            throw new GATInvocationException(
                    "lsf broker could not get user home dir");
        }

        Sandbox sandbox = new Sandbox(gatContext, description, "localhost",
                home, true, true, false, false);

        LsfJob lsfJob = new LsfJob(gatContext, description, sandbox);
        Job job = null;
        if (description instanceof WrapperJobDescription) {
            WrapperJobCpi tmp = new WrapperJobCpi(gatContext, lsfJob,
                    listener, metricDefinitionName);
            listener = tmp;
            job = tmp;
        } else {
            job = lsfJob;
        }
        if (listener != null && metricDefinitionName != null) {
            Metric metric = lsfJob.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            lsfJob.addMetricListener(listener, metric);
        }

        lsfJob.setState(Job.JobState.PRE_STAGING);
        lsfJob.waitForTrigger(Job.JobState.PRE_STAGING);
        sandbox.prestage();

        String exe;
        if (sandbox.getResolvedExecutable() != null) {
            exe = sandbox.getResolvedExecutable().getPath();
            // try to set the executable bit, it might be lost
            try {
                new CommandRunner("chmod", "+x", exe);
            } catch (Throwable t) {
                // ignore
            }
        } else {
            exe = getExecutable(description);
        }
        
        String[] args = getArgumentsArray(description);
        
        java.io.File f = new java.io.File(sandbox.getSandboxPath());
        
        Map<String, Object> env = sd.getEnvironment();
        
        ProcessBundle bundle = new ProcessBundle(description.getProcessCount(), exe, args, f, env);

        lsfJob.setSubmissionTime();
        lsfJob.setState(Job.JobState.SCHEDULED);
        try {
            lsfJob.setState(Job.JobState.RUNNING);
            lsfJob.waitForTrigger(Job.JobState.RUNNING);
            lsfJob.setStartTime();
            bundle.startBundle();
            lsfJob.setProcess(bundle);
        } catch (IOException e) {
            throw new CommandNotFoundException("LsfResourceBrokerAdaptor", e);
        }

        if (!sd.streamingStderrEnabled()) {
            // read away the stderr

            try {
                if (sd.getStderr() != null) {
                    OutputStream err = GAT.createFileOutputStream(gatContext, sd.getStderr());
                    // to file
                    StreamForwarder forwarder = new StreamForwarder(bundle.getStderr(), err, sd
                            .getExecutable()
                            + " [stderr]");
                    lsfJob.setErrorStream(forwarder);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created stderr forwarder to file " + sd.getStderr());
                    }
                } else {
                    // or throw it away
                    new StreamForwarder(bundle.getStderr(), null, sd
                            .getExecutable()
                            + " [stderr]");
                }
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file output stream for stderr!", e);
            }
        }

        if (!sd.streamingStdoutEnabled()) {
            // read away the stdout
            try {
                if (sd.getStdout() != null) {
                    // to file
                    OutputStream out = GAT.createFileOutputStream(gatContext, sd.getStdout());
                    StreamForwarder forwarder = new StreamForwarder(bundle.getStdout(), out, sd
                            .getExecutable()
                            + " [stdout]");
                    lsfJob.setOutputStream(forwarder);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created stdout forwarder to file " + sd.getStdout());
                    }
                } else {
                    // or throw it away
                    new StreamForwarder(bundle.getStdout(), null, sd
                            .getExecutable()
                            + " [stdout]");
                }
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file output stream for stdout!", e);
            }
        }
        
        if (!sd.streamingStdinEnabled() && sd.getStdin() != null) {
            // forward the stdin from file
            try {
                InputStream in = GAT.createFileInputStream(gatContext, sd.getStdin());
                bundle.setStdin(sd.getExecutable(), in);
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException(
                        "Unable to create file input stream for stdin!", e);
            }
        }

        lsfJob.monitorState();

        return job;
    }

}
