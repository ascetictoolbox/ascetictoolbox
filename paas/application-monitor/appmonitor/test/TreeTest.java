/*
 * Author: Mario Macias (Barcelona Supercomputing Center). 2014
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 *
 * http://www.gnu.org/licenses/lgpl-2.1.html
 */
import com.fasterxml.jackson.databind.JsonNode;
import es.bsc.amon.util.tree.TreeNode;
import es.bsc.amon.util.tree.TreeNodeFactory;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;


import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by mmacias on 09/06/14.
 */
@Ignore
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
