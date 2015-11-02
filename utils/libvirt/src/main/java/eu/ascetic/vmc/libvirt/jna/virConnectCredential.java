package eu.ascetic.vmc.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * JNA mapping for the virConnectCredential structure
 */
public class virConnectCredential extends Structure implements Structure.ByReference {
    public int type;
    public String prompt;
    public String challenge;
    public String defresult;
    // public Pointer result;
    public String result;
    public int resultlen;

    @SuppressWarnings("rawtypes")
	private static final List fields = Arrays.asList(
            "type", "prompt", "challenge", "defresult",
            "result", "resultlen");

    @SuppressWarnings("rawtypes")
	@Override
    protected List getFieldOrder() {
        return fields;
    }
}
