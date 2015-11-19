package it.unibz.inf.completeness.index.trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibz.inf.completeness.core.*;
import it.unibz.inf.completeness.index.inverted.PowersetMap;

public class Trie {

	private HashMap<List<String>, List<CompletenessStatement>> hashmap = new HashMap<List<String>, List<CompletenessStatement>>();
	private TrieNode root = new TrieNode(new ArrayList<String>());
	
	// construct a trie
	public Trie(List<CompletenessStatement> csList) {
		
		// the hashmap is the same as that of the PowersetMap
		hashmap = PowersetMap.generateHashmap(csList);
		
		for(List<String> sequence : hashmap.keySet()) {
			this.insert(sequence);
		}
		
	}
	
	// insert a single sequence into the trie
	public void insert(List<String> stringList) {
		
		TrieNode current = root;

		for(int i = 0; i < stringList.size(); i++) {
			
			List<String> increasingPrefix = stringList.subList(0, i + 1);
			TrieNode child = current.getChild(increasingPrefix);
			if(child != null) {
				current = child;
			}
			else {
				current.addNewChild(increasingPrefix);
				current = current.getChild(increasingPrefix);
			}
		
		}
		
	}
	
	// used to store predicate-relevant statements during a search
	Set<CompletenessStatement> relevantCSSet;
	
	// search for predicate-relevant statements
	public Set<CompletenessStatement> search(List<String> listOfQueryPredicates) {
		relevantCSSet = new HashSet<CompletenessStatement>();
		recSearch(root, listOfQueryPredicates);
		return relevantCSSet;
	}
	
	public void recSearch(TrieNode node, List<String> listOfQueryPredicates) {
		
		// base case
		if(listOfQueryPredicates.size() == 0) {
			
			if(hashmap.containsKey(node.getPrefix())) {
				relevantCSSet.addAll(hashmap.get(node.getPrefix()));
			}
			
		}
		
		// recursion case
		else {
			
			if(hashmap.containsKey(node.getPrefix())) {
				relevantCSSet.addAll(hashmap.get(node.getPrefix()));
			}
			
			List<String> decreasingListOfQueryPredicates = listOfQueryPredicates.subList(1, listOfQueryPredicates.size());
			
			// search over the same level with decreasing query sequence
			recSearch(node, decreasingListOfQueryPredicates);
			
			// search over a deeper level with decreasing query sequence 
			List<String> increasingPrefix = new ArrayList<String>();
			increasingPrefix.addAll(node.getPrefix());
			increasingPrefix.add(listOfQueryPredicates.get(0));
			TrieNode childNode = node.getChild(increasingPrefix);
			if(childNode != null)
				recSearch(childNode, decreasingListOfQueryPredicates);
			
		}
		
	}
	
}