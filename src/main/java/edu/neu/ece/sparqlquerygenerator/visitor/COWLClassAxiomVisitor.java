package edu.neu.ece.sparqlquerygenerator.visitor;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.COWLClassImpl;
import edu.neu.ece.sparqlquerygenerator.main.OntologyExtractor;

/**
 * An instances of this class processes class expression axioms that form the
 * definitions/descriptions of the specified OWL class, which includes
 * SubClassOf axiom, EquivalentClasses axiom and DisjointClasses axiom.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class COWLClassAxiomVisitor implements OWLAxiomVisitor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Defines ontology extractor used for processing OWL axioms from input
	 * ontology.
	 */
	private final OntologyExtractor oe;

	/**
	 * Specified OWL class.
	 */
	private final OWLClass owlClass;

	/**
	 * Customization of the specified OWL class.
	 */
	private final COWLClassImpl cowlClassImpl;

	/**
	 * Constructor
	 * 
	 * @param oe
	 *            Ontology extractor used to process OWL axioms of the input
	 *            ontology.
	 * @param owlClass
	 *            Specified OWL class.
	 */
	public COWLClassAxiomVisitor(OntologyExtractor oe, OWLClass owlClass) {
		this.oe = oe;
		this.owlClass = owlClass;
		this.cowlClassImpl = this.oe.getCOWLClassImpl(this.owlClass);
	}

	@Override
	public void doDefault(Object object) {
		logger.warn("Unsupported class axiom: " + object);
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		if (axiom.isGCI()) {
			// TODO anonym subclass behaviour
			logger.info("Anonymous subclass: " + axiom);
			return;
		}

		// COWLClassImpl cowlClassImpl = oe.getCOWLClassImpl(owlClass);
		OWLClassExpression classExp = axiom.getSuperClass();
		if (classExp.isAnonymous()) {
			cowlClassImpl.getDirectAnonymousSuperClasses().add((OWLAnonymousClassExpression) classExp);
		} else {
			OWLClass superClass = classExp.asOWLClass();
			if (superClass.isOWLThing() || superClass.isOWLNothing())
				return;
			cowlClassImpl.getDirectSuperClasses().add(oe.getCOWLClassImpl(superClass));
			oe.getCOWLClassImpl(superClass).getDirectSubClasses().add(cowlClassImpl);
		}
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		for (OWLClassExpression exp : axiom.getClassExpressionsMinus(owlClass))
			cowlClassImpl.getEquivalentClasses().add(exp);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		for (OWLClassExpression exp : axiom.getClassExpressionsMinus(owlClass))
			cowlClassImpl.getDisjointClasses().add(exp);
	}
}
