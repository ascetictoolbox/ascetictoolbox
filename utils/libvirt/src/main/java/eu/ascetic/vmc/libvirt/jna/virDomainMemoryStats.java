package eu.ascetic.vmc.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainMemoryStats extends Structure {
    public int tag ;
    public long val ;

    @SuppressWarnings("rawtypes")
	private static final List fields = Arrays.asList( "tag", "val");

    @SuppressWarnings("rawtypes")
	@Override
    protected List getFieldOrder() {
        return fields;
    }
}
