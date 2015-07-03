package integratedtoolkit.types.data.location;

import integratedtoolkit.types.resources.Resource;
import java.util.HashMap;

public class URI implements Comparable<URI> {

    private final String scheme;
    private final Resource host;
    private final String path;
    private final HashMap<String, Object> internal;

    protected static final String ANY_PROT = "any://";

    public URI(Resource host, String path) {
        this.scheme = ANY_PROT;
        this.host = host;
        this.path = path;
        this.internal = new HashMap<String, Object>();
    }

    public URI(String scheme, Resource host, String path) {
        this.scheme = scheme;
        this.host = host;
        this.path = path;
        this.internal = new HashMap<String, Object>();
    }

    public void setInternalURI(String adaptor, Object uri) {
        internal.put(adaptor, uri);
    }

    public Object getInternalURI(String adaptor) {
        Object o = internal.get(adaptor);
        if (o == null) {
            host.setInternalURI(this);
            o = internal.get(adaptor);
        }
        return o;
    }

    public Resource getHost() {
        return this.host;
    }

    public String getPath() {
        return path;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String toString() {
        return scheme + host.getName() + "/" + path;
    }

    public String debugString() {
        StringBuilder sb = new StringBuilder(scheme + host.toString() + "/" + path + "\n");
        for (java.util.Map.Entry<String, Object> e : internal.entrySet()) {
            sb.append("\t * ").append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public int compareTo(URI o) {
        if (o == null) {
            throw new NullPointerException();
        }
        int compare = host.getName().compareTo(o.host.getName());
        if (compare == 0) {
            compare = path.compareTo(o.path);
        }
        return compare;
    }
}
