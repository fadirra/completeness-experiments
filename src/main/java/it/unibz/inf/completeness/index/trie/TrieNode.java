package it.unibz.inf.completeness.index.trie;

import java.util.HashMap;
import java.util.List;

public class TrieNode {

	// the label is a prefix
	List<String> prefix;
	
	// the children are represented using a hashmap
	HashMap<List<String>, TrieNode> children = new HashMap<List<String>, TrieNode>();

	public TrieNode(List<String> prefix) {
		this.prefix = prefix;
	}
	
	public void addNewChild(List<String> newChild) {
		children.put(newChild, new TrieNode(newChild));
	}
	
	public List<String> getPrefix() {
		return prefix;
	}
	
	public HashMap<List<String>, TrieNode> getChildren() {
		return children;
	}
	
	// return null if there is no child with the prefix
	public TrieNode getChild(List<String> prefix) {
		return children.get(prefix);
	}
	
}