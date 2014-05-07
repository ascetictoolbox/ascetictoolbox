package eu.optimis.vc.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainBlockInfo extends Structure {
    public long capacity;
    public long allocation;
    public long physical;

    @SuppressWarnings("rawtypes")
	private static final List fields = Arrays.asList(
            "capacity", "allocation", "physical");

    @SuppressWarnings("rawtypes")
	@Override
    protected List getFieldOrder() {
        return fields;
    }
}
