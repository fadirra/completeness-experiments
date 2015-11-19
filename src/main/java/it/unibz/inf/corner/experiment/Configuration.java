package it.unibz.inf.corner.experiment;

public class Configuration {
	
	// the dynamic list by convention is used to populate the variable with value -1
	int[] dynamicList;
	int numOfProperties;
	int queryLength;
	int numOfCSs;
	int minPattern;
	int maxPattern;
	int minCondition;
	int maxCondition;
	boolean checkCompleteness; // if false, only perform indexing; if true, also perform completeness reasoning (may take much longer)
	
	public Configuration(int[] dynamicList,
			int numOfProperties, 
			int queryLength,
			int numOfCSs,
			int minPattern,
			int maxPattern,
			int minCondition,
			int maxCondition,
			boolean checkCompleteness) {
		
		this.dynamicList = dynamicList;
		this.numOfProperties = numOfProperties;
		this.queryLength = queryLength;
		this.numOfCSs = numOfCSs;
		this.minPattern = minPattern;
		this.maxPattern = maxPattern;
		this.minCondition = minCondition;
		this.maxCondition = maxCondition;
		this.checkCompleteness = checkCompleteness;
		
	}

	public int[] getDynamicList() {
		return dynamicList;
	}

	public void setDynamicList(int[] dynamicList) {
		this.dynamicList = dynamicList;
	}

	public int getNumOfProperties() {
		return numOfProperties;
	}

	public void setNumOfProperties(int numOfProperties) {
		this.numOfProperties = numOfProperties;
	}

	public int getQueryLength() {
		return queryLength;
	}

	public void setQueryLength(int queryLength) {
		this.queryLength = queryLength;
	}

	public int getNumOfCSs() {
		return numOfCSs;
	}

	public void setNumOfCSs(int numOfCSs) {
		this.numOfCSs = numOfCSs;
	}

	public int getMinPattern() {
		return minPattern;
	}

	public void setMinPattern(int minPattern) {
		this.minPattern = minPattern;
	}

	public int getMaxPattern() {
		return maxPattern;
	}

	public void setMaxPattern(int maxPattern) {
		this.maxPattern = maxPattern;
	}

	public int getMinCondition() {
		return minCondition;
	}

	public void setMinCondition(int minCondition) {
		this.minCondition = minCondition;
	}

	public int getMaxCondition() {
		return maxCondition;
	}

	public void setMaxCondition(int maxCondition) {
		this.maxCondition = maxCondition;
	}

	public boolean isCheckCompleteness() {
		return checkCompleteness;
	}

	public void setCheckCompleteness(boolean checkCompleteness) {
		this.checkCompleteness = checkCompleteness;
	}
		
}
