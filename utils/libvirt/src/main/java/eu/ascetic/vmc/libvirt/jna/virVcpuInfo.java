package eu.ascetic.vmc.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virVcpuInfo structure
 */
public class virVcpuInfo extends Structure {
    public int number;
    public int state;
    public long cpuTime; // this is a long long in the code, so a long mapping
    // is correct
    public int cpu;

    @SuppressWarnings("rawtypes")
	private static final List fields = Arrays.asList(
            "number", "state", "cpuTime", "cpu");

    @SuppressWarnings("rawtypes")
	@Override
    protected List getFieldOrder() {
        return fields;
    }
}
