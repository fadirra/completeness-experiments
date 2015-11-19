package it.unibz.inf.corner.experiment;

import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import it.unibz.inf.completeness.core.*;
import it.unibz.inf.completeness.index.inverted.*;
import it.unibz.inf.completeness.index.trie.*;

/**
 * Experimental evaluation program for completeness reasoning
 * 
 * Idea: We compare the running time of completeness reasoning without indexing, completeness reasoning with indexing, and query evaluation
 * 
 * @author Fariz Darari (fadirra@gmail.com)
 *
 */
public class Main {
	
	static List<String> listOfDBpediaProperties;
	static List<String> listOfDBpediaInstances;
	static int totalNumOfInstances = 1000;
	static String sparqlEndpoint = "http://semtech.inf.unibz.it:8890/sparql";
	
	public static void main(String[] args) {
		
		String programStartsAt = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().getTime());
		
		listOfDBpediaProperties = getPropertiesFromOntology("dbpedia_2014.owl"); // there are 2795 properties
		listOfDBpediaInstances = getInstancesFromDBpedia();
		
		Configuration config = ConfigurationHandler.getConfigurationExample();
		
		// variables for logging global experimental results: index building
		List<Long> avgPowersetMapListForIndexBuilding = new ArrayList<Long>();
		List<Long> avgTrieListForIndexBuilding = new ArrayList<Long>();
		List<Long> avgInvertedIndexListForIndexBuilding = new ArrayList<Long>();

		List<Long> medianPowersetMapListForIndexBuilding = new ArrayList<Long>();
		List<Long> medianTrieListForIndexBuilding = new ArrayList<Long>();
		List<Long> medianInvertedIndexListForIndexBuilding = new ArrayList<Long>();

		// variables for logging global experimental results: index searching
		List<Long> avgPowersetMapListForSearch = new ArrayList<Long>();
		List<Long> avgTrieListForSearch = new ArrayList<Long>();
		List<Long> avgInvertedIndexListForSearch = new ArrayList<Long>();

		List<Long> medianPowersetMapListForSearch = new ArrayList<Long>();
		List<Long> medianTrieListForSearch = new ArrayList<Long>();
		List<Long> medianInvertedIndexListForSearch = new ArrayList<Long>();
		
		int numOfBigIterations = 20; // how many times we have to repeat the experiment per configuration (to provide reliability)
		int[] dynamicList = config.getDynamicList();
		for(int variedParameterIndex = 0; variedParameterIndex < dynamicList.length; variedParameterIndex++) {
			
			// variables for logging local experimental results: index building
			List<Long> listOfInvertedIndexBuildingTime = new ArrayList<Long>();
			List<Long> listOfTrieIndexBuildingTime = new ArrayList<Long>();		
			List<Long> listOfHashmapIndexBuildingTime = new ArrayList<Long>();
			
			// variables for logging local experimental results: index searching
			List<Long> listOfInvertedIndexSearchTime = new ArrayList<Long>();
			List<Long> listOfTrieIndexSearchTime = new ArrayList<Long>();
			List<Long> listOfHashmapIndexSearchTime = new ArrayList<Long>();
			
			// variable for logging the number of relevant CSs
			List<Long> listOfRelevantCSs = new ArrayList<Long>();
			
			// variable for logging the reasoning time and query evaluation time
			List<Long> listOfReasoningTimeWithoutIndexing = new ArrayList<Long>();
			List<Long> listOfReasoningTimeWithIndexing = new ArrayList<Long>();
			List<Long> listOfQueryExecutionTime = new ArrayList<Long>();
			
			// parameters setup
			int numOfConstants = 1; // set hardcoded: number of constants a CS/query may have
			boolean repeating = true; // set hardcoded: allow repeated predicate IRIs?
			
			int queryLength;
			if(config.getQueryLength() < 0)
				queryLength = dynamicList[variedParameterIndex];
			else
				queryLength = config.getQueryLength();
			
			int numOfProperties;
			if(config.getNumOfProperties() < 0)
				numOfProperties = dynamicList[variedParameterIndex];
			else
				numOfProperties = config.getNumOfProperties();
			
			int numOfCSs;
			if(config.getNumOfCSs() < 0)
				numOfCSs = dynamicList[variedParameterIndex];
			else
				numOfCSs = config.getNumOfCSs();
			
			int minPattern;
			if(config.getMinPattern() < 0)
				minPattern = dynamicList[variedParameterIndex];
			else
				minPattern = config.getMinPattern();
			
			int maxPattern;
			if(config.getMaxPattern() < 0)
				maxPattern = dynamicList[variedParameterIndex];
			else
				maxPattern = config.getMaxPattern();
			
			int minCondition = config.getMinCondition();
			int maxCondition = config.getMaxCondition();
			
			// printing experiment status: first run
			if(variedParameterIndex == 0) {

				System.out.println("######################");
				System.out.println("First experiment run is of this configuration");
				System.out.println("######################");
				System.out.println("> query length: " + queryLength);
				System.out.println("> number of IRIs for predicates: " + numOfProperties);
				System.out.println("> number of CSs: " + numOfCSs);
				System.out.println("> CS length: " + (minPattern + minCondition) + "-" + (maxPattern + maxCondition));
				System.out.print("Check completeness? ");
				if(config.isCheckCompleteness())
					System.out.println("Yes");
				else
					System.out.println("No");
				System.out.println();

			}
			
			System.out.println("#######################");
			System.out.println(" >> Current dynamic parameter value: " + dynamicList[variedParameterIndex]);
			System.out.println("#######################");
			
			for(int bigIterationCnt = 0; bigIterationCnt < numOfBigIterations; bigIterationCnt++) {

				System.out.println(" >>> Iteration: " + bigIterationCnt);
				
				// generate completeness statements
				List<CompletenessStatement> csList = RandomGenerator.generateRandomCSs(numOfProperties, numOfConstants, numOfCSs, minPattern, maxPattern, minCondition, maxCondition, repeating);
				List<CompletenessStatement> dummyCsList = RandomGenerator.generateRandomCSs(1, 1, 1, 1, 1, 0, 0, true); // this dummy list of CSs is used when we want to just measure completeness reasoning, not the indexing
				
				// generate a random query
				String query = RandomGenerator.generateRandomQuery(numOfProperties, numOfConstants, queryLength, repeating);
				List<String> queryPredicates = Corner.getAllPropertiesFromQuery(query); // get all the (non-redundant + sorted) predicate IRIs of the query

				// placeholders for measuring simple running time
				long lStartTime;
				long lEndTime;
				
				// index building: powerset map
				lStartTime = System.nanoTime();
				PowersetMap powersetMap = new PowersetMap(csList);
				lEndTime = System.nanoTime();
				long hashmapIndexBuildingTime = lEndTime - lStartTime;
				listOfHashmapIndexBuildingTime.add(hashmapIndexBuildingTime); // logging, also below
				
				// index searching: powerset map
				lStartTime = System.nanoTime();
				List<CompletenessStatement> relevantCSListHashmap = powersetMap.search(queryPredicates);
				lEndTime = System.nanoTime();
				long hashmapIndexSearchTime = lEndTime - lStartTime;
				listOfHashmapIndexSearchTime.add(hashmapIndexSearchTime);
								
				// index building: inverted index
				lStartTime = System.nanoTime();
				InvertedIndexing invertedIndex = null;
				if(config.isCheckCompleteness()) // note for completeness checking, only one indexing (i.e., standard hashing above) is sufficient
					invertedIndex = new InvertedIndexing(dummyCsList);
				else
					invertedIndex = new InvertedIndexing(csList);
				lEndTime = System.nanoTime();
				long invertedIndexBuildingTime = lEndTime - lStartTime;			
				listOfInvertedIndexBuildingTime.add(invertedIndexBuildingTime);
				
				// index searching: inverted index
				lStartTime = System.nanoTime();
				List<CompletenessStatement> relevantCSListInvIndex = invertedIndex.search(queryPredicates);
				lEndTime = System.nanoTime();
				long invertedIndexingSearchTime = lEndTime - lStartTime;
				listOfInvertedIndexSearchTime.add(invertedIndexingSearchTime);
				
				// index building: trie
				lStartTime = System.nanoTime();
				Trie trie = null;
				if(config.isCheckCompleteness())
					trie = new Trie(dummyCsList);
				else
					trie = new Trie(csList);
				lEndTime = System.nanoTime();
				long trieIndexBuildingTime = lEndTime - lStartTime;
				listOfTrieIndexBuildingTime.add(trieIndexBuildingTime);
				
				// index searching: trie
				lStartTime = System.nanoTime();
				Set<CompletenessStatement> relevantCSListTrie = trie.search(queryPredicates);
				lEndTime = System.nanoTime();
				long trieIndexSearchTime = lEndTime - lStartTime;
				listOfTrieIndexSearchTime.add(trieIndexSearchTime);
				
				System.out.println("# Search results of predicate-relevant CSs:");
				System.out.println("- Standard Hashing = " + relevantCSListHashmap.size() + " CSs, search time " + hashmapIndexSearchTime + " ns");
				System.out.println("- Trie = " + relevantCSListTrie.size() + " CSs, search time " + trieIndexSearchTime + " ns");
				System.out.println("- Inverted Index = " + relevantCSListInvIndex.size() + " CSs, search time " +  invertedIndexingSearchTime + " ns");
				
				// if we want to perform completeness checking
				if(config.isCheckCompleteness()) {
				
					listOfRelevantCSs.add(new Long(relevantCSListHashmap.size()));
					
					System.out.println("# Comparison of completeness reasoning time and query evaluation time");
					
					// perform completeness checking w/o indexing
					lStartTime = System.nanoTime();
					Corner.isQueryComplete(csList, query);
					lEndTime = System.nanoTime();
					long withoutIndexingTime = lEndTime - lStartTime;
					listOfReasoningTimeWithoutIndexing.add(withoutIndexingTime);
					
					System.out.println("- Reasoning time without indexing (" + numOfCSs + " CSs) = " + withoutIndexingTime + " ns");
					
					// perform completeness checking w/ indexing
					lStartTime = System.nanoTime();
					Corner.isQueryComplete(relevantCSListHashmap, query);
					lEndTime = System.nanoTime();
					long withIndexingTime = lEndTime - lStartTime;
					listOfReasoningTimeWithIndexing.add(withIndexingTime);
					
					System.out.println("- Reasoning time with indexing (" + relevantCSListHashmap.size() + " CSs) = " + withIndexingTime + " ns (+ Retrieval time: " + hashmapIndexSearchTime + " ns) = " + (withIndexingTime + hashmapIndexSearchTime) + " ns");
					
					// execute the corresponding query over DBpedia
					String mappedQueryStr = mapToRealProperties(query, listOfDBpediaProperties); // first, map the random query to the random DBpedia query with DBpedia properties
					
					Query queryToCheck = QueryFactory.create(mappedQueryStr);
					QueryExecution qe = null;
					long queryExecTime;
					if(queryToCheck.isAskType()) {
						lStartTime = System.nanoTime();
						qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, mappedQueryStr);
						qe.execAsk();
						queryExecTime = System.nanoTime() - lStartTime;
					} else {
						lStartTime = System.nanoTime();
						qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, mappedQueryStr);
						ResultSet rs = qe.execSelect();
						while(rs.hasNext()){ // we must iterate as per https://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/query/QueryExecution.html#execSelect()
							rs.next();
						}
						queryExecTime = System.nanoTime() - lStartTime;
					}
					listOfQueryExecutionTime.add(queryExecTime);
					System.out.println("- Query execution time: " + queryExecTime + " ns");
					
				}

			}
				
			// logging results from standard hashing: average of index building time
			avgPowersetMapListForIndexBuilding.add(averageOf(listOfHashmapIndexBuildingTime));
			medianPowersetMapListForIndexBuilding.add(medianOf(listOfHashmapIndexBuildingTime));
			
			// logging results from standard hashing: average of index searching time
			avgPowersetMapListForSearch.add(averageOf(listOfHashmapIndexSearchTime));
			medianPowersetMapListForSearch.add(medianOf(listOfHashmapIndexSearchTime));
			
			// logging results from trie: average of index building time
			avgTrieListForIndexBuilding.add(averageOf(listOfTrieIndexBuildingTime));
			medianTrieListForIndexBuilding.add(medianOf(listOfTrieIndexBuildingTime));
			
			// logging results from trie: average of index searching time
			avgTrieListForSearch.add(averageOf(listOfTrieIndexSearchTime));
			medianTrieListForSearch.add(medianOf(listOfTrieIndexSearchTime));
			
			// logging results from inverted index: average of index building time
			avgInvertedIndexListForIndexBuilding.add(averageOf(listOfInvertedIndexBuildingTime));
			medianInvertedIndexListForIndexBuilding.add(medianOf(listOfInvertedIndexBuildingTime));
			
			// logging results from inverted index: average of index searching time
			avgInvertedIndexListForSearch.add(averageOf(listOfInvertedIndexSearchTime));
			medianInvertedIndexListForSearch.add(medianOf(listOfInvertedIndexSearchTime));
			
			// printing comparison results of completeness reasoning w/o indexing and w/ indexing
			if(config.isCheckCompleteness()) {
			
				System.out.println("########################################");
				System.out.println("Comparison of completeness reasoning without indexing VS with indexing VS query evaluation over " + numOfBigIterations + " iterations:");
				System.out.println("# AVG " + numOfCSs + " All CS(s) vs. " + averageOf(listOfRelevantCSs) + " Relevant CS(s)");
				System.out.println("# MED " + numOfCSs + " All CS(s) vs. " + medianOf(listOfRelevantCSs) + " Relevant CS(s)");
				System.out.println("# AVG " + averageOf(listOfReasoningTimeWithoutIndexing) + " ns vs. " + averageOf(listOfReasoningTimeWithIndexing) + " ns vs. " + averageOf(listOfQueryExecutionTime) + " ns");
				System.out.println("# MED " + medianOf(listOfReasoningTimeWithoutIndexing) + " ns vs. " + medianOf(listOfReasoningTimeWithIndexing) + " ns vs. " + medianOf(listOfQueryExecutionTime) + " ns");
				System.out.println("########################################");
	
			}
			
			// printing the configuration of last experiment run
			if(variedParameterIndex == (dynamicList.length - 1)) {
			
				System.out.println("######################");
				System.out.println("Last experiment run is of this configuration");
				System.out.println("######################");
				System.out.println("> query length: " + queryLength);
				System.out.println("> number of IRIs for predicates: " + numOfProperties);
				System.out.println("> number of CSs: " + numOfCSs);
				System.out.println("> CS length: " + (minPattern + minCondition) + "-" + (maxPattern + maxCondition));

			}

			
		}
		
		// print all measurement results
		System.out.println("\n########################################");
		System.out.println("FINAL RESULTS");
		System.out.println("########################################");
		
		String allResults = "";
		
		allResults = allResults + "INDEX BUILDING - AVERAGE\n";
		allResults += ", ";
		for(int i = 0; i < dynamicList.length; i++)
			allResults = allResults + dynamicList[i] + ", ";
		allResults += formatRow("PowersetMap", avgPowersetMapListForIndexBuilding);
		allResults += formatRow("Inverted", avgInvertedIndexListForIndexBuilding);
		allResults += formatRow("Trie", avgTrieListForIndexBuilding);
		
		allResults += "\n\n";
		
		allResults = allResults + "INDEX BUILDING - MEDIAN\n";
		allResults += ", ";
		for(int i = 0; i < dynamicList.length; i++)
			allResults = allResults + dynamicList[i] + ", ";
		allResults += formatRow("PowersetMap", medianPowersetMapListForIndexBuilding);
		allResults += formatRow("Inverted", medianInvertedIndexListForIndexBuilding);
		allResults += formatRow("Trie", medianTrieListForIndexBuilding);
		
		allResults += "\n\n";
		
		allResults = allResults + "INDEX SEARCHING - AVERAGE\n";
		allResults += ", ";
		for(int i = 0; i < dynamicList.length; i++)
			allResults = allResults + dynamicList[i] + ", ";
		allResults += formatRow("PowersetMap", avgPowersetMapListForSearch);
		allResults += formatRow("Inverted", avgInvertedIndexListForSearch);
		allResults += formatRow("Trie", avgTrieListForSearch);
		
		allResults += "\n\n";
		
		allResults = allResults + "INDEX SEARCHING - MEDIAN\n";
		allResults += ", ";
		for(int i = 0; i < dynamicList.length; i++)
			allResults = allResults + dynamicList[i] + ", ";
		allResults += formatRow("PowersetMap", medianPowersetMapListForSearch);
		allResults += formatRow("Inverted", medianInvertedIndexListForSearch);
		allResults += formatRow("Trie", medianTrieListForSearch);
		
		System.out.println(allResults);
		
		String programEndsAt = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().getTime());
		
		System.out.println("\nThe program starts at " + programStartsAt + "\nThe program ends at " + programEndsAt);
		
		// beeps when the experiments have finished
		beeping();
		
	}

	/**
	 * @param queryStr
	 * @param listOfProperties
	 * @return get the corresponding DBpedia query over a randomly generated query
	 */
	public static String mapToRealProperties(String queryStr,
			List<String> listOfRealProperties) {
		
		// map the constant constantly
//		queryStr = queryStr.replace("<http://example.org/constant0>", "<http://dbpedia.org/resource/Germany>");
		
		// map the constant randomly, this might avoid caching
		Random rand = new Random();
		queryStr = queryStr.replace("<http://example.org/constant0>", "<" + listOfDBpediaInstances.get(rand.nextInt(totalNumOfInstances)) + ">");
		
		// credit: http://stackoverflow.com/questions/6020384/create-array-of-regex-matches & Rido
		List<String> allMatches = new ArrayList<String>();
		Matcher m = Pattern.compile("<http://example.org/property[0-9]+>")
		     .matcher(queryStr);
		while (m.find())
			allMatches.add(m.group());
		
		// replace the integer properties with the corresponding DBpedia properties
		for(String match : allMatches) {
			String tempString = match.replace("<http://example.org/property", "");
			tempString = tempString.replace(">", "");
			int indexFromString = Integer.parseInt(tempString);
			queryStr = queryStr.replace(match, "<" + listOfRealProperties.get(indexFromString) + ">");
		}
		
		return queryStr;

	}

	/**
	 * Credit: http://www.programcreek.com/java-api-examples/index.php?api=com.hp.hpl.jena.query.ResultSet
	 * 
	 * @return instances of DBpedia
	 */
	public static List<String> getInstancesFromDBpedia() {
		List<String> dbpediaInstances = new ArrayList<String>();
		QueryExecution qe = null;
		qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, "SELECT ?instance WHERE { ?instance a <http://xmlns.com/foaf/0.1/Person> } LIMIT " + totalNumOfInstances);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()){
			QuerySolution sol = rs.nextSolution();
			dbpediaInstances.add(sol.getResource("instance").toString());
		}
		return dbpediaInstances;
	}
	
	/**
	 * @param location
	 * @return list of properties from an ontology
	 */
	private static List<String> getPropertiesFromOntology(String location) {
		List<String> listOfProperties = new ArrayList<String>();
		OntModel dbpediaOntology = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		try {
			InputStream inputStream = 
				      new FileInputStream(location);
			dbpediaOntology.read(inputStream, "RDF/XML"); 
		} catch (Exception e) {
           e.printStackTrace();
           System.exit(0);
		}
		Iterator<ObjectProperty> objectPropertyIt = dbpediaOntology.listObjectProperties();
		while(objectPropertyIt.hasNext()) {
			ObjectProperty objectProperty = objectPropertyIt.next();
			listOfProperties.add(objectProperty.getURI());
		}
		Iterator<DatatypeProperty> datatypePropertyIt = dbpediaOntology.listDatatypeProperties();
		while(datatypePropertyIt.hasNext()) {
			DatatypeProperty datatypeProperty = datatypePropertyIt.next();
			listOfProperties.add(datatypeProperty.getURI());
		}
		return listOfProperties;
	}

	/**
	 * @param resultList
	 * @return the median
	 */
	public static Long medianOf(List<Long> resultList) {
		
		Collections.sort(resultList);
		int listSize = resultList.size();
		Double middleIndex = Math.floor(listSize/2);
		
		if(listSize == 0) return Long.valueOf(0);
		else {
			if(listSize % 2 == 0) // if even
				return ( (resultList.get(middleIndex.intValue()-1) + resultList.get(middleIndex.intValue())) 
							/ 2 );
			else // if odd
				return resultList.get(middleIndex.intValue());
		}

	}

	/**
	 * @param resultList
	 * @return the arithmetic mean
	 */
	public static Long averageOf(List<Long> resultList) {
		Long sum = new Long(0);
		for(Long result : resultList) {
			sum = sum + result;
		}
		return sum/resultList.size();
	}

	/**
	 * @param firstColumn
	 * @param resultList
	 * @return csv-like formatting
	 */
	public static String formatRow(String firstColumn, List<Long> resultList) {
		String row = "\n" + firstColumn;
		for(Long result:resultList) {
			row = row + ", " + result;
		}
		return row;
	}
	
	/**
	 * this method beeps
	 * thanks to: http://stackoverflow.com/questions/3342651/how-can-i-delay-a-java-program-for-a-few-seconds
	 */
	public static void beeping() {
		int totalBeeps = 20;
		for(int howManyBeeps = 1; howManyBeeps <= totalBeeps; howManyBeeps++) {
			try {
			    Thread.sleep(100);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
}