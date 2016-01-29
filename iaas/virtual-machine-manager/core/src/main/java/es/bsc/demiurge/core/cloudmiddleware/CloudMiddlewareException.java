package es.bsc.demiurge.core.cloudmiddleware;

/**
 * Created by mmacias on 9/10/15.
 */
public class CloudMiddlewareException extends Exception {
    public CloudMiddlewareException() {
        super();
    }

    public CloudMiddlewareException(String message) {
        super(message);
    }

    public CloudMiddlewareException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloudMiddlewareException(Throwable cause) {
        super(cause);
    }
}
