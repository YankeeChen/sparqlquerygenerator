package edu.neu.ece.sparqlquerygenerator.visitor;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.COWLDataPropertyImpl;
import edu.neu.ece.sparqlquerygenerator.main.OntologyExtractor;

/**
 * An instance of this class processes OWL data property axioms that form the
 * definitions/descriptions of the specified data property, including
 * SubPropertyOf axiom, EquivalentDataProperties axiom and
 * FunctionalDataProperty axiom.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLDataPropertyAxiomVisitor extends COWLPropertyAxiomVisitor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Customization of the specified OWL data property.
	 */
	private final COWLDataPropertyImpl cowlDataPropertyImpl;

	/**
	 * Constructor
	 * 
	 * @param oe
	 *            Ontology extractor used for processing OWL axioms from input
	 *            ontology.
	 * @param owlProperty
	 *            OWL data property
	 */
	public COWLDataPropertyAxiomVisitor(OntologyExtractor oe, OWLProperty owlProperty) {
		super(oe, owlProperty);
		cowlDataPropertyImpl = oe.getDataPropertyMap().get(owlProperty);
	}

	@Override
	public void doDefault(Object object) {
		logger.warn("Unsupported data property axiom: " + object);
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		OWLDataPropertyExpression propertyExp = axiom.getSuperProperty();
		if (propertyExp.isAnonymous()) {
			// TODO anonym behaviour
			logger.info("Anonymous super data property:" + propertyExp);
			return;
		}
		OWLDataProperty owlProperty = propertyExp.asOWLDataProperty();
		cowlDataPropertyImpl.getDirectSuperOWLProperties().add(oe.getDataPropertyMap().get(owlProperty));
		oe.getDataPropertyMap().get(owlProperty).getDirectSubOWLProperties().add(cowlDataPropertyImpl);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		for (OWLDataPropertyExpression exp : axiom.getPropertiesMinus((OWLDataPropertyExpression) owlProperty)) {
			if (exp.isAnonymous()) {
				// TODO anonymous behaviour
				logger.info("Anonymous equivalent data property: " + exp);
				continue;
			}
			cowlDataPropertyImpl.getEquivalentProperties().add(oe.getDataPropertyMap().get(exp.asOWLDataProperty()));
		}
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		for (OWLDataPropertyExpression exp : axiom.getPropertiesMinus((OWLDataPropertyExpression) owlProperty)) {
			if (exp.isAnonymous()) {
				// TODO anonymous behaviour
				logger.info("Anonymous disjoint data property: " + exp);
				continue;
			}
			cowlDataPropertyImpl.getDisjointProperties().add(oe.getDataPropertyMap().get(exp.asOWLDataProperty()));
		}
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		cowlDataPropertyImpl.getPropertyAttributes().add(axiom.getAxiomType());
	}
}
