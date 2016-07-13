"""
@author: etejedor
@author: fconejer

PyCOMPSs Binding - Launch
=========================
  This file contains the __main__ method.
  It is called from pycompssext script with the user and environment parameters.
"""
import os
import sys
import logging
from tempfile import mkdtemp
from pycompss.api.api import compss_start, compss_stop
from pycompss.runtime.binding import get_logPath
from pycompss.util.logs import init_logging
from cPickle import PicklingError
import traceback
import pycompss.runtime.binding as binding
import pycompss.util.serializer as serializer

app_path = None

if __name__ == "__main__":

    compss_start()

    # Get log_level
    log_level = sys.argv[1]

    # Get object_conversion boolean
    o_c = sys.argv[2]
    if o_c.lower() == 'true':
        # set cross-module variable
        binding.object_conversion = True
    else:
        # set cross-module variable
        binding.object_conversion = False

    # Enable or disable the use of mmap
    # serializer.mmap_file_storage = False

    # Remove launch.py, log_level and object_conversion from sys.argv,
    # It will be inherited by the app through execfile
    sys.argv = sys.argv[3:]

    # Get application execution path
    app_path = sys.argv[0]

    logPath = get_logPath()
    binding.temp_dir = mkdtemp(prefix='pycompss', dir=logPath + '/tmpFiles/')

    # 1.3 logging
    if log_level == "debug":
        init_logging(os.getenv('IT_HOME') +
                     '/Bindings/python/log/logging.json.debug', logPath)
    elif log_level == "info":
        init_logging(os.getenv('IT_HOME') +
                     '/Bindings/python/log/logging.json.off', logPath)
    elif log_level == "off":
        init_logging(os.getenv('IT_HOME') +
                     '/Bindings/python/log/logging.json.off', logPath)
    else:
        # Default
        init_logging(os.getenv('IT_HOME') +
                     '/Bindings/python/log/logging.json', logPath)
    logger = logging.getLogger("pycompss.runtime.launch")

    try:
        logger.debug("--- START ---")
        logger.debug("PyCOMPSs Log path: %s" % logPath)
        execfile(app_path)    # MAIN EXECUTION
        logger.debug("--- END ---")
    except PicklingError:
        # If an object that can not be serialized has been used as a parameter.
        exc_type, exc_value, exc_traceback = sys.exc_info()
        lines = traceback.format_exception(exc_type, exc_value, exc_traceback)
        for line in lines:
            if app_path in line:
                print "[ ERROR ]: In: " + line,

    compss_stop()

    # --- Execution finished ---


# Version 3.0
def launch_pycompss_application(app, func, args=[], kwargs={},
                                classpath='.',
                                debug=False,
                                graph=False,
                                trace=False,
                                project_xml=os.environ['IT_HOME'] + '/Runtime/configuration/xml/projects/default_project.xml',
                                resources_xml=os.environ['IT_HOME'] + '/Runtime/configuration/xml/resources/default_resources.xml',
                                comm='NIO',
                                obj_conv=False,
                                mmap_files=False):
    global app_path
    it_home = os.environ['IT_HOME']
    pythonpath = os.environ['PYTHONPATH']
    e_classpath = os.environ['CLASSPATH']

    binding.object_conversion = obj_conv
    binding.mmap_file_storage = mmap_files

    # dirs = app.split(os.path.sep)
    file_name = os.path.splitext(os.path.basename(app))[0]
    cp = os.path.dirname(app)

    from tempfile import mkstemp
    fd, temp_path = mkstemp()
    jvm_options_file = open(temp_path, 'w')

    jvm_options_file.write('-Djava.class.path=' + it_home + '/Runtime/compss-engine.jar:' + e_classpath + ':' + cp + ':' + classpath + '\n')
    if debug:
        jvm_options_file.write('-Dlog4j.configuration=' + it_home + '/Runtime/configuration/log/COMPSsMaster-log4j.debug\n')   # DEBUG
    else:
        jvm_options_file.write('-Dlog4j.configuration=' + it_home + '/Runtime/configuration/log/COMPSsMaster-log4j\n')       # NO DEBUG
    jvm_options_file.write('-Dit.to.file=false\n')
    jvm_options_file.write('-Dit.lang=python\n')
    jvm_options_file.write('-Dit.project.file=' + project_xml + '\n')
    jvm_options_file.write('-Dit.resources.file=' + resources_xml + '\n')
    jvm_options_file.write('-Dit.project.schema=' + it_home + '/Runtime/configuration/xml/projects/project_schema.xsd\n')
    jvm_options_file.write('-Dit.resources.schema=' + it_home + '/Runtime/configuration/xml/resources/resources_schema.xsd\n')
    # jvm_options_file.write('-Dit.appName=' + app.__name__ + '\n')
    jvm_options_file.write('-Dit.appName=' + file_name + '\n')
    jvm_options_file.write('-Dit.appLogDir=/tmp/\n')
    if graph:
        jvm_options_file.write('-Dit.graph=true\n')
    else:
        jvm_options_file.write('-Dit.graph=false\n')
    jvm_options_file.write('-Dit.monitor=60000\n')
    if trace:
        jvm_options_file.write('-Dit.tracing=true\n')
    else:
        jvm_options_file.write('-Dit.tracing=false\n')
    jvm_options_file.write('-Dit.core.count=50\n')
    jvm_options_file.write('-Dit.worker.cp=' + pythonpath + ':' + e_classpath + ':' + cp + ':' + classpath + '\n')
    if comm == 'GAT':
        jvm_options_file.write('-Dit.comm=integratedtoolkit.gat.master.GATAdaptor\n')
    else:
        jvm_options_file.write('-Dit.comm=integratedtoolkit.nio.master.NIOAdaptor\n')
    jvm_options_file.write('-Dgat.adaptor.path=' + it_home + '/Dependencies/JAVA_GAT/lib/adaptors\n')
    jvm_options_file.write('-Dit.gat.broker.adaptor=sshtrilead\n')
    jvm_options_file.write('-Dit.gat.file.adaptor=sshtrilead\n')

    jvm_options_file.close()
    os.close(fd)
    os.environ['JVM_OPTIONS_FILE'] = temp_path

    # Runtime start
    compss_start()

    # Configure logging
    app_path = app
    logPath = get_logPath()
    if debug:
        # DEBUG
        init_logging(os.getenv('IT_HOME') + '/Bindings/python/log/logging.json.debug', logPath)
    else:
        # NO DEBUG
        init_logging(os.getenv('IT_HOME') + '/Bindings/python/log/logging.json', logPath)
    logger = logging.getLogger("pycompss.runtime.launch")

    logger.debug("--- START ---")
    logger.debug("PyCOMPSs Log path: %s" % logPath)
    saved_argv = sys.argv
    sys.argv = args
    # Execution:
    if func == None or func == '__main__':
        result = execfile(app)
    else:
        import imp
        imported_module = imp.load_source(file_name, app)
        methodToCall = getattr(imported_module, func)
        result = methodToCall(*args, **kwargs)
    # Recover the system arguments
    sys.argv = saved_argv
    logger.debug("--- END ---")

    compss_stop()

    return result


# Version 2.0
# ==============================================================================
# def launch_pycompss_module(app, func, args, kwargs): # explicit parameter passing
#     global app_path
#     it_home = os.environ['IT_HOME']
#     pythonpath = os.environ['PYTHONPATH']
#     classpath = os.environ['CLASSPATH']
#
#     dirs = app.split(os.path.sep)
#     file_name = os.path.splitext(os.path.basename(app))[0]
#     cp = os.path.dirname(app)
#
#     from tempfile import mkstemp
#     fd, temp_path = mkstemp()
#     jvm_options_file = open(temp_path, 'w')
#
#     jvm_options_file.write('-Djava.class.path=' + it_home + '/Runtime/compss-engine.jar:' + classpath + ':' + cp + '\n')
#     jvm_options_file.write('-Dlog4j.configuration=' + it_home + '/Runtime/configuration/log/it-log4j.debug\n')   # DEBUG
#     #jvm_options_file.write('-Dlog4j.configuration=' + it_home + '/Runtime/configuration/log/it-log4j\n')          # NO DEBUG
#     jvm_options_file.write('-Dit.to.file=false\n')
#     jvm_options_file.write('-Dit.lang=python\n')
#     jvm_options_file.write('-Dit.project.file=' + it_home + '/Runtime/configuration/xml/projects/project.xml\n')
#     jvm_options_file.write('-Dit.resources.file=' + it_home + '/Runtime/configuration/xml/resources/resources.xml\n')
#     jvm_options_file.write('-Dit.project.schema=' + it_home + '/Runtime/configuration/xml/projects/project_schema.xsd\n')
#     jvm_options_file.write('-Dit.resources.schema=' + it_home + '/Runtime/configuration/xml/resources/resource_schema.xsd\n')
#     #jvm_options_file.write('-Dit.appName=' + app.__name__ + '\n')
#     jvm_options_file.write('-Dit.appName=' + file_name + '\n')
#     jvm_options_file.write('-Dit.appLogDir=/tmp/\n')
#     jvm_options_file.write('-Dit.graph=false\n')
#     jvm_options_file.write('-Dit.monitor=60000\n')
#     jvm_options_file.write('-Dit.tracing=false\n')
#     jvm_options_file.write('-Dit.core.count=50\n')
#     jvm_options_file.write('-Dit.worker.cp=' + pythonpath + ':' + classpath + ':' + cp +'\n')
#     jvm_options_file.write('-Dit.comm=integratedtoolkit.gat.master.GATAdaptor\n') # integratedtoolkit.nio.master.NIOAdaptor
#     jvm_options_file.write('-Dgat.adaptor.path=' + it_home + '/Dependencies/JAVA_GAT/lib/adaptors\n')
#     jvm_options_file.write('-Dit.gat.broker.adaptor=sshtrilead\n')
#     jvm_options_file.write('-Dit.gat.file.adaptor=sshtrilead\n')
#
#     jvm_options_file.close()
#     os.close(fd)
#     os.environ['JVM_OPTIONS_FILE'] = temp_path
#
#     # Runtime start
#     compss_start()
#
#     # Configure logging
#     app_path = app
#     logPath = get_logPath()
#     init_logging(os.getenv('IT_HOME') + '/Bindings/python/log/logging.json.debug', logPath)   # 1.3 DEBUG
#     #init_logging(os.getenv('IT_HOME') + '/Bindings/python/log/logging.json', logPath)        # 1.3 NO DEBUG
#     logger = logging.getLogger("pycompss.runtime.launch")
#
#     logger.debug("--- START ---")
#     logger.debug("PyCOMPSs Log path: %s" % logPath)
#     saved_argv = sys.argv
#     sys.argv = args
#     # Execution:
#     if func == None or func == '__main__':
#         execfile(app)
#     else:
#         import imp
#         imported_module = imp.load_source(file_name, app)
#         methodToCall = getattr(imported_module, func)
#         result = methodToCall(*args, **kwargs)
#     # Recover the system arguments
#     sys.argv = saved_argv
#     logger.debug("--- END ---")
#
#     compss_stop()
#
#     return result
# ==============================================================================

# Version 1.0
# ==============================================================================
# def launch_pycompss_module(app, func, args):   # use the sys.arg for parameter passing
#     global app_path
#     it_home = os.environ['IT_HOME']
#     pythonpath = os.environ['PYTHONPATH']
#     classpath = os.environ['CLASSPATH']
#
#     dirs = app.split(os.path.sep)
#     file_name = os.path.splitext(os.path.basename(app))[0]
#     cp = os.path.dirname(app)
#
#     from tempfile import mkstemp
#     fd, temp_path = mkstemp()
#     jvm_options_file = open(temp_path, 'w')
#
#     jvm_options_file.write('-Djava.class.path=' + it_home + '/compss-engine.jar:' + classpath + ':' + cp + '\n')
#     #jvm_options_file.write('-Dlog4j.configuration=' + it_home + '/configuration/log/it-log4j.debug\n')   # DEBUG
#     jvm_options_file.write('-Dlog4j.configuration=' + it_home + '/configuration/log/it-log4j\n')          # NO DEBUG
#     jvm_options_file.write('-Dit.to.file=false\n')
#     jvm_options_file.write('-Dit.lang=python\n')
#     jvm_options_file.write('-Dit.project.file=' + it_home + '/configuration/xml/projects/project.xml\n')
#     jvm_options_file.write('-Dit.resources.file=' + it_home + '/configuration/xml/resources/resources.xml\n')
#     jvm_options_file.write('-Dit.project.schema=' + it_home + '/configuration/xml/projects/project_schema.xsd\n')
#     jvm_options_file.write('-Dit.resources.schema=' + it_home + '/configuration/xml/resources/resource_schema.xsd\n')
#     #jvm_options_file.write('-Dit.appName=' + app.__name__ + '\n')
#     jvm_options_file.write('-Dit.appName=' + file_name + '\n')
#     jvm_options_file.write('-Dit.appLogDir=/tmp/\n')
#     jvm_options_file.write('-Dit.graph=false\n')
#     jvm_options_file.write('-Dit.monitor=60000\n')
#     jvm_options_file.write('-Dit.tracing=false\n')
#     jvm_options_file.write('-Dit.core.count=50\n')
#     jvm_options_file.write('-Dit.worker.cp=' + pythonpath + ':' + classpath + ':' + cp +'\n')
#     jvm_options_file.write('-Dit.comm=integratedtoolkit.gat.master.GATAdaptor\n') # integratedtoolkit.nio.master.NIOAdaptor
#     jvm_options_file.write('-Dgat.adaptor.path=' + it_home + '/../Dependencies/JAVA_GAT/lib/adaptors\n')
#     jvm_options_file.write('-Dit.gat.broker.adaptor=sshtrilead\n')
#     jvm_options_file.write('-Dit.gat.file.adaptor=sshtrilead\n')
#
#     jvm_options_file.close()
#     os.close(fd)
#     os.environ['JVM_OPTIONS_FILE'] = temp_path
#
#     # Runtime start
#     compss_start()
#
#     # Configure logging
#     app_path = app
#     logPath = get_logPath()
#     #init_logging(os.getenv('IT_HOME') + '/../Bindings/python/log/logging.json.debug', logPath) # 1.3 DEBUG
#     init_logging(os.getenv('IT_HOME') + '/../Bindings/python/log/logging.json', logPath)        # 1.3 NO DEBUG
#     logger = logging.getLogger("pycompss.runtime.launch")
#
#     logger.debug("--- START ---")
#     logger.debug("PyCOMPSs Log path: %s" % logPath)
#     saved_argv = sys.argv
#     sys.argv = args
#     # Execution:
#     if func == None or func == '__main__':
#         execfile(app)
#     else:
#         import imp
#         imported_module = imp.load_source(file_name, app)
#         methodToCall = getattr(imported_module, func) # con (*args, **kwargs) no va
#         result = methodToCall()
#     # Recover the system arguments
#     sys.argv = saved_argv
#     logger.debug("--- END ---")
#
#     compss_stop()
#
#     return result
# ==============================================================================


################################
# Deprecated - Use version 3.0 #
################################
def pycompss_launch(app, args, kwargs):    # UNIFIED PORTAL - HBP
    """
    PyCOMPSs launch function - Debugging - UNIFIED PORTAL (HBP)

    This function enables to execute a pycompss application from parameters.
    Useful for PyCOMPSs binding debugging from Eclipse IDE.
    Useful for PyCOMPSs binding integration in HBP - Unified Portal.

    @param app: Application to execute.
    @param args: Arguments.
    @param kwargs: Arguments dictionary.

    @return: The execution result.
    """
    import os
    it_home = os.environ['IT_HOME']
    pythonpath = os.environ['PYTHONPATH']

    # TODO: update to PyCOMPSs 1.3
    from tempfile import mkstemp
    fd, temp_path = mkstemp()
    jvm_options_file = open(temp_path, 'w')
    jvm_options_file.write('-Djava.class.path=' + it_home + '/rt/compss-rt.jar\n')
    jvm_options_file.write('-Dlog4j.configuration=' + it_home + '/log/it-log4j\n')
    jvm_options_file.write('-Dgat.adaptor.path=' + it_home + '/../JAVA_GAT/lib/adaptors\n')
    jvm_options_file.write('-Dit.to.file=false\n')
    jvm_options_file.write('-Dit.gat.broker.adaptor=sshtrilead\n')
    jvm_options_file.write('-Dit.gat.file.adaptor=sshtrilead\n')
    jvm_options_file.write('-Dit.lang=python\n')
    jvm_options_file.write('-Dit.project.schema=' + it_home + '/xml/projects/project_schema.xsd\n')
    jvm_options_file.write('-Dit.resources.schema=' + it_home + '/xml/resources/resources_schema.xsd\n')
    jvm_options_file.write('-Dit.graph=false\n')
    jvm_options_file.write('-Dit.monitor=60000\n')
    jvm_options_file.write('-Dit.tracing=false\n')
    jvm_options_file.write('-Dit.core.count=50\n')
    jvm_options_file.write('-Dit.project.file=' + it_home + '/../infrastructure/project.xml\n')
    jvm_options_file.write('-Dit.resources.file=' + it_home + '/../infrastructure/resources.xml\n')
    jvm_options_file.write('-Dit.appName=' + app.__name__ + '\n')
    jvm_options_file.write('-Dit.worker.cp=' + pythonpath + '\n')
    jvm_options_file.close()
    os.close(fd)
    os.environ['JVM_OPTIONS_FILE'] = temp_path

    # from pycompss.api.api import compss_start, compss_stop
    compss_start()

    logPath = get_logPath()
    init_logging(os.getenv('IT_HOME') + '/../Bindings/python/log/logging.json', logPath)  # 1.3
    # init_logging(os.getenv('IT_HOME') + '/bindings/python/log/logging.json')            # 1.2

    result = app(*args, **kwargs)

    compss_stop()

    return result
