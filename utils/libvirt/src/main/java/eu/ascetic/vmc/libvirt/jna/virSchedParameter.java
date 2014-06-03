package eu.ascetic.vmc.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virSchedParameter structure
 */
public class virSchedParameter extends Structure {
    public byte field[] = new byte[Libvirt.VIR_DOMAIN_SCHED_FIELD_LENGTH];
    public int type;
    public virSchedParameterValue value;

    @SuppressWarnings("rawtypes")
	private static final List fields = Arrays.asList(
            "field", "type", "value");

    @SuppressWarnings("rawtypes")
	@Override
    protected List getFieldOrder() {
        return fields;
    }
}
