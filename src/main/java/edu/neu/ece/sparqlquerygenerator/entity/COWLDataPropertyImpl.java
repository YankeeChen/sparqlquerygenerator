package edu.neu.ece.sparqlquerygenerator.entity;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataRange;

/**
 * This class defines customization of the OWL API class OWLDataPropertImpl.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-09-29
 */
public class COWLDataPropertyImpl extends COWLPropertyImpl {

	/**
	 * Data property range.
	 */
	private OWLDataRange range;

	/**
	 * Constructor
	 * 
	 * @param iri
	 *            Data property IRI.
	 */
	public COWLDataPropertyImpl(IRI iri) {
		super(iri);
	}

	/**
	 * Get data property range.
	 * 
	 * @return Data property range.
	 */
	public OWLDataRange getOWLDataRange() {
		return range;
	}

	/**
	 * Set data property range.
	 * 
	 * @param range
	 *            Data property range.
	 */
	public void setOWLDataRange(OWLDataRange range) {
		this.range = range;
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
		return relevantProperties;
	}
}
