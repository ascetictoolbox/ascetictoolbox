import es.bsc.vmmclient.rest.VmmRestClient;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Ignore
public class AuthTest extends TestCase {
	private static final String LOCAL_URL = "https://localhost:34372/api/v1";
	@Test
	@Ignore
	public void testAuthentication() {
		VmmRestClient vmm = new VmmRestClient(LOCAL_URL);
		boolean catchedException = false;
		try {
			vmm.getVmmService().getNodes().getNodes().size();
		} catch(Exception e) {
			catchedException = true;
		}
		assertTrue(catchedException);

		vmm = new VmmRestClient(LOCAL_URL, "admin", "changeme");
		assertEquals(2,vmm.getVmmService().getNodes().getNodes().size());

		vmm = new VmmRestClient(LOCAL_URL, "admin", "wrongpassword");
		catchedException = false;
		try {
			vmm.getVmmService().getNodes().getNodes().size();
		} catch(Exception e) {
			catchedException = true;
		}
		assertTrue(catchedException);
	}
}
