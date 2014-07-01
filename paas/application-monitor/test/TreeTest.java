import com.fasterxml.jackson.databind.JsonNode;
import es.bsc.amon.util.tree.TreeNode;
import es.bsc.amon.util.tree.TreeNodeFactory;
import org.junit.Test;


import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by mmacias on 09/06/14.
 */
public class TreeTest {
	@Test
	public void parseUnparseTest() {
		String src = "{\"a\":\"b\",\"c\":3.0001,\"d\":[1,2,3,\"txt\"]" +
				",\"f\":{\"a\":3,\"b\":\"other text\",\"c\":[\"a\",\"b\",1,{\"a\":\"b\"}]}}";
		TreeNode tn = TreeNodeFactory.fromJson(src);
		JsonNode jn2 = TreeNodeFactory.toJson(tn);
		assertThat(src).isEqualTo(jn2.toString());

	}
	@Test
	public void appendTest() {
		Throwable t = null;
		try {
			TreeNode t1 = TreeNodeFactory.fromJson("{\"a\":\"b\"}");
			t1.append(TreeNodeFactory.fromJson("{\"a\":\"b\"}"));
		} catch(Throwable e) {
			t = e;
		}
		assertThat(t).isNull();
		try {
			TreeNode t1 = TreeNodeFactory.fromJson("{\"a\":\"b\"}");
			t1.append(TreeNodeFactory.fromJson("[{\"a\":\"b\"}]"));
		} catch(Throwable e) {
			t = e;
		}
		assertThat(t).isNotNull();

	}
	@Test
	public void navigateTest() {
		String src = "{\"a\":\"b\",\"c\":3.0001,\"d\":[1,2,3,\"txt\"]" +
				",\"f\":{\"a\":3,\"b\":\"other text\",\"c\":[\"a\",\"b\",1,{\"a\":\"b\"}]}}";
		TreeNode t = TreeNodeFactory.fromJson(src);
		assertThat(t.to("d").to("3").getValue()).isEqualTo("txt");

		Throwable tr=null;
		try {
			t.to("hiYou");
		}catch(Throwable e) {
			tr = e;
		}
		assertThat(tr).isNotNull();

		assertThat(t.to("a").getChildrenIds()).isNullOrEmpty();
		assertThat(t.to("d").getChildrenIds()).contains("1","2","0","3");
		assertThat(t.to("f").to("c").to("3").getChildrenIds().contains("a"));
	}

}
