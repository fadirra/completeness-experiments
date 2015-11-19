package it.unibz.inf.corner.experiment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import it.unibz.inf.completeness.core.*;

// Algorithm overview:
// 1. Generate IRIs for predicates.
// 2. For each triple pattern to be generated, the predicate IRI is taken randomly from the generated IRIs,
//    whereas its subject and object are either variables (either new or used ones) or constants

public class RandomGenerator {

	static int variableCounter = 0;
	static String baseURIProperty = "http://example.org/property";
	static String baseURIConstant = "http://example.org/constant";
	static String baseVariableName = "X";

	public static List<Property> generateIRIsPoolForPredicates(int mProperty){
		
    	List<Property> IRIsForPredicates = new ArrayList<Property>();
    	for(int i = 0; i < mProperty; i++) {
    		Property p = ResourceFactory.createProperty(baseURIProperty + i);
    		IRIsForPredicates.add(p);
    	}
    	return IRIsForPredicates;
		
	}
	
	public static List<RDFNode> generateIRIsPoolForSubjectsAndObjects(int nConstant) {
		
    	List<RDFNode> constantList = new ArrayList<RDFNode>();    	
    	for(int i = 0; i < nConstant; i++) {
    		RDFNode c = ResourceFactory.createResource(baseURIConstant + i);
    		constantList.add(c);
    	}
    	return constantList;
		
	}
	
	// generate a list of random statements from m IRIs for predicates and n IRIs for subjects and objects
	public static List<CompletenessStatement> generateRandomCSs(int mProperty, int nConstant, int numOfCSs, int minPattern, int maxPattern, int minCondition, int maxCondition, boolean repeating) {
		
		List<Property> IRIsForPredicates = generateIRIsPoolForPredicates(mProperty);
		List<RDFNode> constantList = generateIRIsPoolForSubjectsAndObjects(nConstant);
		
		List<CompletenessStatement> csList = new ArrayList<CompletenessStatement>();		
		Random randomizer = new Random();

    	for(int csCounter = 0; csCounter < numOfCSs; csCounter++) {
    		
//    		System.out.println("Generating CS " + csCounter);
    		
    		// reset the variable counter
    		variableCounter = 0;
    		
	    	int numOfPattern = randomizer.nextInt(maxPattern-minPattern+1) + minPattern; // add 1 because of the exclusivity of nextInt
	    	int numOfCondition = randomizer.nextInt(maxCondition-minCondition+1) + minCondition;
	    	int length = numOfPattern + numOfCondition;
	    	
	    	// generate a random body for the CS
	    	List<Triple> bgp = generateRandomBGP(new ArrayList<Property>(IRIsForPredicates), constantList, length, repeating);

	    	List<Triple> genPattern = bgp.subList(0, numOfPattern);
	    	List<Triple> genCondition = bgp.subList(numOfPattern, length);
	    	CompletenessStatement genCS = new CompletenessStatement(genPattern, genCondition);	    	
	    	csList.add(genCS);
	    	
		}
    	
		return csList;
		
	}
	
	// generate a random query
	public static String generateRandomQuery(int mProperty, int nConstant, int length, boolean repeating) {
		
		// reset the variable counter
		variableCounter = 0;
		
		List<Property> IRIsForPredicates = generateIRIsPoolForPredicates(mProperty);
		List<RDFNode> constantList = generateIRIsPoolForSubjectsAndObjects(nConstant);
		
		List<Triple> tpList = generateRandomBGP(IRIsForPredicates, constantList, length, repeating);
		
		// generate the head
		String headString 	= "";
    	List<Node> varList = getAllVariables(tpList);
    	if(varList.isEmpty()) {
    		headString = "ASK ";
    	}
    	else {
    		headString 	= "SELECT ";
    		for(Node var : varList) {
    			headString = headString + var + " ";
    		}
    	}
    	
    	// generate the body
    	String bodyString 	= " WHERE { ";
    	for(Triple tp : tpList) {
    		bodyString = bodyString + CompletenessStatement.formatTriplePatternForConcreteQuery(tp) + " . ";
    	}   		    		    	
   
		return headString + bodyString + "}";
	
	}
	
	// get a random constant from a constant list
	public static Node getRandomConstant(List<RDFNode> constantList) {	
		Random randomizer = new Random();
		RDFNode randomConstant = constantList.get(randomizer.nextInt(constantList.size()));
		return NodeFactory.createURI(randomConstant.toString());
	}
	
	// get all variables in a triple pattern list
	private static List<Node> getAllVariables(List<Triple> tpList) {
		
		Set<Node> nodeSet = new HashSet<Node>();
		
		for(Triple tp : tpList) {
			Node s = tp.getSubject();
			Node o = tp.getObject();
			if(s.isVariable())
				nodeSet.add(s);
			if(o.isVariable())
				nodeSet.add(o);
		}
		
		return new ArrayList<Node>(nodeSet);
	}
	
	// random generator of a boolean value
	public static boolean trueOrFalse() {
		Random randomizer = new Random();		
		if(randomizer.nextInt(2) == 1)
			return true;
		else return false;
	}	
	
	// recursive function to generate a random BGP based on a property list and a constant list
	public static List<Triple> generateRandomBGP(List<Property> propertyList, List<RDFNode> constantList, int length, boolean repeating) {
		
		Random randomizer = new Random();
		List<Triple> bgp = new ArrayList<Triple>();
		
		// base case
		if(length == 1) {
			
			// pick an IRI for a predicate randomly
			Property pAsProperty = propertyList.get(randomizer.nextInt(propertyList.size()));
			Node p = pAsProperty.asNode();
			if(repeating == false)
				propertyList.remove(pAsProperty);
			
			Node node1;
			Node node2;
			
			// generate a node for the triple pattern, randomly decide whether it should be a var or a constant
			boolean var = trueOrFalse();
			if(var) {
				node1 = NodeFactory.createVariable(baseVariableName + (variableCounter++));
			}
			else {
				node1 = getRandomConstant(constantList);
			}
			
			// now generate the other node
			var = trueOrFalse();
			if(var) {
				
				// the var can be fresh or the same as tempS (if tempS is also a var)
				boolean fresh = trueOrFalse();
				if(fresh || !node1.isVariable())
					node2 = NodeFactory.createVariable(baseVariableName + (variableCounter++));
				else
					node2 = NodeFactory.createVariable(node1.getName());
				
			}
			else {
				node2 = getRandomConstant(constantList);
			}
				
			bgp.add(generateTriple(node1, node2, p));
				
		}
		
		// recursion case: similar to the base case but with subtle differences
		else {
			
			List<Triple> bgpRecursive = generateRandomBGP(propertyList, constantList, length-1, repeating);
			List<Node> variableListRecursive = getAllVariables(bgpRecursive);
			
			// pick an IRI for a predicate randomly
			Property pAsProperty = propertyList.get(randomizer.nextInt(propertyList.size()));
			Node p = pAsProperty.asNode();
			if(repeating == false)
				propertyList.remove(pAsProperty);
			
			Node node1;
			Node node2;
			
			// generate the first node
			boolean var = trueOrFalse();
			
			// if a variable
			if(var) {
				// if there's no variable before, create a fresh var
				if(variableListRecursive.size() == 0) {
					node1 = NodeFactory.createVariable(baseVariableName + (variableCounter++));
					variableListRecursive.add(NodeFactory.createVariable(node1.getName()));
				}
				// there must be at least a join with an existing variable to avoid cross product join
				else {
					Node randomVarS = variableListRecursive.get(randomizer.nextInt(variableListRecursive.size()));
					node1 = NodeFactory.createVariable(randomVarS.getName());
				}
			}
			// if a constant
			else {
				node1 = getRandomConstant(constantList);
			}
			
			// generate the second node
			var = trueOrFalse();
			if(var) {
				boolean fresh = trueOrFalse();
				
				// if s is a var, then there is no cross-product join
				if((node1.isVariable() && fresh) || (variableListRecursive.size() == 0))
					node2 = NodeFactory.createVariable(baseVariableName + (variableCounter++));
				
				// if s is no var, then o must be taken from existing variables, otherwise there will be a cross product join!
				else {
					Node randomVarO = variableListRecursive.get(randomizer.nextInt(variableListRecursive.size()));
					node2 = NodeFactory.createVariable(randomVarO.getName());
				}
			}
			else {
				node2 = getRandomConstant(constantList);
			}
			
			bgp.addAll(bgpRecursive);
			bgp.add(generateTriple(node1, node2, p));
			
		}
		
		return bgp;
		
	}
	
	// generate a triple where the choice whether node1/node2 is the subject and node1/node2 is the object is random
	public static Triple generateTriple(Node node1, Node node2, Node p) {
		
		// randomly choose which is generated first, the subject or the object
		boolean subjectFirst = trueOrFalse();
		
		Triple generatedTriple = null;
		if(subjectFirst) {
			generatedTriple = new Triple(node1, p, node2);
		} else {
			generatedTriple = new Triple(node2, p, node1);
		}
		return generatedTriple;
		
	}
	
}