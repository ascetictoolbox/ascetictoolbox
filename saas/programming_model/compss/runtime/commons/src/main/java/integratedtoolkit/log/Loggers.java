package integratedtoolkit.log;

import integratedtoolkit.types.exceptions.NonInstantiableException;

public final class Loggers {

    // Integrated Toolkit
    public static final String IT = "integratedtoolkit";

    // Loader
    public static final String LOADER = IT + ".Loader";
    public static final String LOADER_UTILS = IT + ".LoaderUtils";

    // API
    public static final String API = IT + ".API";

    //Resources
    public static final String RESOURCES = IT + ".Resources";

    // Components
    public static final String ALL_COMP = IT + ".Components";

    public static final String TP_COMP = ALL_COMP + ".TaskProcessor";
    public static final String TD_COMP = ALL_COMP + ".TaskDispatcher";

    public static final String TA_COMP = TP_COMP + ".TaskAnalyser";
    public static final String DIP_COMP = TP_COMP + ".DataInfoProvider";

    public static final String TS_COMP = TD_COMP + ".TaskScheduler";
    public static final String JM_COMP = TD_COMP + ".JobManager";
    public static final String FTM_COMP = TD_COMP + ".FileTransferManager";

    public static final String CONNECTORS = IT + ".Connectors";
    public static final String CONNECTORS_IMPL = IT + ".ConnectorsImpl";

    // Worker
    public static final String WORKER = IT + ".Worker";

    // Communications
    public static final String COMM = IT + ".Communication";

    private Loggers() {
        throw new NonInstantiableException("Loggers should not be instantiated");
    }

}
