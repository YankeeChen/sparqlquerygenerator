package edu.neu.ece.sparqlquerygenerator.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * This class defines customization of the OWL API class OWLClassImpl.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-28
 */
public class COWLClassImpl implements HasIRI {

	/**
	 * Class IRI.
	 */
	private IRI iri;

	/**
	 * Direct super classes of this object
	 */
	private Set<COWLClassImpl> directSuperClasses = new HashSet<>();

	/**
	 * Super classes (direct and inferred) of this object.
	 */
	private Set<COWLClassImpl> superClasses = new HashSet<>();

	/**
	 * Direct anonymous super class expressions of this object.
	 */
	private Set<OWLAnonymousClassExpression> directAnonymousSuperClasses = new HashSet<>();

	/**
	 * Anonymous super classes of this object, including direct anonymous super
	 * classes and anonymous super classes inherited from its super classes.
	 */
	private Set<OWLAnonymousClassExpression> anonymousSuperClasses = new HashSet<>();

	/**
	 * Direct subclasses of this object.
	 */
	private Set<COWLClassImpl> directSubClasses = new HashSet<>();

	/**
	 * Subclasses (direct and inferred) of this object.
	 */
	private Set<COWLClassImpl> subClasses = new HashSet<>();

	/**
	 * Equivalent class expressions (direct and inferred) of this object.
	 */
	private Set<OWLClassExpression> equivalentClasses = new HashSet<>();

	/**
	 * Disjoint class expressions (direct and inferred) of this object.
	 */
	private Set<OWLClassExpression> disjointClasses = new HashSet<>();

	/**
	 * Named classes (Equivalent classes (direct and inferred), subclasses (direct
	 * and inferred), super classes (direct and inferred) and disjoint classes
	 * (direct and inferred)) of this object and itself.
	 */
	private Set<COWLClassImpl> relevantNamedClasses = null;

	/**
	 * Anonymous super class expressions, anonymous equivalent class expressions and
	 * anonymous disjoint class expressions of this object.
	 */
	private Set<OWLAnonymousClassExpression> anonymousClassRestrictions = null;

	/**
	 * Container that stores key-value pairs, where object property is the key and
	 * the property range (class expression) is the value.
	 */
	private Map<OWLObjectProperty, OWLClassExpression> objectPropertyRangesPairs = new HashMap<>();

	/**
	 * Container that stores key-value pairs, where data property is the key and the
	 * property range (data range) is the value.
	 */
	private Map<OWLDataProperty, OWLDataRange> dataPropertyRangesPairs = new HashMap<>();

	/**
	 * Named individuals of class type represented by this object.
	 */
	private LinkedList<OWLNamedIndividual> individuals = new LinkedList<>();

	/**
	 * Detect whether this object is visited when generating SPARQL queries.
	 */
	private boolean isVisited = false;

	/**
	 * Variables that bind to individuals of type represented by this object.
	 */
	private LinkedList<Var> variables = new LinkedList<>();

	/**
	 * A counter that traces the index of next variable that binds to OWL
	 * individuals of class type represented by this object.
	 */
	private long nextVariableIndex = 0;

	/**
	 * Node representation of this object by Jena.
	 */
	private Node node = null;

	/**
	 * Constructor.
	 * 
	 * @param iri
	 *            class IRI.
	 */
	public COWLClassImpl(IRI iri) {
		this.iri = iri;
		node = NodeFactory.createURI(iri.getIRIString());
	}

	/**
	 * Get class IRI.
	 */
	@Override
	public IRI getIRI() {
		return iri;
	}

	/**
	 * Get direct super classes of this object.
	 * 
	 * @return Direct super classes.
	 */
	public Set<COWLClassImpl> getDirectSuperClasses() {
		return directSuperClasses;
	}

	/**
	 * Get super classes (direct and indirect) of this object.
	 * 
	 * @return Super classes (direct and inferred).
	 */
	public Set<COWLClassImpl> getSuperClasses() {
		return superClasses;
	}

	/**
	 * Get direct anonymous super class expressions of this object.
	 * 
	 * @return Direct anonymous super class expressions.
	 */
	public Set<OWLAnonymousClassExpression> getDirectAnonymousSuperClasses() {
		return directAnonymousSuperClasses;
	}

	/**
	 * Get anonymous super classes of this object, including direct anonymous super
	 * classes and anonymous super classes inherited from super classes.
	 * 
	 * @return Anonymous super classes.
	 */
	public Set<OWLAnonymousClassExpression> getAnonymousSuperClasses() {
		return anonymousSuperClasses;
	}

	/**
	 * Set anonymous super classes of this object.
	 * 
	 * @param set
	 *            Anonymous super classes.
	 */
	public void setAnonymousSuperClasses(Set<OWLAnonymousClassExpression> set) {
		anonymousSuperClasses = set;
	}

	/**
	 * Get direct subclasses of this object.
	 * 
	 * @return Direct subclasses.
	 */
	public Set<COWLClassImpl> getDirectSubClasses() {
		return directSubClasses;
	}

	/**
	 * Get subclasses of this object.
	 * 
	 * @return Subclasses (direct and inferred).
	 */
	public Set<COWLClassImpl> getSubClasses() {
		return subClasses;
	}

	/**
	 * Get equivalent class expressions of this object.
	 * 
	 * @return Equivalent class expressions.
	 */
	public Set<OWLClassExpression> getEquivalentClasses() {
		return equivalentClasses;
	}

	/**
	 * Get disjoint class expressions (direct and inferred) of this object.
	 * 
	 * @return Disjoint class expressions.
	 */
	public Set<OWLClassExpression> getDisjointClasses() {
		return disjointClasses;
	}

	/**
	 * Get named classes (equivalent classes (direct and inferred), subclasses
	 * (direct and inferred), super classes (direct and inferred) and disjoint
	 * classes (direct and inferred)) of this object and itself.
	 * 
	 * @param classMap
	 *            Container that stores key-value pairs, where OWL API interface
	 *            OWLClass is the key and the customized class COWLClassImpl is the
	 *            value.
	 * @return Relevant named classes.
	 */
	public Set<COWLClassImpl> getRelevantNamedClasses(Map<OWLClass, COWLClassImpl> classMap) {
		if (relevantNamedClasses != null)
			return relevantNamedClasses;
		relevantNamedClasses = new HashSet<>();
		relevantNamedClasses.add(this);
		relevantNamedClasses.addAll(subClasses);
		relevantNamedClasses.addAll(superClasses);
		for (OWLClassExpression eq : equivalentClasses)
			if (!eq.isAnonymous())
				relevantNamedClasses.add(classMap.get(eq.asOWLClass()));
		for (OWLClassExpression dis : disjointClasses)
			if (!dis.isAnonymous())
				relevantNamedClasses.add(classMap.get(dis.asOWLClass()));
		return relevantNamedClasses;
	}

	/**
	 * Get anonymous class restrictions, including anonymous super class
	 * expressions, anonymous equivalent class expressions and anonymous disjoint
	 * class expressions.
	 * 
	 * @return Anonymous class restrictions.
	 */
	public Set<OWLAnonymousClassExpression> getAnonymousClassRestrictions() {
		if (anonymousClassRestrictions != null)
			return anonymousClassRestrictions;
		anonymousClassRestrictions = new HashSet<>();
		anonymousClassRestrictions.addAll(anonymousSuperClasses);
		for (OWLClassExpression eq : equivalentClasses)
			if (eq.isAnonymous())
				anonymousClassRestrictions.add((OWLAnonymousClassExpression) eq);
		for (OWLClassExpression dis : disjointClasses)
			if (dis.isAnonymous())
				anonymousClassRestrictions.add((OWLAnonymousClassExpression) dis);
		return anonymousClassRestrictions;
	}

	/**
	 * Get the container that stores key-value pairs, where object property is the
	 * key and the property range (class expression) is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRangesPairs() {
		return objectPropertyRangesPairs;
	}

	/**
	 * Get the container that stores key-value pairs, where data property is the key
	 * and the property range (data range) is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLDataProperty, OWLDataRange> getDataPropertyRangesPairs() {
		return dataPropertyRangesPairs;
	}

	/**
	 * Get named individuals of type of this object.
	 * 
	 * @return Named individuals of type of this object.
	 */
	public LinkedList<OWLNamedIndividual> getNamedIndividuals() {
		return individuals;
	}

	/**
	 * Detect whether this object is visited.
	 * 
	 * @return Visit status of this object.
	 */
	public boolean isVisited() {
		return isVisited;
	}

	/**
	 * Set visit status of this object.
	 * 
	 * @param visited
	 *            Visit status of this object.
	 */
	public void setVisited(boolean visited) {
		isVisited = visited;
	}

	/**
	 * Get variables that bind to individuals of type represented by this object.
	 * 
	 * @return A list of variables.
	 */
	public LinkedList<Var> getVariables() {
		return variables;
	}

	/**
	 * Function used to coherently increase the counter that traces the index of
	 * next variable that binds to OWL individuals of class type represented by this
	 * object.
	 * 
	 * @return The index of next OWL named individual class type represented by this
	 *         object.
	 */
	public long getNextVariableIndex() {
		return nextVariableIndex++;
	}

	/**
	 * Get node representation of this object.
	 * 
	 * @return Node representation of this object.
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Get subclasses of this object and itself.
	 * 
	 * @return Subclasses of this object and itself.
	 */
	public Set<COWLClassImpl> getSubClassesAndItself() {
		Set<COWLClassImpl> subAndItSelf = new HashSet<>();
		subAndItSelf.addAll(subClasses);
		subAndItSelf.add(this);
		return subAndItSelf;
	}

	/**
	 * Add a key-value pair to the container, where object property is the key and
	 * the property range (class expression) is the value.
	 * 
	 * @param oop
	 *            The key (object property).
	 * @param classExp
	 *            The value (class expression).
	 */
	public void addAnObjectPropertyRangesPair(OWLObjectProperty oop, OWLClassExpression classExp) {
		objectPropertyRangesPairs.put(oop, classExp);
	}

	/**
	 * Add a key-value pair to the container, where data property is the key and the
	 * property range (data range) is the value.
	 * 
	 * @param odp
	 *            The key (data property).
	 * @param ran
	 *            The value (data range).
	 */
	public void addADataPropertyRangesPair(OWLDataProperty odp, OWLDataRange ran) {
		dataPropertyRangesPairs.put(odp, ran);
	}

	/**
	 * Delete all variables and reset next variable index.
	 * 
	 */
	public void cleanUpVariables() {
		variables.clear();
		nextVariableIndex = 0;
	}
}
