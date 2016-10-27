package es.bsc.comm;

public abstract class CommException extends Exception {

    private static final long serialVersionUID = 1L;


    public CommException(Throwable t) {
        super(t);
    }

    public CommException(Exception e) {
        super(e);
    }

}
