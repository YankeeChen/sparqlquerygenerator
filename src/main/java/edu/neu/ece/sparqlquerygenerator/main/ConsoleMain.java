package edu.neu.ece.sparqlquerygenerator.main;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.main.Controller;

/**
 * Main class, entry to the program.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-06-29
 */
public class ConsoleMain {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Relative path to configuration directory.
	 */
	private static final String CONFIG_PATH = "conf" + File.separator;

	/**
	 * Help option name on console.
	 */
	private static final String HELP_OPTION_NAME = "h";

	/**
	 * Ontology root class IRI option name on console.
	 */
	private static final String ROOT_CLASS_IRI = "rootIRI";

	/**
	 * Input instance data (ABox) URI option name on console.
	 */
	private static final String DATASET_URI = "datasetURI";

	/**
	 * Ontology IRI to document IRI mapping option name on console.
	 */
	private static final String MAPPING = "IRIMapping";

	/**
	 * The number of queries option name on console.
	 */
	private static final String QUERY_NUMBER = "queryNumber";

	/**
	 * Random seed option name on console.
	 */
	private static final String RAM_SEED = "ramSeed";

	/**
	 * Distinct query option name on console.
	 */
	private static final String DISTINCT_OPTION_NAME = "d";
	
	/**
	 * Output directory option name on console.
	 */
	private static final String OUTPUT_DIRECTORY_PATH = "outputDirectoryPath";

	/**
	 * Class constraint selection probability option name on console.
	 */
	private static final String CLASS_CONSTRAINT_SELECTION_PROBABILITY = "classConstraintSelectionProbability";

	/**
	 * Class assertion probability option name on console.
	 */
	private static final String CLASS_ASSERTION_PROBABILITY = "classAssertionProbability";

	/**
	 * Object property assertion probability option name on console.
	 */
	private static final String OBJECT_PROPERTY_ASSERTION_PROBABILITY = "objectPropertyAssertionProbability";

	/**
	 * Data property assertion probability option name on console.
	 */
	private static final String DATA_PROPERTY_ASSERTION_PROBABILITY = "dataPropertyAssertionProbability";

	/**
	 * Inverse object property selection probability option name on console.
	 */
	private static final String INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY = "inverseObjectPropertySelectionProbability";

	/**
	 * New variable probability option name on console.
	 */
	private static final String NEW_VARIABLE_PROBABILITY = "newVariableProbability";

	/**
	 * Link to individual probability option name on console.
	 */
	private static final String LINK_TO_INDIVIDUAL_PROBABILITY = "linkToIndividualProbability";

	/**
	 * Filter probability option name on console.
	 */
	private static final String FILTER_PROBABILITY = "filterProbability";

	/**
	 * Conjunction graph pattern probability option name on console.
	 */
	private static final String CONJUNCTION_GRAPH_PATTERN_PROBABILITY = "conjunctionGraphPatternProbability";

	/**
	 * Optional graph pattern probability option name on console.
	 */
	private static final String OPTIONAL_GRAPH_PATTERN_PROBABILITY = "optionalGraphPatternProbability";

	/**
	 * Union graph pattern probability option name on console.
	 */
	private static final String UNION_GRAPH_PATTERN_PROBABILITY = "unionGraphPatternProbability";

	/**
	 * Negation graph pattern probability option name on console.
	 */
	private static final String NEGATION_GRAPH_PATTERN_PROBABILITY = "negationGraphPatternProbability";

	// Static block
	static {
		PropertyConfigurator.configure(CONFIG_PATH + "log4j.properties");
	}

	/**
	 * Main function, entrance to the program.
	 * 
	 * @param args
	 *            Arguments from console.
	 * @throws URISyntaxException
	 *             If ontology IRI creation failed.
	 * @throws IOException
	 *             If I/O exceptions occur.
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
		new ConsoleMain().parseCommandLine(args);
	}

	/**
	 * Parse arguments from console.
	 * 
	 * @param args
	 *            Arguments from console.
	 * @throws URISyntaxException
	 *             If ontology IRI creation failed.
	 * @throws IOException
	 *             If I/O exceptions occur.
	 */
	public void parseCommandLine(String[] args) throws URISyntaxException, IOException {
		CommandLine line = null;
		Options options = createOptions();

		try {
			line = new DefaultParser().parse(options, args);
		} catch (ParseException exp) {
			logger.error("Parsing failed. Reason:" + exp.getMessage());
			printHelpMenu(options);
			System.exit(1);
		}

		if (line.hasOption(HELP_OPTION_NAME)) {
			printHelpMenu(options);
			System.exit(1);
		}

		String rootIRIString = line.getOptionValue(ROOT_CLASS_IRI);
		String datasetURIString = line.getOptionValue(DATASET_URI);

		IRI ontologyIRI = IRI.create(new URI(datasetURIString));
		// IRI ontologyIRI = IRI.create(new File(datasetURIString));
		Controller.Builder builder = new Controller.Builder(ontologyIRI, rootIRIString);

		logger.info(ROOT_CLASS_IRI + " = " + rootIRIString);
		logger.info(DATASET_URI + " = " + datasetURIString);
		if (line.hasOption(MAPPING)) {
			String mapping = line.getOptionValue(MAPPING);
			Map<String, String> ontologyIRIMapper = new HashMap<>();
			String[] IRIMapperString = mapping.split(";");
			String[] bindingPair;
			for (int i = 0; i < IRIMapperString.length; i++) {
				bindingPair = IRIMapperString[i].split(",");
				if (bindingPair.length != 2) {
					logger.error("Incorrect format for ontology IRI to document IRI mapping.");
					System.exit(1);
				}
				// logger.info(bindingPair[0] + "\t" + bindingPair[1]);
				ontologyIRIMapper.put(bindingPair[0], bindingPair[1]);
			}
			logger.info(MAPPING + " = " + mapping);
			builder.setOntologyIRIMapper(ontologyIRIMapper);
		}

		int queryNumber = 1;
		if (line.hasOption(QUERY_NUMBER)) {
			queryNumber = Integer.parseInt(line.getOptionValue(QUERY_NUMBER));
			if (queryNumber < 0) {
				logger.error("Query number must be a positive number.");
				System.exit(1);
			}
			logger.info(QUERY_NUMBER + " = " + queryNumber);
			builder.setQueryNumber(queryNumber);
		}

		if (line.hasOption(RAM_SEED)) {
			int ramSeed = Integer.parseInt(line.getOptionValue(RAM_SEED));
			logger.info(RAM_SEED + " = " + ramSeed);
			builder.setSeed(ramSeed);
		}
		
		if (line.hasOption(DISTINCT_OPTION_NAME)) {
			logger.info("Distinct queries supported");
			builder.supportDistinct();			
		}
		
		if (line.hasOption(OUTPUT_DIRECTORY_PATH)) {
			File outputDirectory = new File(line.getOptionValue(OUTPUT_DIRECTORY_PATH));
			outputDirectory.mkdirs();
			logger.info(OUTPUT_DIRECTORY_PATH + " = " + line.getOptionValue(OUTPUT_DIRECTORY_PATH));
			builder.setOutputDirectory(outputDirectory);
		}

		if (line.hasOption(CLASS_CONSTRAINT_SELECTION_PROBABILITY)) {
			double classConstraintSelectionProbability = Double
					.parseDouble(line.getOptionValue(CLASS_CONSTRAINT_SELECTION_PROBABILITY));
			if (classConstraintSelectionProbability < 0.0 || classConstraintSelectionProbability > 1.0) {
				logger.error("Class constraint selection probability is out of range [0, 1].");
				System.exit(1);
			}
			logger.info(CLASS_CONSTRAINT_SELECTION_PROBABILITY + " = " + classConstraintSelectionProbability);
			builder.setClassConstraintSelectionProbability(classConstraintSelectionProbability);
		}

		if (line.hasOption(CLASS_ASSERTION_PROBABILITY)) {
			double classAssertionProbability = Double.parseDouble(line.getOptionValue(CLASS_ASSERTION_PROBABILITY));
			if (classAssertionProbability < 0.0 || classAssertionProbability > 1.0) {
				logger.error("Class assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(CLASS_ASSERTION_PROBABILITY + " = " + classAssertionProbability);
			builder.setClassAssertionProbability(classAssertionProbability);
		}

		if (line.hasOption(OBJECT_PROPERTY_ASSERTION_PROBABILITY)) {
			double objectPropertyAssertionProbability = Double
					.parseDouble(line.getOptionValue(OBJECT_PROPERTY_ASSERTION_PROBABILITY));
			if (objectPropertyAssertionProbability < 0.0 || objectPropertyAssertionProbability > 1.0) {
				logger.error("Object property assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(OBJECT_PROPERTY_ASSERTION_PROBABILITY + " = " + objectPropertyAssertionProbability);
			builder.setObjectPropertyAssertionProbability(objectPropertyAssertionProbability);
		}

		if (line.hasOption(DATA_PROPERTY_ASSERTION_PROBABILITY)) {
			double dataPropertyAssertionProbability = Double
					.parseDouble(line.getOptionValue(DATA_PROPERTY_ASSERTION_PROBABILITY));
			if (dataPropertyAssertionProbability < 0.0 || dataPropertyAssertionProbability > 1.0) {
				logger.error("Data property assertion probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(DATA_PROPERTY_ASSERTION_PROBABILITY + " = " + dataPropertyAssertionProbability);
			builder.setDataPropertyAssertionProbability(dataPropertyAssertionProbability);
		}

		if (line.hasOption(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY)) {
			double inverseObjectPropertySelectionProbability = Double
					.parseDouble(line.getOptionValue(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY));
			if (inverseObjectPropertySelectionProbability < 0.0 || inverseObjectPropertySelectionProbability > 1.0) {
				logger.error("Inverse object property selection probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(
					INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY + " = " + inverseObjectPropertySelectionProbability);
			builder.setInverseObjectPropertySelectionProbability(inverseObjectPropertySelectionProbability);
		}

		if (line.hasOption(NEW_VARIABLE_PROBABILITY)) {
			double newVariableProbability = Double.parseDouble(line.getOptionValue(NEW_VARIABLE_PROBABILITY));
			if (newVariableProbability < 0.0 || newVariableProbability > 1.0) {
				logger.error("New variable probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(NEW_VARIABLE_PROBABILITY + " = " + newVariableProbability);
			builder.setNewVariableProbability(newVariableProbability);
		}

		if (line.hasOption(LINK_TO_INDIVIDUAL_PROBABILITY)) {
			double linkToIndividualProbability = Double
					.parseDouble(line.getOptionValue(LINK_TO_INDIVIDUAL_PROBABILITY));
			if (linkToIndividualProbability < 0.0 || linkToIndividualProbability > 1.0) {
				logger.error("Link to individual probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(LINK_TO_INDIVIDUAL_PROBABILITY + " = " + linkToIndividualProbability);
			builder.setLinkToIndividualProbability(linkToIndividualProbability);
		}

		if (line.hasOption(FILTER_PROBABILITY)) {
			double filterProbability = Double.parseDouble(line.getOptionValue(FILTER_PROBABILITY));
			if (filterProbability < 0.0 || filterProbability > 1.0) {
				logger.error("Filter probability is out of range [0, 1]");
				System.exit(1);
			}
			logger.info(FILTER_PROBABILITY + " = " + filterProbability);
			builder.setFilterProbability(filterProbability);
		}

		double conjunctionGraphPatternProbablity = 0.7;
		double optionalGraphPatternProbability = 0.1;
		double unionGraphPatternProbability = 0.1;
		double negationGraphPatternProbability = 0.1;

		if (line.hasOption(CONJUNCTION_GRAPH_PATTERN_PROBABILITY)) {
			conjunctionGraphPatternProbablity = Double
					.parseDouble(line.getOptionValue(CONJUNCTION_GRAPH_PATTERN_PROBABILITY));
			if (conjunctionGraphPatternProbablity < 0.0 || conjunctionGraphPatternProbablity > 1.0) {
				logger.error("Conjunction graph pattern probability is out of range [0, 1]");
				System.exit(1);
			}
		}
		if (line.hasOption(OPTIONAL_GRAPH_PATTERN_PROBABILITY)) {
			optionalGraphPatternProbability = Double
					.parseDouble(line.getOptionValue(OPTIONAL_GRAPH_PATTERN_PROBABILITY));
			if (optionalGraphPatternProbability < 0.0 || optionalGraphPatternProbability > 1.0) {
				logger.error("Optional graph pattern probability is out of range [0, 1]");
				System.exit(1);
			}
		}
		if (line.hasOption(UNION_GRAPH_PATTERN_PROBABILITY)) {
			unionGraphPatternProbability = Double.parseDouble(line.getOptionValue(UNION_GRAPH_PATTERN_PROBABILITY));
			if (unionGraphPatternProbability < 0.0 || unionGraphPatternProbability > 1.0) {
				logger.error("Union graph pattern probability is out of range [0, 1]");
				System.exit(1);
			}
		}
		if (line.hasOption(NEGATION_GRAPH_PATTERN_PROBABILITY)) {
			negationGraphPatternProbability = Double
					.parseDouble(line.getOptionValue(NEGATION_GRAPH_PATTERN_PROBABILITY));
			if (negationGraphPatternProbability < 0.0 || negationGraphPatternProbability > 1.0) {
				logger.error("Union graph pattern probability is out of range [0, 1]");
				System.exit(1);
			}
		}
		double sum = conjunctionGraphPatternProbablity + optionalGraphPatternProbability + unionGraphPatternProbability
				+ negationGraphPatternProbability;
		if (Math.abs(sum - 1.0) >= 1.0E-9) {
			logger.error(
					"The sum of generation probablities of four types of graph patterns (conjunction, optional, union and negation) MUST be 1 instead of {}, please reassign their values accordingly.",
					sum);
			System.exit(1);
		}
		logger.info(
				CONJUNCTION_GRAPH_PATTERN_PROBABILITY + " = {}; " + OPTIONAL_GRAPH_PATTERN_PROBABILITY + " = {}; "
						+ UNION_GRAPH_PATTERN_PROBABILITY + " = {}; " + NEGATION_GRAPH_PATTERN_PROBABILITY + " = {}",
				conjunctionGraphPatternProbablity, optionalGraphPatternProbability, unionGraphPatternProbability,
				negationGraphPatternProbability);
		builder.setGraphPatternProbability(conjunctionGraphPatternProbablity, optionalGraphPatternProbability,
				unionGraphPatternProbability);

		builder.build().generateSPARQLQueries();
	}

	/**
	 * Create Option objects from option names.
	 * 
	 * @return Option objects.
	 */
	private Options createOptions() {
		Options options = new Options();
		options.addOption(Option.builder(HELP_OPTION_NAME).desc("Views this help text").build());

		Option opt = Option.builder(ROOT_CLASS_IRI).argName("IRI").hasArg().desc("Root class IRI as string").build();
		opt.setRequired(true);
		options.addOption(opt);

		opt = Option.builder(DATASET_URI).argName("URI").hasArg().desc("URI that relates to the instance data").build();
		opt.setRequired(true);
		options.addOption(opt);

		options.addOption(Option.builder(MAPPING).argName("IRIMapping1,IRIMapping2,IRIMapping3...").hasArg().desc(
				"Ontology IRIs to document IRIs mapping. Mappings are splitted by semicolon whereas ontology IRI and document IRI within each mapping are splitted by comma.")
				.build());
		options.addOption(Option.builder(QUERY_NUMBER).argName("NUMBER").hasArg()
				.desc("The number of queries; 1 by default").build());
		options.addOption(Option.builder(OUTPUT_DIRECTORY_PATH).argName("PATH").hasArg()
				.desc("Directory of the output queries; SPARQLqueries/ by default").build());
		options.addOption(Option.builder(RAM_SEED).argName("SEED").hasArg()
				.desc("Random seed used for random query generation; 0 by default").build());
		options.addOption(Option.builder(DISTINCT_OPTION_NAME).desc("Generate distinct queries").build());
		options.addOption(Option.builder(CLASS_CONSTRAINT_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of selecting an OWL class constraint (anonymous super class expression/anonymous equivalent class expression/anonymous disjoint class expression) of an OWL named class; 0.9 by default")
				.build());
		options.addOption(Option.builder(CLASS_ASSERTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of generating class assertion triple pattern; 1.0 by default").build());
		options.addOption(Option.builder(OBJECT_PROPERTY_ASSERTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of creating a new object property assertion triple pattern; 0.5 by default")
				.build());
		options.addOption(Option.builder(DATA_PROPERTY_ASSERTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of creating a new data property assertion triple pattern; 0.5 by default")
				.build());
		options.addOption(Option.builder(INVERSE_OBJECT_PROPERTY_SELECTION_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of selecting an inverse property (direct or inferred) of an object property for object property assertion triple pattern generation; 0.8 by default")
				.build());

		options.addOption(Option.builder(NEW_VARIABLE_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probability of creating a new variable; 0.5 by default").build());
		options.addOption(Option.builder(LINK_TO_INDIVIDUAL_PROBABILITY).argName("PROBABILITY").hasArg()
				.desc("The probablity of linking a variable to an individual; 0.2 by default").build());
		options.addOption(Option.builder(FILTER_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of creating a filter expression that restricts the solutions of a graph pattern; 0.5 by default")
				.build());

		options.addOption(Option.builder(CONJUNCTION_GRAPH_PATTERN_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of creating a conjunction graph pattern; 0.8 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.")
				.build());
		options.addOption(Option.builder(OPTIONAL_GRAPH_PATTERN_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of creating an optional graph pattern; 0 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.")
				.build());
		options.addOption(Option.builder(UNION_GRAPH_PATTERN_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of creating an union graph pattern; 0.15 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.")
				.build());
		options.addOption(Option.builder(NEGATION_GRAPH_PATTERN_PROBABILITY).argName("PROBABILITY").hasArg().desc(
				"The probability of creating a negation graph pattern; 0.05 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.")
				.build());
		return options;
	}

	/**
	 * Print help menu, in the case when invalid input occurs.
	 * 
	 * @param options
	 *            Option objects.
	 */
	private void printHelpMenu(Options options) {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("java -jar sparqlquerygenerator-1.0-SNAPSHOT.jar", options);
	}

}
