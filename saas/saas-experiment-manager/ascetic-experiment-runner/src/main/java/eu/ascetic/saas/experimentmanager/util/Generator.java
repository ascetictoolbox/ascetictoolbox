package eu.ascetic.saas.experimentmanager.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.binding.Logging;

public class Generator<T> {

	public class Pivot{
		
		public Pivot(){}
		public List<T> prefix;
		public List<LinkedList<T>> suffixes;
		
		public boolean equals(Object other){
			if(other instanceof Generator.Pivot){
				Generator.Pivot p = (Generator.Pivot) other;
				return this.prefix.equals(p.prefix) 
						&& this.suffixes.equals(p.suffixes);
			}
			else{
				return false;
			}
		}
		
		
	}
	
	public Pivot getPivot(){
		return new Pivot();
	}
	
	public Generator(){
		
	}
	
	public List<Pivot> smartGenerate(List<T> staticPart, 
			List<T> pivots, LinkedList<LinkedList<T>> variations){
		Logger.getLogger("Generator").info("begin smartgenerating ...");
		List<Pivot> result = new ArrayList<>();
		LinkedList<LinkedList<T>> suffixes = generate(variations);
		Logger.getLogger("Generator").info("number of variations in suffixes : "+suffixes.size());
		for(T pivot : pivots){
			Logger.getLogger("Generator").info("considering pivot:"+pivot.toString());
			List<T> entry = new ArrayList<>();
			entry.addAll(staticPart);
			entry.add(pivot);
			Pivot p = new Pivot();
			p.prefix=entry;
			p.suffixes=suffixes;
			result.add(p);
		}
		
		return result;
	}
	
	public LinkedList<LinkedList<T>> generate(LinkedList<LinkedList<T>> axes){
		Logger.getLogger("Generator").info("start variation generator for axes : "+axes.size());
		if(axes.isEmpty()){
			return new LinkedList<>();
		}
		
		LinkedList<LinkedList<T>> result = new LinkedList<>();
		
		List<T> axe = axes.removeFirst();
		LinkedList<LinkedList<T>> subs = generate(axes);
		
		if(subs.isEmpty()){
			for(T item : axe){
				result.add(new LinkedList<T>(){{add(item);}});
			}
			return result;
		}
		
		for(T item : axe){
			Logger.getLogger("Generator").info("considering axe item "+ item);
			for(LinkedList<T> sub:subs){
				LinkedList<T> clone = new LinkedList<>(sub);
				clone.addFirst(item);
				result.add(clone);
			}
		}
		
		return result;
	}
}
