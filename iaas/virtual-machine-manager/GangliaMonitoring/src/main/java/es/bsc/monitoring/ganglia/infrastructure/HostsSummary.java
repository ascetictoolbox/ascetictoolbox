
package es.bsc.monitoring.ganglia.infrastructure;

/**
 *
 * Configuration element for the Ganglia summary mapping
 * 
 * @author Mauro Canuto <mauro.canuto@bsc.es>
 */
public class HostsSummary {
    
    private String up;
    private String down;
    private String source;

    public HostsSummary(String up, String down, String source) {
        this.up = up;
        this.down = down;
        this.source = source;
    }

    public String getUp() {
        return up;
    }

    public void setUp(String up) {
        this.up = up;
    }

    public String getDown() {
        return down;
    }

    public void setDown(String down) {
        this.down = down;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    
    
}
