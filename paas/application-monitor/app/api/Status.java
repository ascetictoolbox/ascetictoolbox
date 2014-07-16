package api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.CommandResult;
import es.bsc.amon.DBManager;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.TreeMap;

/**
 * Wraps the status of the application monitor itself
 */
public class Status extends Controller {

    public static Result getStatus() {
        ObjectNode metrics = Json.newObject();

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        TreeMap<String,Object> values = new TreeMap<String, Object>();
        for (Method method : os.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("get")
                    && Modifier.isPublic(method.getModifiers())) {
                Object value;
                try {
                    value = method.invoke(os);
                    values.put(method.getName(), value);
                } catch (Exception e) {
                    Logger.warn("Error when invoking " + os.getClass().getName()
                            + " (OperatingSystemMXBean) method " + method.getName() + ": " + e);
                } // try
            } // if
        } // for

        metrics.put("jvmLoad",(Double)values.get("getProcessCpuLoad"));
        metrics.put("cpuLoad",(Double)values.get("getSystemCpuLoad"));
        metrics.put("openFD",(Long)values.get("getOpenFileDescriptorCount"));
        metrics.put("maxFD",(Long)values.get("getMaxFileDescriptorCount"));
        metrics.put("freeMemory",(Long)values.get("getFreePhysicalMemorySize"));
        metrics.put("totalMemory",(Long)values.get("getTotalPhysicalMemorySize"));

        return ok(metrics.toString());
    }

    public static Result getMeta() {
        ObjectNode meta = Json.newObject();

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        meta.put("osName", os.getName());
        meta.put("osVersion", os.getVersion());
        meta.put("arch", os.getArch());
        meta.put("cpus", os.getAvailableProcessors());

        return ok(meta.toString());
    }
}
