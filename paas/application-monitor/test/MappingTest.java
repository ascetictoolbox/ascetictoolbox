import com.fasterxml.jackson.databind.ObjectMapper;
import es.bsc.amon.model.Event;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by mmacias on 27/06/14.
 */
public class MappingTest {
	@Test
	public void eventMappingTest() {
		String json = "{ \"_id\":{\"$oid\":\"53934cba30047a8c9f648508\"},\n" +
				"    \"timestamp\":1402162485485,\n" +
				"    \"endTime\":1402162487685,\n" +
				"    \"appId\":\"demoApp\",\n" +
				"    \"nodeId\":\"Node1\",\n" +
				"    \"data\" : {\n" +
				"        \"eventType\":\"VM_EXECUTION\",\n" +
				"        \"message\":\"Job crashed with error 0x212\" }}";

		ObjectMapper om = new ObjectMapper();
		try {
			Event e = om.readValue(json.getBytes(), Event.class);
			assertThat(e.getId()).isEqualToIgnoringCase("53934cba30047a8c9f648508");
			assertThat(e.appId).isEqualToIgnoringCase("demoApp");
			assertThat(e.data.get("eventType").asText()).isEqualTo("VM_EXECUTION");

			om.writeValue(System.out,e);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void legacyMappingTest() {
		String json = "{\"_id\":{\"$oid\":\"53934d3530047a8c9f648517\"},\"timestamp\":1402162485485,\"appId\":\"Idea\",\"nodeId\":\"MyMac3\",\"data\":{\"ps\":[{\"user\":\"mmacias\",\"pid\":\"815\",\"%cpu\":\"76.7\",\"%mem\":\"15.8\",\"vsz\":\"4484280\",\"rss\":\"1322276\",\"tt\":\"??\",\"stat\":\"R\",\"started\":\"8:23PM\",\"time\":\"176:30.48\",\"command\":\"/Applications/IntelliJ IDEA 13.app/Contents/MacOS/idea\"}],\"iostat\":{\"disk0\":{\"KB/t\":\"35.16\",\"tps\":\"8\",\"MB/s\":\"0.26\"},\"cpu\":{\"us\":\"15\",\"sy\":\"5\",\"id\":\"80\"},\"load\":{\"1m\":\"2.54\",\"5m\":\"2.39\",\"15m\":\"2.25\"}}}}";
		ObjectMapper om = new ObjectMapper();
		try {
			Event e = om.readValue(json.getBytes(), Event.class);
			assertThat(e.getId()).isEqualToIgnoringCase("53934d3530047a8c9f648517");
			om.writeValue(System.out,e);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void queryMappingTest() {
		String json ="{\n" +
				"\"start\":1402162485485,\n" +
				"\"end\":1402162495485,\n" +
				"\"appId\":\"demoApp\",\n" +
				"\"op\":\"array\",\n" +
				"\"data\":{\"eventType\":\"VM_EXECUTION\"}\n" +
				"}";

		ObjectMapper om = new ObjectMapper();
		try {
			Query e = om.readValue(json.getBytes(), Query.class);
			assertThat(e.appId).isEqualToIgnoringCase("demoApp");
			assertThat(e.data.get("eventType").asText()).isEqualTo("VM_EXECUTION");

			om.writeValue(System.out,e);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
