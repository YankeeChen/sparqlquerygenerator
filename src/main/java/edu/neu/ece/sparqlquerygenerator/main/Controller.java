package edu.neu.ece.sparqlquerygenerator.main;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.evaluator.Evaluator;
import edu.neu.ece.sparqlquerygenerator.generator.SPARQLQueryGenerator;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyIRIMapperImpl;

/**
 * This class is used for controlling the whole query generation generation.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-06-29
 */
public class Controller {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Ontology root class IRI as string.
	 */
	private final String rootIRIString;

	/**
	 * Input instance data (ABox) IRI.
	 */
	private final IRI ontologyIRI;

	/**
	 * Mapping ontology IRIs to document IRIs; null by default.
	 */
	private final OWLOntologyIRIMapperImpl IRIMapper;

	/**
	 * The number of queries; 1 by default.
	 */
	private final int queryNumber;

	/**
	 * Random seed for query generation; 1 by default.
	 */
	private final long seed;

	/**
	 * Generate distinct queries.
	 */
	private final boolean distinct;
	
	/**
	 * Directory of the output queries; SPARQLqueries by default.
	 */
	private final File outputDirectory;

	/**
	 * The probability of selecting an OWL class constraint (anonymous super class
	 * expressions and anonymous equivalent class expressions) of an OWL class; 0.9
	 * by default.
	 */
	private final double classConstraintSelectionProbability;

	/**
	 * The probability of generating class assertion triple pattern for each named
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
	 * The probability of creating a new variable; 0.5 by default.
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
	 * Hold of an ontology manager.
	 */
	private OWLOntologyManager manager;

	/**
	 * Input instance data (ABox).
	 */
	private OWLOntology ont;

	/**
	 * A map that maps prefix names to prefixes.
	 */
	private Map<String, String> prefixName2PrefixMap;

	/**
	 * A build-in OWL reasoner from OWL API.
	 */
	private OWLReasoner reasoner;

	/**
	 * Inner static Builder class.
	 */
	public static class Builder {
		// Required parameters
		/**
		 * Root class IRI as string.
		 */
		private final String rootIRIString;

		/**
		 * Input instance data (ABox) IRI.
		 */
		private final IRI ontologyIRI;

		// Optional parameters - initialized to default values
		/**
		 * Mapping ontology IRIs to document IRIs; null by default.
		 */
		private OWLOntologyIRIMapperImpl IRIMapper = null;

		/**
		 * The number of queries; 1 by default.
		 */
		private int queryNumber = 1;

		/**
		 * Random seed for query generation; 1 by default.
		 */
		private long seed = queryNumber;
		
		/**
		 * Generate distinct queries.
		 */
		private boolean distinct = false;

		/**
		 * Directory of the output queries; SPARQLqueries by default.
		 */
		private File outputDirectory = new File("SPARQLqueries");

		/**
		 * The probability of selecting an OWL class constraint (anonymous super class
		 * expression, anonymous equivalent class expression and anonymous disjoint
		 * class expression) of an OWL class; 0.9 by default.
		 */
		private double classConstraintSelectionProbability = 0.9;

		/**
		 * The probability of generating class assertion triple pattern; 1.0 by default.
		 */
		private double classAssertionProbability = 1.0;

		/**
		 * The probability of creating an object property assertion triple pattern; 0.5
		 * by default.
		 */
		private double objectPropertyAssertionProbability = 0.5;

		/**
		 * The probability of creating a data property assertion triple pattern; 0.5 by
		 * default.
		 */
		private double dataPropertyAssertionProbability = 0.5;

		/**
		 * The probability of selecting an inverse property (direct or inferred) of an
		 * object property for object property assertion triple pattern generation; 0.8
		 * by default.
		 */
		private double inverseObjectPropertySelectionProbability = 0.8;

		/**
		 * The probability of creating a new variable; 0.5 by default.
		 */
		private double newVariableProbability = 0.5;

		/**
		 * The probability of linking a variable to an individual; 0.2 by default.
		 */
		private double linkToIndividualProbability = 0.2;

		/**
		 * The probability of creating a filter that restricts the solutions of a graph
		 * pattern; 0.5 by default.
		 */
		private double filterProbability = 0.5;

		/**
		 * The probability of creating a conjunction graph pattern; 0.8 by default.
		 */
		private double conjunctionGraphPatternProbability = 0.8;

		/**
		 * The probability of creating an optional graph pattern; 0 by default.
		 */
		private double optionalGraphPatternProbability = 0;

		/**
		 * The probability of creating an union graph pattern; 0.15 by default.
		 */
		private double unionGraphPatternProbability = 0.15;

		/**
		 * Constructor.
		 * 
		 * @param ontologyIRI
		 *            Input instance data (ABox) IRI.
		 * @param rootIRIString
		 *            Root class IRI as string.
		 */
		public Builder(IRI ontologyIRI, String rootIRIString) {
			this.ontologyIRI = ontologyIRI;
			this.rootIRIString = rootIRIString;
			outputDirectory.mkdirs();
		}

		/**
		 * Set ontology IRI mapper.
		 * 
		 * @param mapper
		 *            Ontology IRI mapper.
		 * @return Current Builder object.
		 * @throws URISyntaxException
		 *             If document URI creation fails.
		 */
		public Builder setOntologyIRIMapper(Map<String, String> mapper) throws URISyntaxException {
			IRIMapper = new OWLOntologyIRIMapperImpl();
			for (Entry<String, String> entry : mapper.entrySet())
				IRIMapper.addMapping(IRI.create(entry.getKey()), IRI.create(new URI(entry.getValue())));
			return this;
		}

		/**
		 * Set the number of queries.
		 * 
		 * @param queryNumber
		 *            The number of queries.
		 * @return Current Builder object.
		 */
		public Builder setQueryNumber(int queryNumber) {
			this.queryNumber = queryNumber;
			this.seed = this.queryNumber;
			return this;
		}

		/**
		 * Set the random seed for query generation.
		 * 
		 * @param baseSeed
		 *            Random base seed.
		 * @return Current Builder object.
		 */
		public Builder setSeed(int baseSeed) {
			long seed = baseSeed * (Integer.MAX_VALUE + 1) + queryNumber;
			this.seed = seed;
			return this;
		}

		/**
		 * Support distinct queries.
		 * 
		 * @return Current Builder object.
		 */
		public Builder supportDistinct() {
			distinct = true;
			return this;
		}
		
		/**
		 * Set directory of the generated queries.
		 * 
		 * @param outputDirectory
		 *            Output directory.
		 * @return Current Builder object.
		 */
		public Builder setOutputDirectory(File outputDirectory) {
			this.outputDirectory = outputDirectory;
			return this;
		}

		/**
		 * Set the probability of selecting an OWL class constraint (anonymous super
		 * class expression/anonymous equivalent class expression/disjoint class
		 * expression) of an OWL class
		 * 
		 * @param classConstraintSelectionProbability
		 *            The probability of selecting an OWL class constraint (anonymous
		 *            super class expression/anonymous equivalent class
		 *            expression/disjoint class expression) of an OWL class.
		 * @return Current Builder object.
		 */
		public Builder setClassConstraintSelectionProbability(double classConstraintSelectionProbability) {
			this.classConstraintSelectionProbability = classConstraintSelectionProbability;
			return this;
		}

		/**
		 * Set the probability of generating class assertion axioms for each named
		 * individual.
		 * 
		 * @param classAssertionProbability
		 *            The probability of generating class assertion axioms for each
		 *            named individual.
		 * @return Current Builder object.
		 */
		public Builder setClassAssertionProbability(double classAssertionProbability) {
			this.classAssertionProbability = classAssertionProbability;
			return this;
		}

		/**
		 * Set the probability of creating an object property assertion triple pattern.
		 * 
		 * @param objectPropertyAssertionProbability
		 *            The probability of creating an object property assertion triple
		 *            pattern.
		 * @return Current Builder object.
		 */
		public Builder setObjectPropertyAssertionProbability(double objectPropertyAssertionProbability) {
			this.objectPropertyAssertionProbability = objectPropertyAssertionProbability;
			return this;
		}

		/**
		 * Set the probability of creating a data property assertion triple pattern.
		 * 
		 * @param dataPropertyAssertionProbability
		 *            The probability of creating a data property assertion triple
		 *            pattern.
		 * @return Current Builder object.
		 */
		public Builder setDataPropertyAssertionProbability(double dataPropertyAssertionProbability) {
			this.dataPropertyAssertionProbability = dataPropertyAssertionProbability;
			return this;
		}

		/**
		 * Set the probability of selecting an inverse property (direct or inferred) of
		 * an object property for object property assertion triple pattern generation
		 * 
		 * @param inverseObjectPropertySelectionProbability
		 *            The probability of selecting an inverse property (direct or
		 *            inferred) of an object property for object property assertion
		 *            triple pattern generation.
		 * @return Current Builder object.
		 */
		public Builder setInverseObjectPropertySelectionProbability(double inverseObjectPropertySelectionProbability) {
			this.inverseObjectPropertySelectionProbability = inverseObjectPropertySelectionProbability;
			return this;
		}

		/**
		 * Set the probability of creating a new variable that binds to individuals of
		 * an OWL named class.
		 * 
		 * @param newVariableProbability
		 *            The probability of creating a new variable that binds to
		 *            individuals of an OWL named class.
		 * @return Current Builder object.
		 */
		public Builder setNewVariableProbability(double newVariableProbability) {
			this.newVariableProbability = newVariableProbability;
			return this;
		}

		/**
		 * Set the probability of linking a variable to an individual.
		 * 
		 * @param linkToIndividualProbability
		 *            The probability of linking a variable to an individual.
		 * @return Current Builder object.
		 */
		public Builder setLinkToIndividualProbability(double linkToIndividualProbability) {
			this.linkToIndividualProbability = linkToIndividualProbability;
			return this;
		}

		/**
		 * Set the probability of creating a filter that restricts the solutions of a
		 * graph pattern; 0.5 by default.
		 * 
		 * @param filterProbability
		 *            The probability of creating a filter that restricts the solution
		 *            of a graph pattern.
		 * @return Current Builder object.
		 */
		public Builder setFilterProbability(double filterProbability) {
			this.filterProbability = filterProbability;
			return this;
		}

		/**
		 * Set graph pattern probabilities.
		 * 
		 * @param conjunctionGraphPatternProbability
		 *            Set the probability of creating a conjunction graph pattern.
		 * @param optionalGraphPatternProbability
		 *            Set the probability of creating an optional graph pattern.
		 * @param unionGraphPatternProbability
		 *            Set the probability of creating an union graph pattern.
		 * @return Current Bulider object.
		 */
		public Builder setGraphPatternProbability(double conjunctionGraphPatternProbability,
				double optionalGraphPatternProbability, double unionGraphPatternProbability) {
			this.conjunctionGraphPatternProbability = conjunctionGraphPatternProbability;
			this.optionalGraphPatternProbability = optionalGraphPatternProbability;
			this.unionGraphPatternProbability = unionGraphPatternProbability;
			return this;
		}

		/**
		 * Create an instance of Controller with Builder.
		 * 
		 * @return An instance of Controller.
		 * @throws IOException
		 *             If I/O exceptions occur.
		 */
		public Controller build() throws IOException {
			return new Controller(this);
		}
	}

	/**
	 * Private constructor.
	 * 
	 * @param builder
	 *            Builder that instantiates Controller using Builder design pattern.
	 * @throws IOException
	 *             If I/O exception occurs.
	 */
	private Controller(Builder builder) throws IOException {
		this.rootIRIString = builder.rootIRIString;
		this.ontologyIRI = builder.ontologyIRI;
		this.IRIMapper = builder.IRIMapper;
		this.queryNumber = builder.queryNumber;
		this.seed = builder.seed;
		this.distinct = builder.distinct;
		
		this.outputDirectory = builder.outputDirectory;

		FileUtils.cleanDirectory(outputDirectory);
		this.classConstraintSelectionProbability = builder.classConstraintSelectionProbability;
		this.classAssertionProbability = builder.classAssertionProbability;
		this.objectPropertyAssertionProbability = builder.objectPropertyAssertionProbability;
		this.dataPropertyAssertionProbability = builder.dataPropertyAssertionProbability;
		this.inverseObjectPropertySelectionProbability = builder.inverseObjectPropertySelectionProbability;
		this.newVariableProbability = builder.newVariableProbability;
		this.linkToIndividualProbability = builder.linkToIndividualProbability;
		this.filterProbability = builder.filterProbability;
		this.conjunctionGraphPatternProbability = builder.conjunctionGraphPatternProbability;
		this.optionalGraphPatternProbability = builder.optionalGraphPatternProbability;
		this.unionGraphPatternProbability = builder.unionGraphPatternProbability;
	}

	/**
	 * This function defines the whole control flow of SPARQL query generation
	 * process.
	 */
	public void generateSPARQLQueries() {
		try {
			loadOntology();
			OntologyExtractor extractor = new OntologyExtractor(ont, reasoner);
			extractor.extract();
			long timeStart = System.currentTimeMillis();
			SPARQLQueryGenerator generator = new SPARQLQueryGenerator(rootIRIString, queryNumber, seed, distinct, outputDirectory,
					classConstraintSelectionProbability, classAssertionProbability, objectPropertyAssertionProbability,
					dataPropertyAssertionProbability, inverseObjectPropertySelectionProbability, newVariableProbability,
					linkToIndividualProbability, filterProbability, conjunctionGraphPatternProbability,
					optionalGraphPatternProbability, unionGraphPatternProbability, manager.getOWLDataFactory(),
					extractor, prefixName2PrefixMap);
			ArrayList<Query> queries = generator.generateRandomSPARQLQueries();
			long totalTime = System.currentTimeMillis() - timeStart;
			logger.info("The time for generating " + queryNumber + " SPARQL queries is: " + totalTime + " ms.");
			Evaluator evaluator = new Evaluator(queries, generator);
			evaluator.evaluate();
		} catch (OWLOntologyCreationException e) {
			logger.error("Error : Parsing ontologies failed. Reason: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Error : Generate queries failed. Reason: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * This function loads input instance data (ABox) with ontological model (TBox)
	 * and checks consistency using a build-in reasoner.
	 * 
	 * @throws OWLOntologyCreationException
	 *             if failed to load input ontology.
	 */
	public void loadOntology() throws OWLOntologyCreationException {
		logger.info("Begin loading ontologies...");
		// logger.info("The absolute path of the input ontology is " +
		// inputFile.getAbsolutePath());

		manager = OWLManager.createOWLOntologyManager();

		if (IRIMapper != null)
			manager.getIRIMappers().add(IRIMapper);
		// else
		// manager.getIRIMappers().add(new SimpleIRIMapper(ontologyIRI, ontologyIRI));
		// manager.getIRIMappers().add(new OWLZipClosureIRIMapper(f.getParentFile()));

		ont = manager.loadOntology(ontologyIRI);

		initPrefixMapper();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		// OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
		// System.out.println(reasonerFactory.getReasonerName());
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);

		// Create a reasoner that will reason over our ontology and its imports
		// closure.
		// Pass in the configuration.
		reasoner = reasonerFactory.createReasoner(ont, config);

		// Ask the reasoner to do all the necessary work now
		reasoner.precomputeInferences();
		if (!reasoner.isConsistent()) {
			logger.error("Ontology inconsistency : The loaded ontologies are inconsistent");
			throw new OWLOntologyCreationException();
		}
		// If an ontology doesn't have an ontology IRI then we say that it is
		// "anonymous"
		String logOntoName;
		if (!ont.isAnonymous()) {
			logOntoName = ont.getOntologyID().getOntologyIRI().get().toString();
		} else {
			logOntoName = ontologyIRI.toURI().toString();
			// System.out.println(logOntoName);
			logger.info("Ontology IRI is anonymous. Use loaded document URI instead.");
			// logger.error("No ontolgoy IRI : The loaded ontology doesn't have an ontology
			// IRI");
			// throw new OWLOntologyCreationException();
		}
		logger.info("Ontologies loaded successfully! Main Ontology: " + logOntoName);
	}

	/**
	 * Generate a map that maps prefix names to prefixes. The mapping originates
	 * from two parts: One comes from the input ontology and the other is generated
	 * by the program based on ontology IRIs of import closure of the ontology.
	 * 
	 */
	private void initPrefixMapper() {
		PrefixDocumentFormat df = (PrefixDocumentFormat) manager.getOntologyFormat(ont);
		prefixName2PrefixMap = new HashMap<>(df.getPrefixName2PrefixMap());
		// prefixName2PrefixMap.remove(":");

		Stream<OWLOntology> importOntStream = ont.imports();
		Set<OWLOntology> importOntSet = importOntStream.collect(Collectors.toSet());
		importOntSet.add(ont);

		for (OWLOntology importOnt : importOntSet) {
			IRI ontIRI = importOnt.getOntologyID().getOntologyIRI().get();
			if (!prefixName2PrefixMap.containsValue(ontIRI.getIRIString() + "#")) {
				// System.out.println(ontIRI.getShortForm() + ":" + "\t\t" +
				// ontIRI.getIRIString() + "#");
				if (ontIRI.getShortForm().lastIndexOf('.') != -1)
					prefixName2PrefixMap.put(
							ontIRI.getShortForm().substring(0, ontIRI.getShortForm().lastIndexOf('.')) + ":",
							ontIRI.getIRIString() + "#");
				else
					prefixName2PrefixMap.put(ontIRI.getShortForm() + ":", ontIRI.getIRIString() + "#");
			}
		}

		for (Entry<String, String> entry : prefixName2PrefixMap.entrySet())
			System.out.println(entry.getKey() + "\t" + entry.getValue());
	}
}
