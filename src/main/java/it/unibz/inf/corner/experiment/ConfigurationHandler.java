package it.unibz.inf.corner.experiment;

import org.apache.commons.lang3.ArrayUtils;

/**
 * This class stores experiment configurations
 * 
 * @author Fariz Darari (fadirra@gmail.com)
 *
 */
public class ConfigurationHandler {

	// hardcoded configuration for check completeness flag
	private static boolean checkCompleteness = true;
	
	public static boolean isCheckCompleteness() {
		return checkCompleteness;
	}

	public static void setCheckCompleteness(boolean checkCompleteness) {
		ConfigurationHandler.checkCompleteness = checkCompleteness;
	}
	
	// dummy parameters are needed to avoid cold-start results
	public static Configuration getConfigurationExample() {
		
		int[] dummyParameters = {10};
		int[] configDynamicList = ArrayUtils.addAll(dummyParameters, 10);
		int configNumOfProperties = -1;
		int configQueryLength = 3;
		int configNumOfCSs = 1000;
		int configMinPattern = 1;
		int configMaxPattern = 10;
		int configMinCondition = 0;
		int configMaxCondition = 0;
		boolean configCheckCompleteness = ConfigurationHandler.isCheckCompleteness();
		
		return new Configuration(configDynamicList,
				configNumOfProperties, 
				configQueryLength,
				configNumOfCSs,
				configMinPattern,
				configMaxPattern,
				configMinCondition,
				configMaxCondition,
				configCheckCompleteness);
		
	}
	
	public static Configuration getConfigurationIncreasingCS() {

		int[] dummyParameters = {100000, 100000, 100000, 100000, 100000};
		int[] configDynamicList = ArrayUtils.addAll(dummyParameters, 100000, 300000, 500000, 700000, 900000, 1100000);
		int configNumOfProperties = 2000;
		int configQueryLength = 3; // short query
//		int configQueryLength = 10; // long query
		int configNumOfCSs = -1;
		int configMinPattern = 1;
		int configMaxPattern = 10;
		int configMinCondition = 0;
		int configMaxCondition = 0;
		boolean configCheckCompleteness = ConfigurationHandler.isCheckCompleteness();
		
		return new Configuration(configDynamicList,
				configNumOfProperties, 
				configQueryLength,
				configNumOfCSs,
				configMinPattern,
				configMaxPattern,
				configMinCondition,
				configMaxCondition,
				configCheckCompleteness);
		
	}

	public static Configuration getConfigurationIncreasingPredicateIRIs() {

		int[] dummyParameters = {400, 400};
		int[] configDynamicList = ArrayUtils.addAll(dummyParameters, 400, 800, 1200, 1600, 2000, 2400, 2800);
		int configNumOfProperties = -1;
		//int configQueryLength = 3; // short query
		int configQueryLength = 10; // long query
		int configNumOfCSs = 1000000;
		int configMinPattern = 1;
		int configMaxPattern = 10;
		int configMinCondition = 0;
		int configMaxCondition = 0;
		boolean configCheckCompleteness = ConfigurationHandler.isCheckCompleteness();
		
		return new Configuration(configDynamicList,
				configNumOfProperties, 
				configQueryLength,
				configNumOfCSs,
				configMinPattern,
				configMaxPattern,
				configMinCondition,
				configMaxCondition,
				configCheckCompleteness);
		
	}
	
	public static Configuration getConfigurationIncreasingCSLength() {

		int[] dummyParameters = {1, 1};
		int[] configDynamicList = ArrayUtils.addAll(dummyParameters, 1, 3, 5, 7, 9, 11);
		int configNumOfProperties = 2000;
		//int configQueryLength = 3; // short query
		int configQueryLength = 10; // long query
		int configNumOfCSs = 1000000;
		int configMinPattern = 1;
		int configMaxPattern = -1;
		int configMinCondition = 0;
		int configMaxCondition = 0;
		boolean configCheckCompleteness = ConfigurationHandler.isCheckCompleteness();
		
		return new Configuration(configDynamicList,
				configNumOfProperties, 
				configQueryLength,
				configNumOfCSs,
				configMinPattern,
				configMaxPattern,
				configMinCondition,
				configMaxCondition,
				configCheckCompleteness);
		
	}

	public static Configuration getConfigurationIncreasingQueryLength() {

		int[] dummyParameters = {1, 1};
		int[] configDynamicList = ArrayUtils.addAll(dummyParameters, 1, 4, 7, 10, 13, 16, 19, 22);
		int configNumOfProperties = 2000;
		int configQueryLength = -1;
		int configNumOfCSs = 1000000;
		int configMinPattern = 1;
		int configMaxPattern = 10;
		int configMinCondition = 0;
		int configMaxCondition = 0;
		boolean configCheckCompleteness = ConfigurationHandler.isCheckCompleteness();
		
		return new Configuration(configDynamicList,
				configNumOfProperties, 
				configQueryLength,
				configNumOfCSs,
				configMinPattern,
				configMaxPattern,
				configMinCondition,
				configMaxCondition,
				configCheckCompleteness);
		
	}

	public static Configuration getConfigurationCompareReasoningWithAndWithoutIndexing() {

		int[] configDynamicList = {10, 1000, 1000000};
		int configNumOfProperties = 2000;
		int configQueryLength = 3; // short queries
		//int configQueryLength = 10; // long queries
		int configNumOfCSs = -1;
		int configMinPattern = 1;
		int configMaxPattern = 10;
		int configMinCondition = 0;
		int configMaxCondition = 0;
		boolean configCheckCompleteness = true;
		
		return new Configuration(configDynamicList,
				configNumOfProperties, 
				configQueryLength,
				configNumOfCSs,
				configMinPattern,
				configMaxPattern,
				configMinCondition,
				configMaxCondition,
				configCheckCompleteness);
		
	}

}
