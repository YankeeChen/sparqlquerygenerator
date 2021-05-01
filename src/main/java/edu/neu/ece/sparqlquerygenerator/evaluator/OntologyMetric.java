package edu.neu.ece.sparqlquerygenerator.evaluator;

/**
 * The instance of this class records the number of OWL class, data properties,
 * object properties and individuals of the input ontology.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public class OntologyMetric {

	/**
	 * The number of OWL class.
	 */
	private int namedClassCount = 0;

	/**
	 * The number of object properties.
	 */
	private int objectPropertyCount = 0;

	/**
	 * The number of data properties.
	 */
	private int dataPropertyCount = 0;

	/**
	 * The number of OWL named individuals.
	 */
	private int namedIndividualCount = 0;

	/**
	 * Get the number of OWL class.
	 * 
	 * @return The number of OWL class.
	 */
	public int getNamedClassCount() {
		return namedClassCount;
	}

	/**
	 * Get the number of object properties.
	 * 
	 * @return The number of object properties.
	 */
	public int getObjectPropertyCount() {
		return objectPropertyCount;
	}

	/**
	 * Get the number of data properties.
	 * 
	 * @return The number of data properties.
	 */
	public int getDatatypePropertyCount() {
		return dataPropertyCount;
	}

	/**
	 * Get the number of OWL named individuals.
	 * 
	 * @return The number of OWL named individuals.
	 */
	public int getNamedIndividualCount() {
		return namedIndividualCount;
	}

	/**
	 * Increase the number of OWL class by 1.
	 */
	public void increaseClassCountByOne() {
		namedClassCount++;
	}

	/**
	 * Increase the number of OWL object properties by 1.
	 */
	public void increaseObjectPropertyCountByOne() {
		objectPropertyCount++;
	}

	/**
	 * Increase the number of OWL data properties by 1.
	 */
	public void increaseDataPropertyCountByOne() {
		dataPropertyCount++;
	}

	/**
	 * Increase the number of OWL named individuals by 1.
	 */
	public void increaseNamedIndividualCountByOne() {
		namedIndividualCount++;
	}

	@Override
	public String toString() {
		return "Ontology metrics {" + "Class count = " + namedClassCount + ", Object property count = "
				+ objectPropertyCount + ", Data property count = " + dataPropertyCount + ", Named individual count = "
				+ namedIndividualCount + '}';
	}
}
