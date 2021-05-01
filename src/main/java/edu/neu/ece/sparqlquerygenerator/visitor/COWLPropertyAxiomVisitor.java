package edu.neu.ece.sparqlquerygenerator.visitor;

import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLProperty;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.main.OntologyExtractor;

/**
 * This class defines a generic OWL property axiom visitor.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLPropertyAxiomVisitor implements OWLAxiomVisitor {
	// private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Defines ontology extractor used for processing OWL axioms from input
	 * ontology.
	 */
	protected final OntologyExtractor oe;

	/**
	 * OWL property.
	 */
	protected final OWLProperty owlProperty;

	/**
	 * Constructor
	 * 
	 * @param oe
	 *            Ontology extractor used for processing OWL axioms from input
	 *            ontology.
	 * @param owlProperty
	 *            OWL property.
	 */
	public COWLPropertyAxiomVisitor(OntologyExtractor oe, OWLProperty owlProperty) {
		this.oe = oe;
		this.owlProperty = owlProperty;
	}
}
