package edu.neu.ece.sparqlquerygenerator.visitor;

import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.COWLObjectPropertyImpl;
import edu.neu.ece.sparqlquerygenerator.main.OntologyExtractor;

/**
 * An instance of this class processes object property axioms that form the
 * definitions/descriptions of the specified object property, including
 * SubObjectPropertyOf axiom, EquivalentObjectProperties axiom,
 * InverseObjectProperties axiom, FunctionalObjectProperty axiom and
 * SymmetricObjectProperty axiom.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLObjectPropertyAxiomVisitor extends COWLPropertyAxiomVisitor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Customization of the specified object property.
	 */
	private final COWLObjectPropertyImpl cowlObjectPropertyImpl;

	/**
	 * Constructor
	 * 
	 * @param oe
	 *            Ontology extractor used for extracting OWL axioms from input
	 *            ontology.
	 * @param owlObjectProperty
	 *            Target OWL object property.
	 */
	public COWLObjectPropertyAxiomVisitor(OntologyExtractor oe, OWLObjectProperty owlObjectProperty) {
		super(oe, owlObjectProperty);
		cowlObjectPropertyImpl = oe.getObjectPropertyMap().get(owlProperty);

	}

	@Override
	public void doDefault(Object object) {
		logger.warn("Unsupported object property axiom: " + object);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		OWLObjectPropertyExpression propertyExp = axiom.getSuperProperty();
		if (propertyExp.isAnonymous()) {
			// TODO anonym behaviour
			logger.info("Anonymous super object property:" + propertyExp);
			return;
		}
		OWLObjectProperty owlProperty = propertyExp.asOWLObjectProperty();
		cowlObjectPropertyImpl.getDirectSuperOWLProperties().add(oe.getObjectPropertyMap().get(owlProperty));
		oe.getObjectPropertyMap().get(owlProperty).getDirectSubOWLProperties().add(cowlObjectPropertyImpl);
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		for (OWLObjectPropertyExpression exp : axiom.getPropertiesMinus((OWLObjectPropertyExpression) owlProperty)) {
			if (exp.isAnonymous()) {
				// TODO anonymous behaviour
				logger.info("Anonymous equivalent object property: " + exp);
				continue;
			}
			cowlObjectPropertyImpl.getEquivalentProperties()
					.add(oe.getObjectPropertyMap().get(exp.asOWLObjectProperty()));
		}
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		for (OWLObjectPropertyExpression exp : axiom.getPropertiesMinus((OWLObjectPropertyExpression) owlProperty)) {
			if (exp.isAnonymous()) {
				// TODO anonymous behaviour
				logger.info("Anonymous disjoint object property: " + exp);
				continue;
			}
			cowlObjectPropertyImpl.getDirectDisjointProperties()
					.add(oe.getObjectPropertyMap().get(exp.asOWLObjectProperty()));
			oe.getObjectPropertyMap().get(exp.asOWLObjectProperty()).getDirectDisjointProperties()
					.add(cowlObjectPropertyImpl);
		}
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		for (OWLObjectPropertyExpression exp : axiom.getPropertiesMinus((OWLObjectPropertyExpression) owlProperty)) {
			if (exp.isAnonymous()) {
				// TODO anonymous behaviour
				logger.info("Anonymous inverse object property: " + exp);
				continue;
			}
			cowlObjectPropertyImpl.getInverseProperties().add(oe.getObjectPropertyMap().get(exp.asOWLObjectProperty()));
			oe.getObjectPropertyMap().get(exp.asOWLObjectProperty()).getInverseProperties().add(cowlObjectPropertyImpl);
		}
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		cowlObjectPropertyImpl.getPropertyAttributes().add(axiom.getAxiomType());
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		cowlObjectPropertyImpl.getPropertyAttributes().add(axiom.getAxiomType());
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		cowlObjectPropertyImpl.getPropertyAttributes().add(axiom.getAxiomType());
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		cowlObjectPropertyImpl.getPropertyAttributes().add(axiom.getAxiomType());
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		cowlObjectPropertyImpl.getPropertyAttributes().add(axiom.getAxiomType());
	}
}
