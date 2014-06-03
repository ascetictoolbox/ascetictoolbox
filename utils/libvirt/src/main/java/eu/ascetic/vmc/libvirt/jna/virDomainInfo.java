package eu.ascetic.vmc.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

/**
 * JNA mapping for the virDomainInfo structure
 */
public class virDomainInfo extends Structure {
    public int state;
    public NativeLong maxMem;
    public NativeLong memory;
    public short nrVirtCpu;
    public long cpuTime;

    @SuppressWarnings("rawtypes")
	private static final List fields = Arrays.asList(
            "state", "maxMem", "memory", "nrVirtCpu", "cpuTime");

    @SuppressWarnings("rawtypes")
	@Override
    protected List getFieldOrder() {
        return fields;
    }
}
