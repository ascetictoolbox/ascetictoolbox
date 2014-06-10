import com.fasterxml.jackson.databind.JsonNode;
import es.bsc.amon.util.Tree;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.test.*;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;
/**
 * Created by mmacias on 09/06/14.
 */
public class TreeTest {
	@Test
	public void parseUnparseTest() {
		String src = "{\"a\":\"b\",\"c\":3.0001,\"d\":[1,2,3,\"txt\"]" +
				",\"f\":{\"a\":3,\"b\":\"other text\",\"c\":[\"a\",\"b\",1,{\"a\":\"b\"}]}}";
		JsonNode jn = Json.parse(src);
		Tree.Node tn = Tree.fromJson(jn);
		JsonNode jn2 = Tree.toJson(tn);
		System.out.println(jn2.toString());
		assertThat(src).isEqualTo(jn2.toString());

	}
}
