package edu.neu.ece.sparqlquerygenerator.evaluator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.COWLClassImpl;
import edu.neu.ece.sparqlquerygenerator.entity.COWLDataPropertyImpl;
import edu.neu.ece.sparqlquerygenerator.entity.COWLObjectPropertyImpl;
import edu.neu.ece.sparqlquerygenerator.entity.COWLPropertyImpl;
import edu.neu.ece.sparqlquerygenerator.generator.SPARQLQueryGenerator;
import edu.neu.ece.sparqlquerygenerator.utility.MathUtil;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataExactCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataHasValueImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectExactCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasSelfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasValueImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectInverseOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMaxCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectOneOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

/**
 * This class is defined exclusively for query evaluation.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-07-29
 */
public class Evaluator {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * An array list of queries.
	 */
	private final ArrayList<Query> queries;

	/**
	 * Selected root class in the input ontology as the entry for dataset
	 * generation.
	 */
	private final COWLClassImpl rootClass;

	/**
	 * The number of queries.
	 */
	private final int queryNumber;

	/**
	 * OWL data factory for creating entities, class expressions and axioms.
	 */
	private final OWLDataFactory factory;

	/**
	 * Container that stores key-value pairs, where OWL API interface OWLClass is
	 * the key and the customized class COWLClassImpl is the value.
	 */
	private final Map<OWLClass, COWLClassImpl> classMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 */
	private final Map<OWLDataProperty, COWLDataPropertyImpl> dataPropertyMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 */
	private final Map<OWLObjectProperty, COWLObjectPropertyImpl> objectPropertyMap;

	/**
	 * Mapping from focus class to its visit status (true if visited, false
	 * otherwise).
	 */
	private Map<String, Boolean> targetClassAndVisitStatus = new TreeMap<>();

	/**
	 * Mapping from focus data property to its visit status (true if visited, false
	 * otherwise).
	 */
	private Map<String, Boolean> targetDataPropertyAndVisitStatus = new TreeMap<>();

	/**
	 * Mapping from focus object property to its visit status (true if visited,
	 * false otherwise).
	 */
	private Map<String, Boolean> targetObjectPropertyAndVisitStatus = new TreeMap<>();

	/**
	 * Non-focus classes that are used in the queries for testing purpose.
	 */
	private Set<String> nonTargetClasses = new TreeSet<>();

	/**
	 * Non-focus properties that are used in the queries.
	 */
	private Set<String> nonTargetProperties = new TreeSet<>();

	/**
	 * The nodes that are visited while traversing the model graph from the root
	 * class .
	 */
	private Set<Object> visitedNodes = new HashSet<>();

	/**
	 * Class coverage metric.
	 */
	private double classCoverage = 0.00;

	/**
	 * Data property coverage metric.
	 */
	private double dataPropertyCoverage = 0.00;

	/**
	 * Object property coverage metric.
	 */
	private double objectPropertyCoverage = 0.00;

	/**
	 * Keyword FILTER coverage metric.
	 */
	private double keywordFILTERCoverage = 0.00;

	/**
	 * Keyword AND coverage metric.
	 */
	private double keywordANDCoverage = 0.00;

	/**
	 * Keyword UNION coverage metric.
	 */
	private double keywordUNIONCoverage = 0.00;

	/**
	 * Keyword OPTIONAL coverage metric.
	 */
	private double keywordOPTIONALCoverage = 0.00;

	/**
	 * Keyword MINUS coverage metric.
	 */
	private double keywordMINUSCoverage = 0.00;

	/**
	 * Keyword NOT EXISTS coverage metric.
	 */
	private double keywordNOTEXISTSCoverage = 0.00;

	/**
	 * Keyword EXISTS coverage metric.
	 */
	private double keywordEXISTSCoverage = 0.00;

	/**
	 * Proportion of each of the 16 subsets of operator set {FILTER, AND, OPTIONAL,
	 * UNION}.
	 */
	private double[] targetOperatorSetDistribution = { 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
			0.00, 0.00, 0.00, 0.00, 0.00 };

	/**
	 * Proportion of queries that contain triple patterns numbers 1-3, 4-6, 7-9,
	 * 10-12, 13-15 and 16+, respectively.
	 */
	private double[] triplePatternAmountDistribution = { 1.00, 0.00, 0.00, 0.00, 0.00, 0.00 };

	/**
	 * Proportion of queries with join amount 0-4, 5-9, 10-14, 15-19, 20-24 and 25+,
	 * respectively.
	 */
	private double[] joinAmountDistribution = { 1.00, 0.00, 0.00, 0.00, 0.00, 0.00 };

	/**
	 * Proportion of the six join type: Subject-Subject(SS),
	 * Predicate-Predicate(PP), Object-Object(OO), Subject-Predicate(SP),
	 * Subject-Object(SO) and Predicate-Object(PO).
	 */
	private double[] tripleJointypeDistribution = { 1.00, 0.00, 0.00, 0.00, 0.00, 0.00 };

	/**
	 * Constructor
	 * 
	 * @param queries
	 *            An array list of queries.
	 * @param generator
	 *            Query generator.
	 */
	public Evaluator(ArrayList<Query> queries, SPARQLQueryGenerator generator) {
		this.queries = queries;
		rootClass = generator.getRootClass();
		queryNumber = generator.getQueryNumber();
		factory = generator.getFactory();
		classMap = generator.getClassMap();
		dataPropertyMap = generator.getDataPropertyMap();
		objectPropertyMap = generator.getObjectPropertyMap();
	}

	/**
	 * This function defines the whole control flow of the evaluation process.
	 * 
	 * @throws Exception
	 *             If there exists invalid input arguments.
	 */
	public void evaluate() throws Exception {
		logger.info("Begin evaluating the generated queries...");
		findTargetSignatures(factory.getOWLClass(rootClass.getIRI()));
		ArrayList<QueryProperty> queryProperties = generateQueryProperties();
		// for(int i = 0; i < queryProperties.size(); i++)
		// System.out.println(queryProperties.get(i).toString());
		collectMetrics(queryProperties);
		printToFile();
	}

	/**
	 * This function utilizes DFS algorithm to find target signatures in which
	 * concepts are reachable from the root class.
	 * 
	 * @param <T>
	 *            The class of the node.
	 * @param node
	 *            Node of generic type T.
	 */
	private <T> void findTargetSignatures(T node) {
		if (node == null)
			return;
		visitedNodes.add(node);

		if (node instanceof OWLClassImpl) {
			OWLClassImpl clsImpl = (OWLClassImpl) node;
			if (clsImpl.isOWLThing() || clsImpl.isOWLNothing())
				return;
			targetClassAndVisitStatus.put(clsImpl.getIRI().getIRIString(), Boolean.FALSE);
			COWLClassImpl owlClsImpl = classMap.get(clsImpl);
			for (COWLClassImpl sub : owlClsImpl.getSubClasses()) {
				OWLClass cls = factory.getOWLClass(sub.getIRI());
				if (!visitedNodes.contains(cls))
					findTargetSignatures(cls);
			}
			for (COWLClassImpl sup : owlClsImpl.getSuperClasses()) {
				OWLClass cls = factory.getOWLClass(sup.getIRI());
				if (!visitedNodes.contains(cls))
					findTargetSignatures(cls);
			}
			for (OWLClassExpression exp : owlClsImpl.getEquivalentClasses())
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
			for (OWLClassExpression exp : owlClsImpl.getDisjointClasses())
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
			for (OWLClassExpression exp : owlClsImpl.getAnonymousSuperClasses())
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
			for (Entry<OWLObjectProperty, OWLClassExpression> entry : owlClsImpl.getObjectPropertyRangesPairs()
					.entrySet())
				if (!visitedNodes.contains(entry))
					findTargetSignatures(entry);
			for (Entry<OWLDataProperty, OWLDataRange> entry : owlClsImpl.getDataPropertyRangesPairs().entrySet())
				if (!visitedNodes.contains(entry))
					findTargetSignatures(entry);
		} else if (node instanceof OWLObjectIntersectionOfImpl) {
			for (OWLClassExpression exp : ((OWLObjectIntersectionOfImpl) node).operands().collect(Collectors.toSet()))
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
		} else if (node instanceof OWLObjectUnionOfImpl) {
			for (OWLClassExpression exp : ((OWLObjectUnionOfImpl) node).operands().collect(Collectors.toSet()))
				if (!visitedNodes.contains(exp))
					findTargetSignatures(exp);
		} else if (node instanceof OWLObjectComplementOfImpl) {
			OWLClassExpression exp = ((OWLObjectComplementOfImpl) node).getOperand();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
		} else if (node instanceof OWLObjectOneOfImpl) {
			return;
		} else if (node instanceof OWLObjectSomeValuesFromImpl) {
			OWLObjectSomeValuesFromImpl res = (OWLObjectSomeValuesFromImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectAllValuesFromImpl) {
			OWLObjectAllValuesFromImpl res = (OWLObjectAllValuesFromImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectHasValueImpl) {
			OWLObjectHasValueImpl res = (OWLObjectHasValueImpl) node;
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectHasSelfImpl) {
			OWLObjectHasSelfImpl res = (OWLObjectHasSelfImpl) node;
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectMinCardinalityImpl) {
			OWLObjectMinCardinalityImpl res = (OWLObjectMinCardinalityImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectMaxCardinalityImpl) {
			OWLObjectMaxCardinalityImpl res = (OWLObjectMaxCardinalityImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectExactCardinalityImpl) {
			OWLObjectExactCardinalityImpl res = (OWLObjectExactCardinalityImpl) node;
			OWLClassExpression exp = res.getFiller();
			if (!visitedNodes.contains(exp))
				findTargetSignatures(exp);
			OWLObjectPropertyExpression prop = res.getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataSomeValuesFromImpl) {
			OWLDataPropertyExpression prop = ((OWLDataSomeValuesFromImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataAllValuesFromImpl) {
			OWLDataPropertyExpression prop = ((OWLDataAllValuesFromImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataHasValueImpl) {
			OWLDataPropertyExpression prop = ((OWLDataHasValueImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataMinCardinalityImpl) {
			OWLDataPropertyExpression prop = ((OWLDataMinCardinalityImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataMaxCardinalityImpl) {
			OWLDataPropertyExpression prop = ((OWLDataMaxCardinalityImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLDataExactCardinalityImpl) {
			OWLDataPropertyExpression prop = ((OWLDataExactCardinalityImpl) node).getProperty();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectInverseOfImpl) {
			OWLObjectProperty prop = ((OWLObjectInverseOfImpl) node).getInverse();
			if (!visitedNodes.contains(prop))
				findTargetSignatures(prop);
		} else if (node instanceof OWLObjectPropertyImpl) {
			OWLObjectPropertyImpl propImpl = (OWLObjectPropertyImpl) node;
			if (propImpl.isOWLTopObjectProperty() || propImpl.isOWLBottomObjectProperty())
				return;
			targetObjectPropertyAndVisitStatus.put(propImpl.getIRI().getIRIString(), Boolean.FALSE);
			COWLObjectPropertyImpl owlPropImpl = objectPropertyMap.get(propImpl);

			for (COWLPropertyImpl sub : owlPropImpl.getSubOWLProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(sub.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl sup : owlPropImpl.getSuperOWLProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(sup.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl eq : owlPropImpl.getEquivalentProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(eq.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl dis : owlPropImpl.getDisjointProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(dis.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl inv : owlPropImpl.getInverseProperties()) {
				OWLObjectProperty prop = factory.getOWLObjectProperty(inv.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
		} else if (node instanceof OWLDataPropertyImpl) {
			OWLDataPropertyImpl propImpl = (OWLDataPropertyImpl) node;
			if (propImpl.isOWLTopDataProperty() || propImpl.isOWLBottomDataProperty())
				return;
			targetDataPropertyAndVisitStatus.put(propImpl.getIRI().getIRIString(), Boolean.FALSE);
			COWLDataPropertyImpl owlPropImpl = dataPropertyMap.get(propImpl);
			for (COWLPropertyImpl sub : owlPropImpl.getSubOWLProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(sub.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl sup : owlPropImpl.getSuperOWLProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(sup.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl eq : owlPropImpl.getEquivalentProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(eq.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
			for (COWLPropertyImpl dis : owlPropImpl.getDisjointProperties()) {
				OWLDataProperty prop = factory.getOWLDataProperty(dis.getIRI());
				if (!visitedNodes.contains(prop))
					findTargetSignatures(prop);
			}
		} else if (node instanceof Entry<?, ?>) {
			Entry<?, ?> entry = (Entry<?, ?>) node;
			Object key = entry.getKey();
			if (!visitedNodes.contains(key))
				findTargetSignatures(key);
			Object value = entry.getValue();
			if (!visitedNodes.contains(value))
				findTargetSignatures(value);
		} else if (node instanceof OWLDataRange)
			return;
		else {
			logger.error("Node of type " + node.getClass().getName()
					+ " is ignored in the process of finding target signatures!");
		}
	}

	/**
	 * Generate QueryProperty objects that record properties of the queries.
	 * 
	 * @return An array list of QueryProperty objects.
	 */
	private ArrayList<QueryProperty> generateQueryProperties() {
		ArrayList<QueryProperty> queryProperties = new ArrayList<>(queryNumber);
		QueryProperty queryProperty;
		for (int i = 0; i < queryNumber; i++) {
			queryProperty = new QueryProperty(i);
			processQueryPattern(queries.get(i).getQueryPattern(), queryProperty);
			queryProperty.processTriplePatterns();
			queryProperties.add(queryProperty);
		}
		return queryProperties;
	}

	/**
	 * Generate a QueryProperty object that record properties of the specified
	 * query.
	 * 
	 * @param element
	 *            A pattern element of the specified query.
	 * @param queryProperty
	 *            QueryProperty object.
	 */
	private void processQueryPattern(Element element, QueryProperty queryProperty) {
		if (element == null || queryProperty == null)
			return;

		if (element instanceof ElementGroup) {
			List<Element> elements = ((ElementGroup) element).getElements();
			for (int i = 0; i < elements.size() - 1; i++)
				if ((elements.get(i).getClass() == ElementGroup.class
						|| elements.get(i).getClass() == ElementTriplesBlock.class)
						&& (elements.get(i + 1).getClass() == ElementGroup.class
								|| elements.get(i + 1).getClass() == ElementTriplesBlock.class))
					queryProperty.containKeywordAND = true;
			for (Element el : elements)
				processQueryPattern(el, queryProperty);
		} else if (element instanceof ElementTriplesBlock) {
			List<Triple> triples = ((ElementTriplesBlock) element).getPattern().getList();
			if (triples.size() > 1)
				queryProperty.containKeywordAND = true;
			queryProperty.triplePatterns.addAll(triples);
		} else if (element instanceof ElementUnion) {
			queryProperty.containKeywordUNION = true;
			List<Element> elements = ((ElementUnion) element).getElements();
			for (Element el : elements)
				processQueryPattern(el, queryProperty);
		} else if (element instanceof ElementOptional) {
			queryProperty.containKeywordOPTIONAL = true;
			Element el = ((ElementOptional) element).getOptionalElement();
			processQueryPattern(el, queryProperty);
		} else if (element instanceof ElementMinus) {
			queryProperty.containKeywordMINUS = true;
			Element el = ((ElementMinus) element).getMinusElement();
			processQueryPattern(el, queryProperty);
		}
		/*
		 * else if (element instanceof ElementNotExists) {
		 * queryProperty.containKeywordNOTEXISTS = true; Element el =
		 * ((ElementNotExists) element).getElement(); processQueryPattern(el,
		 * queryProperty); } else if (element instanceof ElementExists) {
		 * queryProperty.containKeywordEXISTS = true; Element el = ((ElementExists)
		 * element).getElement(); processQueryPattern(el, queryProperty); }
		 */
		else if (element instanceof ElementFilter) {
			Expr expr = ((ElementFilter) element).getExpr();
			if (expr instanceof E_Exists) {
				queryProperty.containKeywordEXISTS = true;
				Element el = ((E_Exists) expr).getElement();
				processQueryPattern(el, queryProperty);
			} else if (expr instanceof E_NotExists) {
				queryProperty.containKeywordNOTEXISTS = true;
				Element el = ((E_NotExists) expr).getElement();
				processQueryPattern(el, queryProperty);
			} else
				queryProperty.containKeywordFILTER = true;
		} else {
			logger.error("Element of type " + element.getClass().getName()
					+ " is ignored in the process of traversing query patterns!");
		}
	}

	/**
	 * Collect query evaluation metrics.
	 * 
	 * @param queryProperties
	 *            An array list of QueryProperty objects.
	 * @throws Exception
	 *             If there exists invalid input arguments.
	 */
	private void collectMetrics(ArrayList<QueryProperty> queryProperties) throws Exception {
		if (queryProperties == null)
			throw new NullPointerException("null arguments.");

		int keywordFILTERCount = 0, keywordANDCount = 0, keywordUNIONCount = 0, keywordOPTIONALCount = 0,
				keywordMINUSCount = 0, keywordNOTEXISTSCount = 0, keywordEXISTSCount = 0;

		int[] operatorSetCount = new int[16];

		final int Array_Size = 100;
		int[] triplePatternCount = new int[Array_Size];

		int[] joinTypeCount = new int[6];
		int[] joinAmountCount = new int[Array_Size];
		int total = 0;

		for (QueryProperty qp : queryProperties) {
			for (String node : qp.classNodes) {
				if (targetClassAndVisitStatus.containsKey(node))
					targetClassAndVisitStatus.replace(node, Boolean.TRUE);
				else
					nonTargetClasses.add(node);
			}
			for (String node : qp.propertyNodes) {
				if (targetDataPropertyAndVisitStatus.containsKey(node))
					targetDataPropertyAndVisitStatus.replace(node, Boolean.TRUE);
				else if (targetObjectPropertyAndVisitStatus.containsKey(node))
					targetObjectPropertyAndVisitStatus.replace(node, Boolean.TRUE);
				else
					nonTargetProperties.add(node);
			}

			if (qp.containKeywordFILTER)
				keywordFILTERCount++;
			if (qp.containKeywordAND)
				keywordANDCount++;
			if (qp.containKeywordUNION)
				keywordUNIONCount++;
			if (qp.containKeywordOPTIONAL)
				keywordOPTIONALCount++;
			if (qp.containKeywordMINUS)
				keywordMINUSCount++;
			if (qp.containKeywordNOTEXISTS)
				keywordNOTEXISTSCount++;
			if (qp.containKeywordEXISTS)
				keywordEXISTSCount++;

			if (!qp.containKeywordFILTER && !qp.containKeywordAND && !qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[0] += 1;
			else if (!qp.containKeywordFILTER && !qp.containKeywordAND && !qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[1] += 1;
			else if (!qp.containKeywordFILTER && !qp.containKeywordAND && qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[2] += 1;
			else if (!qp.containKeywordFILTER && !qp.containKeywordAND && qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[3] += 1;
			else if (!qp.containKeywordFILTER && qp.containKeywordAND && !qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[4] += 1;
			else if (!qp.containKeywordFILTER && qp.containKeywordAND && !qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[5] += 1;
			else if (!qp.containKeywordFILTER && qp.containKeywordAND && qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[6] += 1;
			else if (!qp.containKeywordFILTER && qp.containKeywordAND && qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[7] += 1;
			else if (qp.containKeywordFILTER && !qp.containKeywordAND && !qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[8] += 1;
			else if (qp.containKeywordFILTER && !qp.containKeywordAND && !qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[9] += 1;
			else if (qp.containKeywordFILTER && !qp.containKeywordAND && qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[10] += 1;
			else if (qp.containKeywordFILTER && !qp.containKeywordAND && qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[11] += 1;
			else if (qp.containKeywordFILTER && qp.containKeywordAND && !qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[12] += 1;
			else if (qp.containKeywordFILTER && qp.containKeywordAND && !qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[13] += 1;
			else if (qp.containKeywordFILTER && qp.containKeywordAND && qp.containKeywordUNION
					&& !qp.containKeywordOPTIONAL)
				operatorSetCount[14] += 1;
			else if (qp.containKeywordFILTER && qp.containKeywordAND && qp.containKeywordUNION
					&& qp.containKeywordOPTIONAL)
				operatorSetCount[15] += 1;

			if (qp.triplePatterns.size() < Array_Size)
				triplePatternCount[qp.triplePatterns.size()] += 1;
			else
				triplePatternCount[Array_Size - 1] += 1;

			total = 0;
			for (int i = 0; i < 6; i++) {
				joinTypeCount[i] += qp.tripleJointypeCount[i];
				total += qp.tripleJointypeCount[i];
			}
			if (total < Array_Size)
				joinAmountCount[total] += 1;
			else
				joinAmountCount[Array_Size - 1] += 1;
		}

		// Calculate space coverage metrics
		classCoverage = MathUtil.calculateSpaceCoverage(targetClassAndVisitStatus.values());
		dataPropertyCoverage = MathUtil.calculateSpaceCoverage(targetDataPropertyAndVisitStatus.values());
		objectPropertyCoverage = MathUtil.calculateSpaceCoverage(targetObjectPropertyAndVisitStatus.values());

		// Calculate keyword coverage metrics
		keywordFILTERCoverage = (double) keywordFILTERCount / queryNumber;
		keywordANDCoverage = (double) keywordANDCount / queryNumber;
		keywordUNIONCoverage = (double) keywordUNIONCount / queryNumber;
		keywordOPTIONALCoverage = (double) keywordOPTIONALCount / queryNumber;
		keywordMINUSCoverage = (double) keywordMINUSCount / queryNumber;
		keywordNOTEXISTSCoverage = (double) keywordNOTEXISTSCount / queryNumber;
		keywordEXISTSCoverage = (double) keywordEXISTSCount / queryNumber;

		// Calculate operator set distribution metrics
		for (int i = 0; i < operatorSetCount.length; i++)
			targetOperatorSetDistribution[i] = (double) operatorSetCount[i] / queryNumber;

		// Calculate triple pattern amount metric
		int subTotal1 = 0, subTotal2 = 0, subTotal3 = 0, subTotal4 = 0, subTotal5 = 0, subTotal6 = 0;
		for (int i = 1; i <= 3; i++)
			subTotal1 += triplePatternCount[i];
		for (int i = 4; i <= 6; i++)
			subTotal2 += triplePatternCount[i];
		for (int i = 7; i <= 9; i++)
			subTotal3 += triplePatternCount[i];
		for (int i = 10; i <= 12; i++)
			subTotal4 += triplePatternCount[i];
		for (int i = 13; i <= 15; i++)
			subTotal5 += triplePatternCount[i];
		for (int i = 16; i < Array_Size; i++)
			subTotal6 += triplePatternCount[i];
		total = subTotal1 + subTotal2 + subTotal3 + subTotal4 + subTotal5 + subTotal6;
		triplePatternAmountDistribution[0] = (double) subTotal1 / total;
		triplePatternAmountDistribution[1] = (double) subTotal2 / total;
		triplePatternAmountDistribution[2] = (double) subTotal3 / total;
		triplePatternAmountDistribution[3] = (double) subTotal4 / total;
		triplePatternAmountDistribution[4] = (double) subTotal5 / total;
		triplePatternAmountDistribution[5] = (double) subTotal6 / total;

		// Calculate join amount distribution metrics
		subTotal1 = 0;
		subTotal2 = 0;
		subTotal3 = 0;
		subTotal4 = 0;
		subTotal5 = 0;
		subTotal6 = 0;
		for (int i = 0; i <= 4; i++)
			subTotal1 += joinAmountCount[i];
		for (int i = 5; i <= 9; i++)
			subTotal2 += joinAmountCount[i];
		for (int i = 10; i <= 14; i++)
			subTotal3 += joinAmountCount[i];
		for (int i = 15; i <= 19; i++)
			subTotal4 += joinAmountCount[i];
		for (int i = 20; i <= 24; i++)
			subTotal5 += joinAmountCount[i];
		for (int i = 25; i < Array_Size; i++)
			subTotal6 += joinAmountCount[i];
		total = subTotal1 + subTotal2 + subTotal3 + subTotal4 + subTotal5 + subTotal6;
		joinAmountDistribution[0] = (double) subTotal1 / total;
		joinAmountDistribution[1] = (double) subTotal2 / total;
		joinAmountDistribution[2] = (double) subTotal3 / total;
		joinAmountDistribution[3] = (double) subTotal4 / total;
		joinAmountDistribution[4] = (double) subTotal5 / total;
		joinAmountDistribution[5] = (double) subTotal6 / total;

		// Calculate join type distribution metrics
		total = 0;
		for (int i = 0; i < 6; i++)
			total += joinTypeCount[i];
		for (int i = 0; i < 6; i++)
			tripleJointypeDistribution[i] = (double) joinTypeCount[i] / total;
	}

	/**
	 * Dump metrics information to file.
	 */
	private void printToFile() {
		StringBuffer outputs = new StringBuffer();
		// Output format
		String format = "%-5b %-100s\n";
		outputs.append(targetClassAndVisitStatus.keySet().size() + " out of " + classMap.keySet().size()
				+ " OWL classes are selected:\n");
		for (Entry<String, Boolean> classEntry : targetClassAndVisitStatus.entrySet()) {
			outputs.append(String.format(format, classEntry.getValue().booleanValue(), classEntry.getKey().toString()));
		}
		if (!nonTargetClasses.isEmpty()) {
			outputs.append(
					"\n\n" + nonTargetClasses.size() + " OWL classes are not selected but are used in the queries:\n");
			for (String cls : nonTargetClasses)
				outputs.append(cls.toString() + "\n");
		}
		outputs.append("\n\n" + targetObjectPropertyAndVisitStatus.keySet().size() + " out of "
				+ objectPropertyMap.keySet().size() + " OWL object properties are selected:\n");
		for (Entry<String, Boolean> propertyEntry : targetObjectPropertyAndVisitStatus.entrySet()) {
			outputs.append(
					String.format(format, propertyEntry.getValue().booleanValue(), propertyEntry.getKey().toString()));
		}
		outputs.append("\n\n" + targetDataPropertyAndVisitStatus.keySet().size() + " out of "
				+ dataPropertyMap.keySet().size() + " OWL data properties are selected:\n");
		for (Entry<String, Boolean> propertyEntry : targetDataPropertyAndVisitStatus.entrySet()) {
			outputs.append(
					String.format(format, propertyEntry.getValue().booleanValue(), propertyEntry.getKey().toString()));
		}
		if (!nonTargetProperties.isEmpty()) {
			outputs.append("\n\n" + nonTargetProperties.size()
					+ " OWL properties are not selected but are used in the datasets:\n");
			for (String prop : nonTargetProperties)
				outputs.append(prop.toString() + "\n");
		}

		DecimalFormat df = new DecimalFormat("#,##0.00%");
		outputs.append("\n\nSignature coverage of the queries is summarized below:\n");
		outputs.append("Class coverage (CC) = " + df.format(classCoverage) + "\n");
		outputs.append("Data property coverage (DPC) = " + df.format(dataPropertyCoverage) + "\n");
		outputs.append("Object property coverage (OPC) = " + df.format(objectPropertyCoverage) + "\n");

		outputs.append("\n\nKeyword coverage of the queries is summarized below:\n");
		outputs.append("FILTER coverage = " + df.format(keywordFILTERCoverage) + "\n");
		outputs.append("AND coverage = " + df.format(keywordANDCoverage) + "\n");
		outputs.append("UNION coverage = " + df.format(keywordUNIONCoverage) + "\n");
		outputs.append("OPTIONAL coverage = " + df.format(keywordOPTIONALCoverage) + "\n");
		outputs.append("MINUS coverage = " + df.format(keywordMINUSCoverage) + "\n");
		outputs.append("NOT EXISTS coverage = " + df.format(keywordNOTEXISTSCoverage) + "\n");
		outputs.append("EXISTS coverage = " + df.format(keywordEXISTSCoverage) + "\n");

		outputs.append("\n\nOperator set distribution is summarized below:\n");
		outputs.append("{} = " + df.format(targetOperatorSetDistribution[0]) + "\n");
		outputs.append("{And} = " + df.format(targetOperatorSetDistribution[4]) + "\n");
		outputs.append("{Filter} = " + df.format(targetOperatorSetDistribution[8]) + "\n");
		outputs.append("{Filter, And} = " + df.format(targetOperatorSetDistribution[12]) + "\n");
		double cpfSubtotal = targetOperatorSetDistribution[0] + targetOperatorSetDistribution[4]
				+ targetOperatorSetDistribution[8] + targetOperatorSetDistribution[12];
		outputs.append("CPF subtotal = " + df.format(cpfSubtotal) + "\n");
		double subTotal = 0;
		outputs.append("{Union} = " + df.format(targetOperatorSetDistribution[2]) + "\n");
		subTotal += targetOperatorSetDistribution[2];
		outputs.append("{Opt} = " + df.format(targetOperatorSetDistribution[1]) + "\n");
		subTotal += targetOperatorSetDistribution[1];
		outputs.append("{Filter, Union} = " + df.format(targetOperatorSetDistribution[10]) + "\n");
		subTotal += targetOperatorSetDistribution[10];
		outputs.append("{Filter, Opt} = " + df.format(targetOperatorSetDistribution[9]) + "\n");
		subTotal += targetOperatorSetDistribution[9];
		outputs.append("{And, Union} = " + df.format(targetOperatorSetDistribution[6]) + "\n");
		subTotal += targetOperatorSetDistribution[6];
		outputs.append("{And, Opt} = " + df.format(targetOperatorSetDistribution[5]) + "\n");
		subTotal += targetOperatorSetDistribution[5];
		outputs.append("{Union, Opt} = " + df.format(targetOperatorSetDistribution[3]) + "\n");
		subTotal += targetOperatorSetDistribution[3];
		outputs.append("{Filter, And, Union} = " + df.format(targetOperatorSetDistribution[14]) + "\n");
		subTotal += targetOperatorSetDistribution[14];
		outputs.append("{Filter, Union, Opt} = " + df.format(targetOperatorSetDistribution[11]) + "\n");
		subTotal += targetOperatorSetDistribution[11];
		outputs.append("{Filter, And, Opt} = " + df.format(targetOperatorSetDistribution[13]) + "\n");
		subTotal += targetOperatorSetDistribution[13];
		outputs.append("{And, Union, Opt} = " + df.format(targetOperatorSetDistribution[7]) + "\n");
		subTotal += targetOperatorSetDistribution[7];
		outputs.append("{Filter, And, Union, Opt} = " + df.format(targetOperatorSetDistribution[15]) + "\n");
		subTotal += targetOperatorSetDistribution[15];
		outputs.append("Non-CPF subtotal = " + df.format(subTotal) + "\n");

		outputs.append("\n\nTriple pattern amount distribution is summarized below:\n");
		outputs.append("{1~3} = " + df.format(triplePatternAmountDistribution[0]) + "\n");
		outputs.append("{4~6} = " + df.format(triplePatternAmountDistribution[1]) + "\n");
		outputs.append("{7~9} = " + df.format(triplePatternAmountDistribution[2]) + "\n");
		outputs.append("{10~12} = " + df.format(triplePatternAmountDistribution[3]) + "\n");
		outputs.append("{13~16} = " + df.format(triplePatternAmountDistribution[4]) + "\n");
		outputs.append("{16+} = " + df.format(triplePatternAmountDistribution[5]) + "\n");

		outputs.append("\n\nJoin amount distribution is summarized below:\n");
		outputs.append("{0~4} = " + df.format(joinAmountDistribution[0]) + "\n");
		outputs.append("{5~9} = " + df.format(joinAmountDistribution[1]) + "\n");
		outputs.append("{10~14} = " + df.format(joinAmountDistribution[2]) + "\n");
		outputs.append("{15~19} = " + df.format(joinAmountDistribution[3]) + "\n");
		outputs.append("{20~24} = " + df.format(joinAmountDistribution[4]) + "\n");
		outputs.append("{25+} = " + df.format(joinAmountDistribution[5]) + "\n");

		outputs.append("\n\nJoin type distribution is summarized below:\n");
		outputs.append("Subject-Subject (SS) = " + df.format(tripleJointypeDistribution[0]) + "\n");
		outputs.append("Predicate-Predicate (PP) = " + df.format(tripleJointypeDistribution[1]) + "\n");
		outputs.append("Object-Object (OO) = " + df.format(tripleJointypeDistribution[2]) + "\n");
		outputs.append("Subject-Predicate (SP) = " + df.format(tripleJointypeDistribution[3]) + "\n");
		outputs.append("Subject-Object (SO) = " + df.format(tripleJointypeDistribution[4]) + "\n");
		outputs.append("Predicate-Object (PO) = " + df.format(tripleJointypeDistribution[5]) + "\n");

		String evaluationFilePath = "evaluationresults" + File.separator + "QueryEvaluationResults_" + queryNumber
				+ "Queries" + ".txt";
		File outputFile = new File(evaluationFilePath);
		try {
			FileUtils.writeStringToFile(outputFile, outputs.toString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("There was an error while dumping into evaluation results file.", e);
		}
		logger.info("Evaluation results are dumped into local file: " + outputFile.getAbsolutePath());
		logger.info("Done!");
	}
}
