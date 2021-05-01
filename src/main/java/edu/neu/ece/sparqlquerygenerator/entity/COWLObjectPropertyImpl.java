package edu.neu.ece.sparqlquerygenerator.entity;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * This class defines customization of the OWL API class OWLObjectPropertImpl.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 */
public class COWLObjectPropertyImpl extends COWLPropertyImpl {

	/**
	 * Inverse properties of this object.
	 */
	private Set<COWLObjectPropertyImpl> inverseProperties = new HashSet<>();

	/**
	 * Constructor.
	 * 
	 * @param iri
	 *            Object property IRI.
	 */
	public COWLObjectPropertyImpl(IRI iri) {
		super(iri);
	}

	/**
	 * Get inverse properties of this object.
	 * 
	 * @return Inverse properties of this object.
	 */
	public Set<COWLObjectPropertyImpl> getInverseProperties() {
		return inverseProperties;
	}

	@Override
	public Set<COWLPropertyImpl> setRelevantProperties() {
		isVisited = true;
		relevantProperties = new HashSet<>();
		relevantProperties.add(this);
		for (COWLPropertyImpl sub : subOWLProperties)
			if (!sub.isVisited())
				relevantProperties.addAll(sub.getRelevantProperties());
		for (COWLPropertyImpl sup : superOWLProperties)
			if (!sup.isVisited())
				relevantProperties.addAll(sup.getRelevantProperties());
		for (COWLPropertyImpl eq : equivalentProperties)
			if (!eq.isVisited())
				relevantProperties.addAll(eq.getRelevantProperties());
		for (COWLPropertyImpl dis : disjointProperties)
			if (!dis.isVisited())
				relevantProperties.addAll(dis.getRelevantProperties());
		for (COWLPropertyImpl inv : inverseProperties)
			if (!inv.isVisited())
				relevantProperties.addAll(inv.getRelevantProperties());
		return relevantProperties;
	}

}
