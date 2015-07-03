package integratedtoolkit.connectors;


public class ConnectorException extends Exception {

	public ConnectorException(String message) {
		super(message);
	}
	public ConnectorException(Exception e) {
		super(e.getMessage());
	}
}
