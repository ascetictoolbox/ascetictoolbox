package eu.ascetic.saas.experimentmanager.wslayer;

import java.io.InputStream;
import java.util.List;

import eu.ascetic.saas.experimentmanager.wslayer.exception.ResponseParsingException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSBaseException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSException;

public interface Handler {

	public String getAccepted();
	
	public String getSingle(InputStream result, String query) throws ResponseParsingException, WSException;
	
	public List<String> getList(InputStream result, String query) throws ResponseParsingException, WSException;
}
