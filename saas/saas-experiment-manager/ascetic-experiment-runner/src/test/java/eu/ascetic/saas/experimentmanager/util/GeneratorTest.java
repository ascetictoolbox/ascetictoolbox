package eu.ascetic.saas.experimentmanager.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class GeneratorTest {

	@Test
	public void testSmartGenerateOne() {
		LinkedList<Integer> input_staticPart = new LinkedList<Integer>(){{
			add(1);
		}}; 
		LinkedList<Integer> input_pivots = new LinkedList<Integer>(){{
			add(2);
		}};
		LinkedList<LinkedList<Integer>> input_variations = 
				new LinkedList<LinkedList<Integer>>(){{
			LinkedList<Integer> axe1= new LinkedList<>();
			axe1.add(3);
			add(axe1);
		}};
		List<Generator<Integer>.Pivot> expected = new ArrayList(){{
			Generator<Integer>.Pivot p = (new Generator<Integer>()).getPivot();
			p.prefix = new ArrayList<Integer>(){{
				add(1);add(2);
			}};
			p.suffixes = new ArrayList<LinkedList<Integer>>(){{
				LinkedList<Integer> axe1= new LinkedList<>();
				axe1.add(3);
				add(axe1);
			}}; 
			add(p);
		}};
		
		Generator<Integer> gen = new Generator<>();
		List<Generator<Integer>.Pivot> actual = 
				gen.smartGenerate(input_staticPart, input_pivots, input_variations);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSmartGenerate2pivot2iteminaxes() {
		LinkedList<Integer> input_staticPart = new LinkedList<Integer>(){{
			add(11);
			add(12);
		}}; 
		LinkedList<Integer> input_pivots = new LinkedList<Integer>(){{
			add(21);
			add(22);
		}};
		LinkedList<LinkedList<Integer>> input_variations = 
				new LinkedList<LinkedList<Integer>>(){{
			LinkedList<Integer> axe1= new LinkedList<>();
			axe1.add(3);
			axe1.add(4);
			add(axe1);
		}};
		List<Generator<Integer>.Pivot> expected = new ArrayList(){{
			Generator<Integer>.Pivot p = (new Generator<Integer>()).getPivot();
			p.prefix = new ArrayList<Integer>(){{
				add(11);add(12);add(21);
			}};
			p.suffixes = new ArrayList<LinkedList<Integer>>(){{
				LinkedList<Integer> axe1= new LinkedList<>();
				axe1.add(3);
				add(axe1);
				LinkedList<Integer> axe2= new LinkedList<>();
				axe2.add(4);
				add(axe2);
			}}; 
			add(p);
			
			Generator<Integer>.Pivot p2 = (new Generator<Integer>()).getPivot();
			p2.prefix = new ArrayList<Integer>(){{
				add(11);add(12);add(22);
			}};
			p2.suffixes = new ArrayList<LinkedList<Integer>>(){{
				LinkedList<Integer> axe1= new LinkedList<>();
				axe1.add(3);
				add(axe1);
				LinkedList<Integer> axe2= new LinkedList<>();
				axe2.add(4);
				add(axe2);
			}}; 
			add(p2);
		}};
		
		Generator<Integer> gen = new Generator<>();
		List<Generator<Integer>.Pivot> actual = 
				gen.smartGenerate(input_staticPart, input_pivots, input_variations);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSmartGenerateEmpty() {
		LinkedList<Integer> input_staticPart = new LinkedList<>(); 
		LinkedList<Integer> input_pivots = new LinkedList<>();
		LinkedList<LinkedList<Integer>> input_variations = new LinkedList<>(); 
		List<Generator<Integer>.Pivot> expected = new ArrayList();
		
		Generator<Integer> gen = new Generator<>();
		List<Generator<Integer>.Pivot> actual = 
				gen.smartGenerate(input_staticPart, input_pivots, input_variations);
		
		assertEquals(expected, actual);
	}

	@Test
	public void testGenerateEmpty() {
		LinkedList<LinkedList<Integer>> input = new LinkedList<>(); 
		LinkedList<LinkedList<Integer>> expected = new LinkedList<>(); 
		
		Generator<Integer> gen = new Generator<>();
		LinkedList<LinkedList<Integer>> actual = gen.generate(input);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testGenerateOne() {
		LinkedList<LinkedList<Integer>> input = new LinkedList<LinkedList<Integer>>(){{
			LinkedList<Integer> axe1= new LinkedList<>();
			axe1.add(1);
			add(axe1);
		}};
		LinkedList<LinkedList<Integer>> expected = new LinkedList<LinkedList<Integer>>(){{
			LinkedList<Integer> axe1= new LinkedList<>();
			axe1.add(1);
			add(axe1);
		}};
		
		Generator<Integer> gen = new Generator<>();
		LinkedList<LinkedList<Integer>> actual = gen.generate(input);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testGenerateTwo() {
		LinkedList<LinkedList<Object>> input = new LinkedList<LinkedList<Object>>(){{
			LinkedList<Object> axe1= new LinkedList<>();
			axe1.add(1);
			axe1.add(2);
			axe1.add(3);
			add(axe1);
			
			LinkedList<Object> axe2= new LinkedList<>();
			axe2.add("a");
			axe2.add("b");
			axe2.add("c");
			add(axe2);
		}};
		LinkedList<LinkedList<Object>> expected = new LinkedList<LinkedList<Object>>(){{
			LinkedList<Object> item1= new LinkedList<>();
			item1.add(1);
			item1.add("a");
			add(item1);
			
			LinkedList<Object> item2= new LinkedList<>();
			item2.add(1);
			item2.add("b");
			add(item2);
			
			LinkedList<Object> item3= new LinkedList<>();
			item3.add(1);
			item3.add("c");
			add(item3);
			
			LinkedList<Object> item4= new LinkedList<>();
			item4.add(2);
			item4.add("a");
			add(item4);
			
			LinkedList<Object> item5= new LinkedList<>();
			item5.add(2);
			item5.add("b");
			add(item5);
			
			LinkedList<Object> item6= new LinkedList<>();
			item6.add(2);
			item6.add("c");
			add(item6);
			
			LinkedList<Object> item7= new LinkedList<>();
			item7.add(3);
			item7.add("a");
			add(item7);
			
			LinkedList<Object> item8= new LinkedList<>();
			item8.add(3);
			item8.add("b");
			add(item8);
			
			LinkedList<Object> item9= new LinkedList<>();
			item9.add(3);
			item9.add("c");
			add(item9);
			
		}};
		
		Generator<Object> gen = new Generator<>();
		LinkedList<LinkedList<Object>> actual = gen.generate(input);
		
		assertEquals(expected, actual);
	}

}
