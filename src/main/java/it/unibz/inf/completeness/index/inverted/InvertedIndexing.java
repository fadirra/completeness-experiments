package it.unibz.inf.completeness.index.inverted;

import it.unibz.inf.completeness.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Guava library, see also https://github.com/google/guava
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class InvertedIndexing {

	// hash map from predicate IRIs to respective bag of completeness statements
	private HashMap<String, Multiset<CompletenessStatement>> hashMap = new HashMap<String, Multiset<CompletenessStatement>>();
	
	// construct the index structure
	public InvertedIndexing(List<CompletenessStatement> csList) {
		
		// iterate over all completeness statements
		for(CompletenessStatement cs : csList) {
			
			// iterate over all predicates (as a bag) in the completeness statement
			for(String predicate : cs.getPredicateList()) {
				
				// store the completeness statement in the value of the predicate key wrt the hashmap
				if(hashMap.containsKey(predicate)) {
					hashMap.get(predicate).add(cs);
				}
				else {
					Multiset<CompletenessStatement> csBag = HashMultiset.create();
					csBag.add(cs);
					hashMap.put(predicate, csBag);
				}
				
			}
			
		}
		
	}
	
	// subset-querying over the index structure
	// see also "A performance study of four index structures for set-valued attributes of low cardinality"
	// url: http://dx.doi.org/10.1007/s00778-003-0106-0
	public List<CompletenessStatement> search(List<String> predicateListOfQuery) {

		// computes the bag union of the mapping of all predicates in Q
		Multiset<CompletenessStatement> csBagWrtQ = HashMultiset.create();
		for(String predicate : predicateListOfQuery) {
			if(hashMap.containsKey(predicate))
				csBagWrtQ.addAll(hashMap.get(predicate));
		}
		
		// uncomment to print the size of B_Q
//		System.out.println("B_Q = " + csBagWrtQ.size());
		
		// contains all the predicate-relevant completeness statements
		List<CompletenessStatement> csList = new ArrayList<CompletenessStatement>();
		for(CompletenessStatement cs : csBagWrtQ.elementSet()) {
			if(csBagWrtQ.count(cs) == cs.getLength())
				csList.add(cs);
		}
		
		return csList;
		
	}
	
	public HashMap<String, Multiset<CompletenessStatement>> getHashMap() {
		return hashMap;
	}

}