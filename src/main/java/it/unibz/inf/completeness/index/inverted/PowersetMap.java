package it.unibz.inf.completeness.index.inverted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unibz.inf.completeness.core.*;

public class PowersetMap {

	private HashMap<List<String>, List<CompletenessStatement>> hashmap = new HashMap<List<String>, List<CompletenessStatement>>();
	
	public static HashMap<List<String>, List<CompletenessStatement>> generateHashmap(List<CompletenessStatement> csList) {
		
		HashMap<List<String>, List<CompletenessStatement>> generatedHashmap = new HashMap<List<String>, List<CompletenessStatement>>();
		
		for(CompletenessStatement cs : csList) {
			
			// getUniquePredicateList() already returns a distinct and sorted list
			List<String> predicateList = cs.getUniquePredicateList();
			if(generatedHashmap.containsKey(predicateList))
				generatedHashmap.get(predicateList).add(cs);
			else {
				List<CompletenessStatement> csListWRTPredicateList = new ArrayList<CompletenessStatement>();
				csListWRTPredicateList.add(cs);
				generatedHashmap.put(predicateList, csListWRTPredicateList);
			}
			
		}
		
		return generatedHashmap;
		
	}
	
	public PowersetMap(List<CompletenessStatement> csList) {
		hashmap = generateHashmap(csList);
	}
	
	public HashMap<List<String>, List<CompletenessStatement>> getHashmap() {
		return hashmap;
	}
	
	private List<CompletenessStatement> relevantCSList;
	
	public List<CompletenessStatement> search(List<String> predicateListOfQuery) {
		relevantCSList = new ArrayList<CompletenessStatement>();
		recSearch(new ArrayList<String>(), predicateListOfQuery, 0);
		return relevantCSList;
	}

	// credit to Werner Nutt: idea is flipping bits between 0 and 1
	private void recSearch(List<String> prefix,
			List<String> predicateListOfQuery, int level) {
		
		if(level > predicateListOfQuery.size() - 1) {
			if(hashmap.containsKey(prefix))
				relevantCSList.addAll(hashmap.get(prefix));
		}
		else {
			List<String> nonGrowingPrefix = new ArrayList<String>();
			List<String> growingPrefix = new ArrayList<String>();
			nonGrowingPrefix.addAll(prefix);
			growingPrefix.addAll(prefix);
			growingPrefix.add(predicateListOfQuery.get(level));
			recSearch(nonGrowingPrefix, predicateListOfQuery, level + 1);
			recSearch(growingPrefix, predicateListOfQuery, level + 1);
		}
		
	}
		
}