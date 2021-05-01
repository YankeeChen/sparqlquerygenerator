package edu.neu.ece.sparqlquerygenerator.visitor;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.*;
import edu.neu.ece.sparqlquerygenerator.main.OntologyExtractor;

/**
 * An instance of this class processes OWL entities of the input ontology,
 * including OWL class, OWL object property, OWL data property and OWL named
 * individual.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLEntityVisitor implements OWLObjectVisitor {
	// private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Defines ontology extractor used for processing OWL axioms from input
	 * ontology.
	 */
	private final OntologyExtractor oe;

	/**
	 * Constructor
	 * 
	 * @param oe
	 *            Ontology extractor used for processing OWL axioms from input
	 *            ontology.
	 */
	public COWLEntityVisitor(OntologyExtractor oe) {
		this.oe = oe;
	}

	@Override
	public void visit(OWLClass oc) {
		if (oc.isOWLThing() || oc.isOWLNothing())
			return;
		COWLClassImpl cocImpl;
		if (!oe.getClassMap().containsKey(oc)) {
			// logger.info("The extracted class IRI is:" + oc.getIRI().getIRIString());
			cocImpl = new COWLClassImpl(oc.getIRI());
			oe.addClass(oc, cocImpl);
			oe.getOntologyMetric().increaseClassCountByOne();
		}
	}

	@Override
	public void visit(OWLDataProperty dp) {
		if (dp.isOWLTopDataProperty() || dp.isOWLBottomDataProperty())
			return;
		COWLDataPropertyImpl codpImpl;
		if (!oe.getDataPropertyMap().containsKey(dp)) {
			// logger.info("The extracted data property IRI is:" +
			// dp.getIRI().getIRIString());
			codpImpl = new COWLDataPropertyImpl(dp.getIRI());
			oe.addDataProperty(dp, codpImpl);
			oe.getOntologyMetric().increaseDataPropertyCountByOne();
		}
	}

	@Override
	public void visit(OWLObjectProperty op) {
		if (op.isOWLTopObjectProperty() || op.isOWLBottomObjectProperty())
			return;
		COWLObjectPropertyImpl coopImpl;
		if (!oe.getObjectPropertyMap().containsKey(op)) {
			// logger.info("The extracted object property IRI is:" +
			// op.getIRI().getIRIString());
			coopImpl = new COWLObjectPropertyImpl(op.getIRI());
			oe.addObjectProperty(op, coopImpl);
			oe.getOntologyMetric().increaseObjectPropertyCountByOne();
		}
	}

	@Override
	public void visit(OWLNamedIndividual ind) {
		if (!oe.getExistingIndividuals().contains(ind)) {
			// logger.info("The extracted named individual IRI is:" +
			// ind.getIRI().getIRIString());
			oe.addAnIndividual(ind);
			oe.getOntologyMetric().increaseNamedIndividualCountByOne();
		}
	}

}
