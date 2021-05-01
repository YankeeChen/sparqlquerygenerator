package edu.neu.ece.sparqlquerygenerator.visitor;

import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.COWLClassImpl;
import edu.neu.ece.sparqlquerygenerator.main.OntologyExtractor;

/**
 * An instance of this class processes assertion axioms that form the
 * definitions/descriptions of the specified OWL named individual, including
 * class assertion axiom.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLIndividualAxiomVisitor implements OWLAxiomVisitor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Specified OWL named individual.
	 */
	private final OWLNamedIndividual ind;

	/**
	 * Ontology extractor used for processing OWL axioms from input ontology.
	 */
	private final OntologyExtractor oe;

	/**
	 * Constructor
	 * 
	 * @param oe
	 *            Ontology extractor used for processing OWL axioms from input
	 *            ontology.
	 * @param ind
	 *            OWL named individual.
	 */
	public COWLIndividualAxiomVisitor(OntologyExtractor oe, OWLNamedIndividual ind) {
		this.ind = ind;
		this.oe = oe;
	}

	@Override
	public void doDefault(Object object) {
		// logger.warn("Unsupported assertion axiom: " + object);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		OWLClassExpression exp = axiom.getClassExpression();
		if (exp.isAnonymous())
			logger.warn("Class assertion axiom for " + ind.getIRI().getShortForm()
					+ " will be ignored since it contains anonymous class");
		COWLClassImpl cowlClassImpl = oe.getClassMap().get(exp.asOWLClass());
		cowlClassImpl.getNamedIndividuals().add(ind);
	}
}
