package edu.neu.ece.sparqlquerygenerator.generator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLQuantifiedDataRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.COWLClassImpl;
import edu.neu.ece.sparqlquerygenerator.entity.COWLDataPropertyImpl;
import edu.neu.ece.sparqlquerygenerator.entity.COWLObjectPropertyImpl;
import edu.neu.ece.sparqlquerygenerator.main.OntologyExtractor;
import edu.neu.ece.sparqlquerygenerator.utility.CollectionUtil;
import edu.neu.ece.sparqlquerygenerator.utility.MathUtil;
import uk.ac.manchester.cs.owl.owlapi.OWLDataComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataHasValueImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeRestrictionImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNaryBooleanClassExpressionImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNaryDataRangeImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasSelfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasValueImpl;

/**
 * This class defines SPQRQL query generator, which includes core algorithms for
 * SPARQL query generation.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-07-29
 */
public class SPARQLQueryGenerator {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Ontology root class IRI as string.
	 */
	private final String rootIRIString;

	/**
	 * Selected root class in the input ontology as the entry for dataset
	 * generation.
	 */
	private COWLClassImpl rootClass = null;

	/**
	 * The number of queries; 1 by default.
	 */
	private final int queryNumber;

	/**
	 * Used to generate a stream of pesudorandom numbers.
	 */
	private final Random ran;

	/**
	 * Support distinct queries.
	 */
	private final boolean distinct;

	/**
	 * Directory of the output queries; SPARQLqueries by default.
	 */
	private final File outputDirectory;

	/**
	 * The probability of selecting an OWL class constraint (anonymous super class
	 * expression, anonymous equivalent class expression and anonymous disjoint
	 * class expression) of an OWL class; 0.9 by default.
	 */
	private final double classConstraintSelectionProbability;

	/**
	 * The probability of generating class assertion axioms for each named
	 * individual; 1.0 by default.
	 */
	private final double classAssertionProbability;

	/**
	 * The probability of creating an object property assertion triple pattern; 0.5
	 * by default.
	 */
	private final double objectPropertyAssertionProbability;

	/**
	 * The probability of creating a data property assertion triple pattern; 0.5 by
	 * default.
	 */
	private final double dataPropertyAssertionProbability;

	/**
	 * The probability of selecting an inverse property (direct or inferred) of an
	 * object property for object property assertion triple pattern generation; 0.8
	 * by default.
	 */
	private final double inverseObjectPropertySelectionProbability;

	/**
	 * The probability of creating an OWL named individual; 0.5 by default.
	 */
	private final double newVariableProbability;

	/**
	 * The probability of linking a variable to an individual; 0.2 by default.
	 */
	private final double linkToIndividualProbability;

	/**
	 * The probability of creating a filter that restricts the solutions of a graph
	 * pattern; 0.5 by default.
	 */
	private final double filterProbability;

	/**
	 * The probability of creating a conjunction graph pattern; 0.8 by default.
	 */
	private final double conjunctionGraphPatternProbability;

	/**
	 * The probability of creating an optional graph pattern; 0 by default.
	 */
	private final double optionalGraphPatternProbability;

	/**
	 * The probability of creating an union graph pattern; 0.15 by default.
	 */
	private final double unionGraphPatternProbability;

	/**
	 * An OWL data factory object used to create entities, class expressions and
	 * axioms.
	 */
	private final OWLDataFactory factory;

	/**
	 * Container that stores key-value pairs, where OWL API interface OWLClass is
	 * the key and the customized class COWLClassImpl is the value.
	 */
	private Map<OWLClass, COWLClassImpl> classMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 */
	private Map<OWLDataProperty, COWLDataPropertyImpl> dataPropertyMap;

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 */
	private Map<OWLObjectProperty, COWLObjectPropertyImpl> objectPropertyMap;

	/**
	 * The map that maps prefix names to prefixes.
	 */
	private final Map<String, String> prefixName2PrefixMap;

	/**
	 * Constructor
	 * 
	 * @param rootIRIString
	 *            Ontology root class IRI as string.
	 * @param queryNumber
	 *            The number of queries.
	 * @param seed
	 *            Random seed for query generation.
	 * @param distinct
	 *            Generate distinct queries.
	 * @param outputDirectory
	 *            Directory of the output queries.
	 * @param classConstraintSelectionProbability
	 *            The probability of selecting an OWL class constraint (anonymous
	 *            super class expressions and anonymous equivalent class
	 *            expressions) of an OWL class.
	 * @param classAssertionProbability
	 *            The probability of generating class assertion triple pattern.
	 * @param objectPropertyAssertionProbability
	 *            The probability of creating an object property assertion triple
	 *            pattern.
	 * @param dataPropertyAssertionProbability
	 *            The probability of creating a data property assertion triple
	 *            pattern.
	 * @param inverseObjectPropertySelectionProbability
	 *            The probability of selecting an inverse property (direct or
	 *            inferred) of an object property for object property assertion
	 *            triple pattern generation.
	 * @param newVariableProbability
	 *            The probability of creating a new variable.
	 * @param linkToIndividualProbability
	 *            The probability of linking a variable to an individual.
	 * @param filterProbability
	 *            The probability of creating a filter that restricts the solutions
	 *            of a graph pattern.
	 * @param conjunctionGraphPatternProbability
	 *            The probability of creating a conjunction graph pattern.
	 * @param optionalGraphPatternProbability
	 *            The probability of creating an optional graph pattern.
	 * @param unionGraphPatternProbability
	 *            The probability of creating an union graph pattern.
	 * @param factory
	 *            An OWL data factory object used to create entities, class
	 *            expressions and axioms.
	 * @param extractor
	 *            Ontology extractor used for extracting OWL axioms from input
	 *            ontology.
	 * @param prefixName2PrefixMap
	 *            A map that maps prefix names to prefixes.
	 */
	public SPARQLQueryGenerator(String rootIRIString, int queryNumber, long seed, boolean distinct,
			File outputDirectory, double classConstraintSelectionProbability, double classAssertionProbability,
			double objectPropertyAssertionProbability, double dataPropertyAssertionProbability,
			double inverseObjectPropertySelectionProbability, double newVariableProbability,
			double linkToIndividualProbability, double filterProbability, double conjunctionGraphPatternProbability,
			double optionalGraphPatternProbability, double unionGraphPatternProbability, OWLDataFactory factory,
			OntologyExtractor extractor, Map<String, String> prefixName2PrefixMap) {
		this.rootIRIString = rootIRIString;
		this.queryNumber = queryNumber;
		ran = new Random(seed);
		this.distinct = distinct;
		this.outputDirectory = outputDirectory;
		this.classConstraintSelectionProbability = classConstraintSelectionProbability;
		this.classAssertionProbability = classAssertionProbability;
		this.objectPropertyAssertionProbability = objectPropertyAssertionProbability;
		this.dataPropertyAssertionProbability = dataPropertyAssertionProbability;
		this.inverseObjectPropertySelectionProbability = inverseObjectPropertySelectionProbability;
		this.newVariableProbability = newVariableProbability;
		this.linkToIndividualProbability = linkToIndividualProbability;
		this.filterProbability = filterProbability;
		this.conjunctionGraphPatternProbability = conjunctionGraphPatternProbability;
		this.optionalGraphPatternProbability = optionalGraphPatternProbability;
		this.unionGraphPatternProbability = unionGraphPatternProbability;
		this.factory = factory;

		classMap = extractor.getClassMap();
		dataPropertyMap = extractor.getDataPropertyMap();
		objectPropertyMap = extractor.getObjectPropertyMap();

		this.prefixName2PrefixMap = prefixName2PrefixMap;
	}

	/**
	 * Get root class in the input ontology.
	 * 
	 * @return Root class.
	 */
	public COWLClassImpl getRootClass() {
		return rootClass;
	}

	/**
	 * Get the number of queries.
	 * 
	 * @return The number of queries.
	 */
	public int getQueryNumber() {
		return queryNumber;
	}

	/**
	 * Get ontology data factory used for creating entities, class expressions and
	 * axioms.
	 * 
	 * @return Ontology data factory.
	 */
	public OWLDataFactory getFactory() {
		return factory;
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLClass is the key and the customized class COWLClassImpl is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLClass, COWLClassImpl> getClassMap() {
		return classMap;
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLDataProperty, COWLDataPropertyImpl> getDataPropertyMap() {
		return dataPropertyMap;
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLObjectProperty, COWLObjectPropertyImpl> getObjectPropertyMap() {
		return objectPropertyMap;
	}

	/**
	 * This function defines control flow of the SPARQL query generation process
	 * based on the constructed Java model.
	 * 
	 * @throws Exception
	 *             If query generation fails.
	 * @return A list of queries.
	 */
	public ArrayList<Query> generateRandomSPARQLQueries() throws Exception {
		logger.info("Begin generating SPARQL queries...");
		if (distinct)
			return generateDistinctSPARQLQueries();
		ArrayList<Query> queries = new ArrayList<>(queryNumber);
		for (OWLClass oc : classMap.keySet())
			if (oc.getIRI().getIRIString().equals(rootIRIString)) {
				logger.info("Find out root class with IRI: " + rootIRIString);
				rootClass = classMap.get(oc);
				Query query;
				ElementGroup elg;
				Map<String, String> queryNsPrefixes;
				COWLClassImpl selectedClass;
				Var var;
				PrefixMapping pmap;
				// Op op;
				for (int i = 0; i < queryNumber; i++) {
					logger.info("Begin generating SPARQL query with query ID {}...", i);
					query = QueryFactory.make();
					query.setQuerySelectType();
					queryNsPrefixes = new HashMap<>();
					queryNsPrefixes.put("ObjectDescription", prefixName2PrefixMap.get(":"));

					selectedClass = CollectionUtil.getARandomElementFromSet(rootClass.getSubClassesAndItself(), ran);
					var = VariableGenerator.generateVariable(selectedClass);
					elg = generateRecursiveGraphPatternsFromNamedClass(var, factory.getOWLClass(selectedClass.getIRI()),
							queryNsPrefixes, true);
					query.setQueryPattern(elg);
					query.setDistinct(true);
					query.addResultVar(var);

					// op = Algebra.compile(query);
					// op = Algebra.optimize(op);
					// query = OpAsQuery.asQuery(op);

					pmap = new PrefixMappingImpl();
					pmap.setNsPrefixes(queryNsPrefixes);
					query.setPrefixMapping(pmap);
					queries.add(query);
					// logger.info("Generated query script is shown below:");
					// query.serialize(new IndentedWriter(System.out, false),
					// Syntax.syntaxSPARQL_11);
					// System.out.print("\n\n");
					dumpIntoFile(query, outputDirectory, i);
					resetStatus(classMap.values());
				}
				break;
			}
		return queries;
	}

	/**
	 * This function defines control flow of distinct SPARQL query generation
	 * process based on the constructed Java model.
	 * 
	 * @throws Exception
	 *             If query generation fails.
	 * @return A list of distinct queries.
	 */
	private ArrayList<Query> generateDistinctSPARQLQueries() throws Exception {
		HashSet<Query> distinctQueries = new HashSet<>(queryNumber);
		for (OWLClass oc : classMap.keySet())
			if (oc.getIRI().getIRIString().equals(rootIRIString)) {
				logger.info("Find out root class with IRI: " + rootIRIString);
				rootClass = classMap.get(oc);
				Query query;
				ElementGroup elg;
				Map<String, String> queryNsPrefixes;
				COWLClassImpl selectedClass;
				Var var;
				PrefixMapping pmap;
				// Op op;
				int count = 0, attempt = 0;
				boolean flag = true;
				while (count < queryNumber) {
					if (flag) {
						logger.info("Begin generating SPARQL query with query ID {}...", count);
						flag = false;
					}
					query = QueryFactory.make();
					query.setQuerySelectType();
					queryNsPrefixes = new HashMap<>();
					queryNsPrefixes.put("ObjectDescription", prefixName2PrefixMap.get(":"));

					selectedClass = CollectionUtil.getARandomElementFromSet(rootClass.getSubClassesAndItself(), ran);
					var = VariableGenerator.generateVariable(selectedClass);
					elg = generateRecursiveGraphPatternsFromNamedClass(var, factory.getOWLClass(selectedClass.getIRI()),
							queryNsPrefixes, true);
					query.setQueryPattern(elg);
					query.setDistinct(true);
					query.addResultVar(var);

					// op = Algebra.compile(query);
					// op = Algebra.optimize(op);
					// query = OpAsQuery.asQuery(op);

					pmap = new PrefixMappingImpl();
					pmap.setNsPrefixes(queryNsPrefixes);
					query.setPrefixMapping(pmap);
					if (!distinctQueries.contains(query)) {
						distinctQueries.add(query);
						dumpIntoFile(query, outputDirectory, count);
						count++;
						flag = true;
						// logger.info("Generated query script is shown below:");
						// query.serialize(new IndentedWriter(System.out, false),
						// Syntax.syntaxSPARQL_11);
						// System.out.print("\n\n");
					}
					resetStatus(classMap.values());
					attempt++;
				}
				logger.info("{} SPARQL queries have been generated with {} attempts.", count, attempt);
				break;
			}
		ArrayList<Query> queries = new ArrayList<>(distinctQueries);
		return queries;
	}

	/**
	 * This function recursively generates graph patterns by navigating through the
	 * model from the specified OWL class.
	 * 
	 * @param var1
	 *            Variable.
	 * @param oc
	 *            OWL class.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @param isFirstRecursion
	 *            Detect whether this function is invoked for the first time. True
	 *            if it is, false otherwise.
	 * @return Graph pattern.
	 * @throws Exception
	 *             If input arguments are invalid or there exists sub-functions
	 *             throwing exceptions.
	 */
	public ElementGroup generateRecursiveGraphPatternsFromNamedClass(Var var1, OWLClass oc,
			Map<String, String> queryNsPrefixes, boolean isFirstRecursion) throws Exception {
		if (var1 == null || oc == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		ElementGroup elg1 = new ElementGroup();
		if (oc.isOWLThing() || oc.isOWLNothing())
			return elg1;
		COWLClassImpl ocImpl1 = classMap.get(oc);
		logger.info("Selected OWL class IRI is: " + ocImpl1.getIRI().getIRIString());
		ocImpl1.setVisited(true);
		ocImpl1.getVariables().add(var1);
		Triple triple;
		if (ran.nextDouble() < classAssertionProbability || isFirstRecursion == true) {
			triple = generateClassAssertionTriplePattern(var1, oc, queryNsPrefixes);
			if (triple != null)
				elg1.addTriplePattern(triple);
		}

		LinkedList<ElementGroup> elgList = new LinkedList<>();
		ElementGroup elg2 = new ElementGroup();
		if (ran.nextDouble() < classConstraintSelectionProbability) {
			OWLAnonymousClassExpression clsExp = CollectionUtil
					.getARandomElementFromSet(ocImpl1.getAnonymousClassRestrictions(), ran);
			if (clsExp != null) {
				logger.info("Selected OWL class constraint is: " + clsExp.toString());
				elg2 = generateRecursiveGraphPatternsFromAnonymousClass(var1, clsExp, queryNsPrefixes);
				if (!elg2.isEmpty())
					elgList.offer(elg2);
			}
		}
		Var var2;
		if (ran.nextDouble() < dataPropertyAssertionProbability) {
			Entry<OWLDataProperty, OWLDataRange> entry = CollectionUtil
					.getARandomElementFromSet(ocImpl1.getDataPropertyRangesPairs().entrySet(), ran);
			if (entry != null) {
				OWLDataProperty dataProperty = entry.getKey();
				OWLDataRange range = entry.getValue();
				logger.info("Selected data property <key-value> pair is key = " + dataProperty.getIRI().getShortForm()
						+ ", value = " + range.toString());
				var2 = VariableGenerator.generateBindToDataValueVariable();
				triple = generateDataPropertyAssertionTriplePattern(var1, dataProperty, var2, queryNsPrefixes);
				elg1.addTriplePattern(triple);
				if (ran.nextDouble() < filterProbability) {
					Expr expr = generateRecursiveFilterExpressionFromDataRange(var2, range, queryNsPrefixes);
					if (expr != null)
						elg1.addElementFilter(new ElementFilter(expr));
				}
			}
		}
		ElementGroup elg3 = new ElementGroup();
		if (ran.nextDouble() < objectPropertyAssertionProbability) {
			Entry<OWLObjectProperty, OWLClassExpression> entry = CollectionUtil
					.getARandomElementFromSet(ocImpl1.getObjectPropertyRangesPairs().entrySet(), ran);
			if (entry != null) {
				OWLObjectProperty objectProperty = entry.getKey();
				OWLClassExpression classExp = entry.getValue();
				logger.info("Selected object property <key-value> pair is key = "
						+ objectProperty.getIRI().getShortForm() + ", value = " + classExp.toString());
				triple = null;
				if (!classExp.isAnonymous()) {
					OWLClass oc2 = classExp.asOWLClass();
					if (!oc2.isOWLThing() && !oc2.isOWLNothing()) {
						COWLClassImpl ocImpl2 = classMap.get(oc2);
						LinkedList<OWLNamedIndividual> individuals = ocImpl2.getNamedIndividuals();
						if (ran.nextDouble() < linkToIndividualProbability && individuals.isEmpty() == false) {
							OWLNamedIndividual ind = CollectionUtil.getARandomElementFromList(individuals, ran);
							triple = generateObjectPropertyAssertionTriplePattern(var1, objectProperty, ind,
									queryNsPrefixes);
						} else {
							ocImpl2 = CollectionUtil.getARandomElementFromSet(ocImpl2.getRelevantNamedClasses(classMap),
									ran);
							if (!ocImpl2.isVisited()) {
								var2 = VariableGenerator.generateVariable(ocImpl2);
								elg3 = generateRecursiveGraphPatternsFromNamedClass(var2,
										factory.getOWLClass(ocImpl2.getIRI()), queryNsPrefixes, false);
							} else if (ran.nextDouble() < newVariableProbability) {
								var2 = VariableGenerator.generateVariable(ocImpl2);
								ocImpl2.getVariables().add(var2);
							} else
								var2 = CollectionUtil.getARandomElementFromList(ocImpl2.getVariables(), ran);
							triple = generateObjectPropertyAssertionTriplePattern(var1, objectProperty, var2,
									queryNsPrefixes);
						}
					}
				} else {
					var2 = VariableGenerator.generateVariable();
					elg3 = generateRecursiveGraphPatternsFromAnonymousClass(var2, classExp, queryNsPrefixes);
					triple = generateObjectPropertyAssertionTriplePattern(var1, objectProperty, var2, queryNsPrefixes);
				}
				if (triple != null)
					elg1.addTriplePattern(triple);
				if (!elg3.isEmpty())
					elgList.offer(elg3);
			}
		}
		if (!elg1.isEmpty())
			elgList.offerFirst(elg1);
		return joinGraphPatterns(elgList, !isFirstRecursion);
	}

	/**
	 * This function recursively generates graph patterns by navigating through the
	 * model from the specified anonymous class expression.
	 * 
	 * @param var
	 *            Variable
	 * @param clsExp
	 *            Anonymous class expression.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @return Graph pattern.
	 * @throws Exception
	 *             If input arguments are invalid or there exists sub-functions
	 *             throwing such exceptions.
	 */
	private ElementGroup generateRecursiveGraphPatternsFromAnonymousClass(Var var, OWLClassExpression clsExp,
			Map<String, String> queryNsPrefixes) throws Exception {
		if (var == null || clsExp == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		Triple triple;
		ElementGroup elg = new ElementGroup();
		if (clsExp.isOWLThing() || clsExp.isOWLNothing())
			return elg;
		if (clsExp instanceof OWLNaryBooleanClassExpressionImpl) {
			Set<OWLClassExpression> operands = ((OWLNaryBooleanClassExpressionImpl) clsExp).operands()
					.collect(Collectors.toSet());
			Set<OWLClassExpression> selectedOperands = CollectionUtil
					.getARandomElementFromList(CollectionUtil.getAllSubSetsOfASet(operands), ran);
			ElementGroup subElg;
			LinkedList<ElementGroup> elgList = new LinkedList<>();
			for (OWLClassExpression exp : selectedOperands) {
				if (exp.isOWLThing() || exp.isOWLNothing())
					continue;
				subElg = new ElementGroup();
				if (!exp.isAnonymous()) {
					COWLClassImpl ocImpl = classMap.get(exp.asOWLClass());
					ocImpl = CollectionUtil.getARandomElementFromSet(ocImpl.getRelevantNamedClasses(classMap), ran);
					if (!ocImpl.isVisited())
						subElg = generateRecursiveGraphPatternsFromNamedClass(var, factory.getOWLClass(ocImpl.getIRI()),
								queryNsPrefixes, false);
				} else
					subElg = generateRecursiveGraphPatternsFromAnonymousClass(var, exp, queryNsPrefixes);
				if (!subElg.isEmpty())
					elgList.offer(subElg);
			}
			return joinGraphPatterns(elgList, true);
		} else if (clsExp instanceof OWLObjectComplementOfImpl) {
			OWLClassExpression exp = ((OWLObjectComplementOfImpl) clsExp).getOperand();
			if (exp.isOWLThing() || exp.isOWLNothing())
				return elg;
			if (!exp.isAnonymous()) {
				COWLClassImpl ocImpl = classMap.get(exp.asOWLClass());
				ocImpl = CollectionUtil.getARandomElementFromSet(ocImpl.getRelevantNamedClasses(classMap), ran);
				if (!ocImpl.isVisited())
					elg = generateRecursiveGraphPatternsFromNamedClass(var, factory.getOWLClass(ocImpl.getIRI()),
							queryNsPrefixes, false);
			} else
				elg = generateRecursiveGraphPatternsFromAnonymousClass(var, exp, queryNsPrefixes);
		} else if (clsExp instanceof OWLObjectHasValueImpl) {
			if (ran.nextDouble() < objectPropertyAssertionProbability) {
				OWLObjectProperty objectProperty = processObjectPropertyExpression(
						((OWLObjectHasValueImpl) clsExp).getProperty());
				if (ran.nextDouble() < linkToIndividualProbability) {
					OWLIndividual individual = ((OWLObjectHasValueImpl) clsExp).getFiller();
					if (!individual.isAnonymous()) {
						triple = generateObjectPropertyAssertionTriplePattern(var, objectProperty,
								individual.asOWLNamedIndividual(), queryNsPrefixes);
						elg.addTriplePattern(triple);
					} else
						logger.warn("Anonymous individual from OWLObjectHasValue " + clsExp + "will be ignored");
				} else {
					Var var2 = VariableGenerator.generateVariable();
					triple = generateObjectPropertyAssertionTriplePattern(var, objectProperty, var2, queryNsPrefixes);
					elg.addTriplePattern(triple);
				}
			}
		} else if (clsExp instanceof OWLObjectHasSelfImpl) {
			if (ran.nextDouble() < objectPropertyAssertionProbability) {
				OWLObjectProperty objectProperty = processObjectPropertyExpression(
						((OWLObjectHasValueImpl) clsExp).getProperty());
				triple = generateObjectPropertyAssertionTriplePattern(var, objectProperty, var, queryNsPrefixes);
				elg.addTriplePattern(triple);
			}
		} else if (clsExp instanceof OWLQuantifiedObjectRestriction) {
			if (ran.nextDouble() < objectPropertyAssertionProbability) {
				OWLObjectProperty objectProperty = processObjectPropertyExpression(
						((OWLQuantifiedObjectRestriction) clsExp).getProperty());
				OWLClassExpression classExp = ((OWLQuantifiedObjectRestriction) clsExp).getFiller();
				if (classExp.isOWLThing() || classExp.isOWLNothing())
					return elg;
				ElementGroup subElg = new ElementGroup();
				Var var2;
				if (!classExp.isAnonymous()) {
					OWLClass oc2 = classExp.asOWLClass();
					LinkedList<OWLNamedIndividual> individuals = classMap.get(oc2).getNamedIndividuals();
					if (ran.nextDouble() < linkToIndividualProbability && individuals.isEmpty() == false) {
						OWLNamedIndividual ind = CollectionUtil.getARandomElementFromList(individuals, ran);
						triple = generateObjectPropertyAssertionTriplePattern(var, objectProperty, ind,
								queryNsPrefixes);
					} else {
						COWLClassImpl ocImpl2 = classMap.get(oc2);
						ocImpl2 = CollectionUtil.getARandomElementFromSet(ocImpl2.getRelevantNamedClasses(classMap),
								ran);
						if (!ocImpl2.isVisited()) {
							var2 = VariableGenerator.generateVariable(ocImpl2);
							subElg = generateRecursiveGraphPatternsFromNamedClass(var2,
									factory.getOWLClass(ocImpl2.getIRI()), queryNsPrefixes, false);
						} else if (ran.nextDouble() < newVariableProbability) {
							var2 = VariableGenerator.generateVariable(ocImpl2);
							ocImpl2.getVariables().add(var2);
						} else
							var2 = CollectionUtil.getARandomElementFromList(ocImpl2.getVariables(), ran);
						triple = generateObjectPropertyAssertionTriplePattern(var, objectProperty, var2,
								queryNsPrefixes);
					}
				} else {
					var2 = VariableGenerator.generateVariable();
					subElg = generateRecursiveGraphPatternsFromAnonymousClass(var2, classExp, queryNsPrefixes);
					triple = generateObjectPropertyAssertionTriplePattern(var, objectProperty, var2, queryNsPrefixes);
				}
				elg.addTriplePattern(triple);
				if (!subElg.isEmpty())
					elg.addElement(subElg);
			}
		} else if (clsExp instanceof OWLDataHasValueImpl) {
			if (ran.nextDouble() < dataPropertyAssertionProbability) {
				OWLDataProperty dataProperty = ((OWLDataHasValueImpl) clsExp).getProperty().asOWLDataProperty();
				Var var2 = VariableGenerator.generateBindToDataValueVariable();
				triple = generateDataPropertyAssertionTriplePattern(var, dataProperty, var2, queryNsPrefixes);
				elg.addTriplePattern(triple);
			}
		} else if (clsExp instanceof OWLQuantifiedDataRestriction) {
			if (ran.nextDouble() < dataPropertyAssertionProbability) {
				OWLDataProperty dataProperty = ((OWLQuantifiedDataRestriction) clsExp).getProperty()
						.asOWLDataProperty();
				OWLDataRange range = ((OWLQuantifiedDataRestriction) clsExp).getFiller();
				Var var2 = VariableGenerator.generateBindToDataValueVariable();
				triple = generateDataPropertyAssertionTriplePattern(var, dataProperty, var2, queryNsPrefixes);
				elg.addTriplePattern(triple);
				if (ran.nextDouble() < filterProbability) {
					Expr expr = generateRecursiveFilterExpressionFromDataRange(var2, range, queryNsPrefixes);
					if (expr != null)
						elg.addElementFilter(new ElementFilter(expr));
				}
			}
		} else {
			logger.warn("OWL class expression {} is ignored during query generation: " + clsExp);
		}
		return elg;
	}

	/**
	 * This function generates a filter that restricts the specified variable based
	 * on the given OWL data range.
	 * 
	 * @param var
	 *            Variable.
	 * @param range
	 *            OWL data range.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @return Filter expression.
	 * @throws Exception
	 *             If input arguments are invalid or there exists sub-functions
	 *             throwing such exceptions.
	 */
	private Expr generateRecursiveFilterExpressionFromDataRange(Var var, OWLDataRange range,
			Map<String, String> queryNsPrefixes) throws Exception {
		if (var == null || range == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		if (range instanceof OWLDataComplementOfImpl) {
			return generateRecursiveFilterExpressionFromDataRange(var, ((OWLDataComplementOfImpl) range).getDataRange(),
					queryNsPrefixes);
		} else if (range instanceof OWLDatatype) {
			return generateFilterExpressionFromDatatype(var, (OWLDatatype) range, queryNsPrefixes);
		} else if (range instanceof OWLDatatypeRestrictionImpl) {
			OWLDatatype dt = ((OWLDatatypeRestriction) range).getDatatype();
			Set<OWLFacetRestriction> facetRestrictions = ((OWLDatatypeRestrictionImpl) range).facetRestrictions()
					.collect(Collectors.toSet());
			Set<OWLFacetRestriction> selectedFacetRestrictions = CollectionUtil
					.getARandomElementFromList(CollectionUtil.getAllSubSetsOfASet(facetRestrictions), ran);
			Expr current = null;
			Expr temp = null;
			boolean ranBoolean;
			for (OWLFacetRestriction restriction : selectedFacetRestrictions) {
				temp = generateFilterExpressionFromDatatype(var, dt, restriction, queryNsPrefixes);
				if (temp != null) {
					if (current != null) {
						ranBoolean = ran.nextBoolean();
						if (ranBoolean)
							temp = new E_LogicalAnd(current, temp);
						else
							temp = new E_LogicalOr(current, temp);
					}
					if (ran.nextBoolean())
						temp = new E_LogicalNot(temp);
					current = temp;
				}
			}
			return current;
		} else if (range instanceof OWLNaryDataRangeImpl) {
			Set<OWLDataRange> rangeSet = CollectionUtil.getARandomElementFromList(CollectionUtil
					.getAllSubSetsOfASet(((OWLNaryDataRangeImpl) range).operands().collect(Collectors.toSet())), ran);
			Expr current = null;
			Expr temp = null;
			boolean ranBoolean;
			for (OWLDataRange sub : rangeSet) {
				temp = generateRecursiveFilterExpressionFromDataRange(var, sub, queryNsPrefixes);
				if (temp != null) {
					if (current != null) {
						ranBoolean = ran.nextBoolean();
						if (ranBoolean)
							temp = new E_LogicalAnd(current, temp);
						else
							temp = new E_LogicalOr(current, temp);
					}
					if (ran.nextBoolean())
						temp = new E_LogicalNot(temp);
					current = temp;
				}
			}
			return current;
		} else {
			logger.warn("OWL data range {} is ignored during query generation: " + range);
			return null;
		}
	}

	/**
	 * This function generates a filter expression based on the given OWL datatype.
	 * 
	 * @param var
	 *            Variable.
	 * @param dt
	 *            OWL datatype.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @return Filter expression.
	 * @throws Exception
	 *             If input arguments are invalid.
	 */
	private Expr generateFilterExpressionFromDatatype(Var var, OWLDatatype dt, Map<String, String> queryNsPrefixes)
			throws Exception {
		if (var == null || dt == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		if (!dt.isBuiltIn()) {
			logger.warn("None built-in datatype " + dt + " is not supported.");
			return null;
		}
		OWL2Datatype d2t = dt.getBuiltInDatatype();
		ExprVar exprVar = new ExprVar(var);
		NodeValue value = null;
		switch (d2t) {
		case XSD_BOOLEAN:
			value = NodeValue.makeNodeBoolean(String.valueOf(ran.nextBoolean()));
			break;
		case XSD_DECIMAL:
			value = NodeValue.makeNodeDecimal(MathUtil.getRandomDecimalInString(-100, 100, ran));
			break;
		case XSD_DOUBLE:
			value = NodeValue.makeNodeDouble(String.valueOf(MathUtil.getRandomDoubleInRange(-100, 100, ran)));
			break;
		case XSD_FLOAT:
			value = NodeValue.makeNodeFloat(String.valueOf(MathUtil.getRandomFloatInRange(-100, 100, ran)));
			break;
		case XSD_INT:
		case XSD_INTEGER:
			value = NodeValue.makeNodeInteger(
					String.valueOf(MathUtil.getRandomIntegerInRange(Integer.MIN_VALUE, Integer.MAX_VALUE, ran)));
			break;
		case XSD_NON_NEGATIVE_INTEGER:
			value = NodeValue
					.makeNodeInteger(String.valueOf(MathUtil.getRandomIntegerInRange(0, Integer.MAX_VALUE, ran)));
			break;
		case XSD_POSITIVE_INTEGER:
			value = NodeValue
					.makeNodeInteger(String.valueOf(MathUtil.getRandomIntegerInRange(1, Integer.MAX_VALUE, ran)));
			break;
		default:
			logger.warn("Unsupported OWL 2 datatype: " + d2t);
			return null;
		}
		queryNsPrefixes.put("xsd", XSD.getURI());
		return generateFilterExpression(exprVar, value);
	}

	/**
	 * This function generates a particular type of filter expression.
	 * 
	 * @param expr
	 *            Expression that is a variable in an expression.
	 * @param value
	 *            Node value that denotes a particular data value.
	 * @return Filter expression.
	 */
	private Expr generateFilterExpression(ExprVar expr, NodeValue value) {
		int ranInt = ran.nextInt(6);
		if (ranInt == 0)
			return new E_Equals(expr, value);
		else if (ranInt == 1)
			return new E_GreaterThan(expr, value);
		else if (ranInt == 2)
			return new E_GreaterThanOrEqual(expr, value);
		else if (ranInt == 3)
			return new E_LessThan(expr, value);
		else if (ranInt == 4)
			return new E_LessThanOrEqual(expr, value);
		else
			return new E_NotEquals(expr, value);
	}

	/**
	 * This function generates a filter expression based on the given OWL datatype
	 * restriction.
	 * 
	 * @param var
	 *            Variable.
	 * @param dt
	 *            OWL datatype.
	 * @param restriction
	 *            Facet restriction used to restrict the specified datatype.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @return Filter expression.
	 * @throws Exception
	 *             If input arguments are invalid.
	 */
	private Expr generateFilterExpressionFromDatatype(Var var, OWLDatatype dt, OWLFacetRestriction restriction,
			Map<String, String> queryNsPrefixes) throws Exception {
		if (var == null || dt == null || restriction == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		if (!dt.isBuiltIn()) {
			logger.warn("None built-in datatype " + dt + " is not supported.");
			return null;
		}
		OWL2Datatype d2t = dt.getBuiltInDatatype();
		ExprVar exprVar = new ExprVar(var);
		NodeValue value = null;
		OWLFacet facet = restriction.getFacet();
		OWLLiteral literal = restriction.getFacetValue();
		switch (d2t) {
		case XSD_BOOLEAN:
			value = NodeValue.makeNodeBoolean(literal.getLiteral());
			break;
		case XSD_DECIMAL:
			value = NodeValue.makeNodeDecimal(literal.getLiteral());
			break;
		case XSD_DOUBLE:
			value = NodeValue.makeNodeDouble(literal.getLiteral());
			break;
		case XSD_FLOAT:
			value = NodeValue.makeNodeFloat(literal.getLiteral());
			break;
		case XSD_INT:
		case XSD_INTEGER:
		case XSD_NON_NEGATIVE_INTEGER:
		case XSD_POSITIVE_INTEGER:
			value = NodeValue.makeNodeInteger(literal.getLiteral());
			break;
		default:
			logger.warn("Unsupported OWL 2 datatype: " + d2t);
			return null;
		}
		queryNsPrefixes.put("xsd", XSD.getURI());
		return generateFilterExpression(exprVar, value, facet);
	}

	/**
	 * This function generates a particular type of filter expression.
	 * 
	 * @param expr
	 *            Expression that is a variable in an expression.
	 * @param value
	 *            Node value that denotes a particular data value.
	 * @param facet
	 *            Facet used for restricting the specified datatype.
	 * @return Filter expression.
	 * @throws Exception
	 *             If input arguments are invalid.
	 */
	private Expr generateFilterExpression(ExprVar expr, NodeValue value, OWLFacet facet) throws Exception {
		if (expr == null || value == null || facet == null)
			throw new NullPointerException("null arguments.");
		switch (facet) {
		case MIN_INCLUSIVE:
			return new E_GreaterThanOrEqual(expr, value);
		case MIN_EXCLUSIVE:
			return new E_GreaterThan(expr, value);
		case MAX_INCLUSIVE:
			return new E_LessThanOrEqual(expr, value);
		case MAX_EXCLUSIVE:
			return new E_LessThan(expr, value);
		default:
			logger.warn("Unsupported OWL 2 facet: " + facet);
			return null;
		}
	}

	/**
	 * This function processes the given OWL object property expression so as to get
	 * its semantically equivalent OWL object property.
	 * 
	 * @param objectPropertyExp
	 *            OWL object property expression.
	 * @return OWL Object property.
	 */
	private OWLObjectProperty processObjectPropertyExpression(OWLObjectPropertyExpression objectPropertyExp) {
		if (objectPropertyExp == null)
			return null;
		OWLObjectProperty objectProperty;
		if (!objectPropertyExp.isAnonymous())
			objectProperty = objectPropertyExp.asOWLObjectProperty();
		else {
			OWLObjectPropertyExpression simplified = objectPropertyExp.getSimplified();
			if (simplified.isAnonymous())
				objectProperty = simplified.getInverseProperty().asOWLObjectProperty();
			else
				objectProperty = simplified.asOWLObjectProperty();
		}
		return objectProperty;
	}

	/**
	 * This function aims to generate a class assertion triple pattern.
	 * 
	 * @param var
	 *            Variable
	 * @param cls
	 *            OWL class
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @return A class assertion triple pattern
	 * @throws Exception
	 *             If there exists sub-functions throwing such exceptions.
	 */
	private Triple generateClassAssertionTriplePattern(Var var, OWLClass cls, Map<String, String> queryNsPrefixes)
			throws Exception {
		if (var == null || cls == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		if (cls.isOWLThing() || cls.isOWLNothing())
			return null;
		COWLClassImpl ocImpl = classMap.get(cls);
		prefixCheck(prefixName2PrefixMap, queryNsPrefixes, ocImpl.getIRI());
		return new Triple(var, RDF.Nodes.type, ocImpl.getNode());
	}

	/**
	 * This function generates a data property assertion triple pattern.
	 * 
	 * @param var1
	 *            Variable placed as subject.
	 * @param dataProperty
	 *            OWL data property.
	 * @param var2
	 *            Variable placed as object.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @return Triple t = (var1, dataProperty, var2).
	 * @throws Exception
	 *             If sub-function throws exception.
	 */
	private Triple generateDataPropertyAssertionTriplePattern(Var var1, OWLDataProperty dataProperty, Var var2,
			Map<String, String> queryNsPrefixes) throws Exception {
		if (var1 == null || dataProperty == null || var2 == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		COWLDataPropertyImpl odpImpl = dataPropertyMap.get(dataProperty);
		odpImpl = (COWLDataPropertyImpl) CollectionUtil.getARandomElementFromSet(odpImpl.getRelevantProperties(), ran);
		prefixCheck(prefixName2PrefixMap, queryNsPrefixes, odpImpl.getIRI());
		return new Triple(var1, odpImpl.getNode(), var2);
	}

	/**
	 * This function generates an object property assertion triple pattern.
	 * 
	 * @param var1
	 *            Variable.
	 * @param objectProperty
	 *            OWL object property.
	 * @param var2
	 *            Variable.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @return Triple.
	 * @throws Exception
	 *             If input arguments are invalid or sub-functions throws exception.
	 */
	private <T> Triple generateObjectPropertyAssertionTriplePattern(Var var1, OWLObjectProperty objectProperty, T var2,
			Map<String, String> queryNsPrefixes) throws Exception {
		if (var1 == null || objectProperty == null || var2 == null || queryNsPrefixes == null)
			throw new NullPointerException("null arguments.");
		COWLObjectPropertyImpl oopImpl = objectPropertyMap.get(objectProperty);
		oopImpl = (COWLObjectPropertyImpl) CollectionUtil.getARandomElementFromSet(oopImpl.getRelevantProperties(),
				ran);
		prefixCheck(prefixName2PrefixMap, queryNsPrefixes, oopImpl.getIRI());
		if (ran.nextDouble() < inverseObjectPropertySelectionProbability) {
			if (var2 instanceof OWLNamedIndividual) {
				prefixCheck(prefixName2PrefixMap, queryNsPrefixes, ((OWLNamedIndividual) var2).getIRI());
				return new Triple(NodeFactory.createURI(((OWLNamedIndividual) var2).getIRI().getIRIString()),
						oopImpl.getNode(), var1);
			} else if (var2 instanceof Var)
				return new Triple((Var) var2, oopImpl.getNode(), var1);
		} else {
			if (var2 instanceof OWLNamedIndividual) {
				prefixCheck(prefixName2PrefixMap, queryNsPrefixes, ((OWLNamedIndividual) var2).getIRI());
				return new Triple(var1, oopImpl.getNode(),
						NodeFactory.createURI(((OWLNamedIndividual) var2).getIRI().getIRIString()));
			} else if (var2 instanceof Var)
				return new Triple(var1, oopImpl.getNode(), (Var) var2);
		}

		throw new Exception("Object type of " + var2 + "is invalid!");
	}

	/**
	 * Join graph patterns with random key words (AND/UNION/OPTIONAL/FILTER NOT
	 * EXISTS/FILTER EXISTS/MINUS).
	 * 
	 * @param list
	 *            List of graph patterns.
	 * @param supportUnion
	 *            True if support UNION key word, False otherwise.
	 * @return A graph pattern that randomly joins all the specified graph patterns
	 *         with the specified key words.
	 */
	private ElementGroup joinGraphPatterns(LinkedList<ElementGroup> list, boolean supportUnion) {
		if (list == null)
			return null;
		if (list.isEmpty())
			return new ElementGroup();
		if (list.size() == 1)
			return list.poll();

		// Logger information
		// StringBuffer sb = new StringBuffer();
		// sb.append("List of element group information is shown below:\n");
		// for (ElementGroup sub : list)
		// sb.append(sub.toString() + "\n");
		// logger.info(sb.toString());

		// list = list \ subList
		LinkedList<ElementGroup> subList = new LinkedList<>();
		int count = ran.nextInt(list.size() - 1) + 1;
		while (count > 0) {
			subList.offer(list.poll());
			count--;
		}
		ElementGroup left = joinGraphPatterns(subList, supportUnion);
		ElementGroup right = joinGraphPatterns(list, supportUnion);
		double ranDouble = ran.nextDouble();
		if (ranDouble < conjunctionGraphPatternProbability) {
			left.addElement(right);
			return left;
		} else if (ranDouble < conjunctionGraphPatternProbability + unionGraphPatternProbability) {
			if (supportUnion) {
				ElementGroup group = new ElementGroup();
				ElementUnion union = new ElementUnion(left);
				union.addElement(right);
				group.addElement(union);
				return group;
			} else {
				left.addElement(right);
				return left;
			}
		} else if (ranDouble < conjunctionGraphPatternProbability + unionGraphPatternProbability
				+ optionalGraphPatternProbability) {
			left.addElement(new ElementOptional(right));
			return left;
		} else {
			// Join two graph patterns (left, right) with negation with random type (FILTER
			// EXISTS, FILTER NOT EXISTS or MINUS)
			count = ran.nextInt(3);
			switch (count) {
			case 0:
				// left.addElement(new ElementNotExists(right));
				left.addElementFilter(new ElementFilter(new E_NotExists(right)));
				break;
			case 1:
				// left.addElement(new ElementExists(right));
				left.addElementFilter(new ElementFilter(new E_Exists(right)));
				break;
			case 2:
				left.addElement(new ElementMinus(right));
				break;
			}
			return left;
		}
	}

	/**
	 * This function checks if namespace of the specified resource is included in
	 * the query prefix map.
	 * 
	 * @param prefixName2PrefixMap
	 *            A map that maps all possible prefix names to prefixes.
	 * @param queryNsPrefixes
	 *            A map that maps prefix names to prefixes used by the query.
	 * @param iri
	 *            IRI of the resource.
	 * @return True if the prefix name is already included in the query prefix
	 *         mapper, false otherwise.
	 * @throws Exception
	 *             If input arguments are invalid.
	 */
	private static boolean prefixCheck(Map<String, String> prefixName2PrefixMap, Map<String, String> queryNsPrefixes,
			IRI iri) throws Exception {
		if (prefixName2PrefixMap == null || queryNsPrefixes == null || iri == null)
			throw new NullPointerException("null arguments.");
		String namespace = iri.getNamespace();
		if (queryNsPrefixes.containsValue(namespace))
			return true;
		for (Entry<String, String> entry : prefixName2PrefixMap.entrySet()) {
			// System.out.println(entry.getValue() + "\t" + namespace);
			if (entry.getValue().equals(namespace)) {
				queryNsPrefixes.put(entry.getKey().replace(":", ""), namespace);
				return false;
			}
		}
		queryNsPrefixes.put(namespace.substring(namespace.lastIndexOf('/') + 1).replace("#", ""), namespace);
		return false;
		// throw new Exception("There exists an IRI whose namespace " + namespace + " is
		// not contained by the prefix mapper!");
	}

	/**
	 * This function dumps a SPARQL query with specified query ID and directory into
	 * file.
	 * 
	 * @param query
	 *            The number of queries.
	 * @param outputDirectory
	 *            Directory of the output queries
	 * @param queryID
	 *            Query number.
	 */
	public void dumpIntoFile(Query query, File outputDirectory, int queryID) {
		/*
		 * try { query.serialize(new IndentedWriter(new
		 * FileOutputStream(sparqlQueryFile), false), Syntax.syntaxSPARQL_11); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); }
		 */
		try {
			if (query == null || outputDirectory == null)
				throw new IOException();
			File sparqlQueryFile = new File(outputDirectory, "query" + queryID + ".rq");
			FileUtils.writeStringToFile(sparqlQueryFile, query.serialize(Syntax.syntaxSPARQL_11),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("There was an error while dumping into file.", e);
		}
	}

	/**
	 * Reset variable and class status, such as resetting all OWL classes as not
	 * visited, removing all variables and resetting the last variable indexes to 0.
	 * 
	 * @param classes
	 *            A collection of OWL classes.
	 */
	public void resetStatus(Collection<COWLClassImpl> classes) {
		if (classes == null)
			return;
		for (COWLClassImpl tc : classes) {
			if (tc.isVisited())
				tc.setVisited(false);
			tc.cleanUpVariables();
			VariableGenerator.resetNextBindToDataValueVariableIndex();
			VariableGenerator.resetNextBindToAnonymousClassExpressionVariableIndex();
		}
	}
}
