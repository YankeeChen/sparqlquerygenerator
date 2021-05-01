package edu.neu.ece.sparqlquerygenerator.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.neu.ece.sparqlquerygenerator.entity.*;
import edu.neu.ece.sparqlquerygenerator.evaluator.OntologyMetric;
import edu.neu.ece.sparqlquerygenerator.visitor.COWLClassAxiomVisitor;
import edu.neu.ece.sparqlquerygenerator.visitor.COWLDataPropertyAxiomVisitor;
import edu.neu.ece.sparqlquerygenerator.visitor.COWLEntityVisitor;
import edu.neu.ece.sparqlquerygenerator.visitor.COWLIndividualAxiomVisitor;
import edu.neu.ece.sparqlquerygenerator.visitor.COWLObjectPropertyAxiomVisitor;

/**
 * As one of the most critical classes of the program, OntologyExtractor defines
 * the processes of various OWL axioms of the input ontology.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-07-02
 *
 */
public class OntologyExtractor {

	/**
	 * Logger class, used for generating log file and debugging info on console.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Container that stores key-value pairs, where OWL API interface OWLClass is
	 * the key and the customized class COWLClassImpl is the value.
	 */
	private Map<OWLClass, COWLClassImpl> classMap = new HashMap<>();

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 */
	private Map<OWLDataProperty, COWLDataPropertyImpl> dataPropertyMap = new HashMap<>();

	/**
	 * Container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 */
	private Map<OWLObjectProperty, COWLObjectPropertyImpl> objectPropertyMap = new HashMap<>();

	/**
	 * OWL named individuals of the input ontology.
	 */
	private Set<OWLNamedIndividual> existingIndividuals = new HashSet<>();

	/**
	 * Hold of input ontology.
	 */
	private OWLOntology ont;

	/**
	 * An OWL API build-in reasoner.
	 */
	private OWLReasoner reasoner;

	/**
	 * OWL metric that records the number of OWL named class, data properties,
	 * object properties and individuals.
	 */
	private OntologyMetric metric = new OntologyMetric();

	/**
	 * Constructor
	 * 
	 * @param ont
	 *            Hold of ontology.
	 * @param reasoner
	 *            OWL reasoner.
	 */
	public OntologyExtractor(OWLOntology ont, OWLReasoner reasoner) {
		this.ont = ont;
		this.reasoner = reasoner;
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLClass is the key and the customized class COWLClassImpl is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLClass, COWLClassImpl> getClassMap() {
		return Collections.unmodifiableMap(classMap);
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLDataProperty, COWLDataPropertyImpl> getDataPropertyMap() {
		return Collections.unmodifiableMap(dataPropertyMap);
	}

	/**
	 * Get the container that stores key-value pairs, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 * 
	 * @return The container.
	 */
	public Map<OWLObjectProperty, COWLObjectPropertyImpl> getObjectPropertyMap() {
		return Collections.unmodifiableMap(objectPropertyMap);
	}

	/**
	 * Get OWL named individuals of the input ontology.
	 * 
	 * @return OWL named individuals.
	 */
	public Set<OWLNamedIndividual> getExistingIndividuals() {
		return Collections.unmodifiableSet(existingIndividuals);
	}

	/**
	 * Get the hold of input ontology.
	 * 
	 * @return The hold of input ontology.
	 */
	public OWLOntology getOWLOntology() {
		return ont;
	}

	/**
	 * Get the OWL API build-in reasoner.
	 * 
	 * @return The reasoner.
	 */
	public OWLReasoner getOWLReasoner() {
		return reasoner;
	}

	/**
	 * Get OWL metric that records the number of OWL class, data properties, object
	 * properties and individuals of the input ontology.
	 * 
	 * @return OWL metric.
	 */
	public OntologyMetric getOntologyMetric() {
		return metric;
	}

	/**
	 * Get customization of the specified OWL class.
	 * 
	 * @param owlClass
	 *            Specified OWL class.
	 * @return Customization of the specified OWL class.
	 */
	public COWLClassImpl getCOWLClassImpl(OWLClass owlClass) {
		return classMap.get(owlClass);
	}

	/**
	 * This function defines control flow of ontology processing, including OWL
	 * entity processing (preparsing), OWL axiom processing (parsing) and new
	 * knowledge reasoning (postparsing).
	 */
	public void extract() {
		preParsing();
		parsing();
		postParsing();
		// logger.info(toString());
	}

	/**
	 * This function defines OWL entity processing (preparsing).
	 */
	private void preParsing() {
		logger.info("Begin extracting OWL entities...");
		OWLOntologyWalker walker = new OWLOntologyWalker(ont.importsClosure().collect(Collectors.toSet()));
		// Here we use visitor design pattern to visit ontology entities through
		// COWLEntityVistor.
		COWLEntityVisitor oev = new COWLEntityVisitor(this);
		walker.walkStructure(oev);
		logger.info(metric.toString());
		logger.info("Extract OWL entities successfully!");
	}

	/**
	 * This function defines control flow of ontology axiom processing, including
	 * class axiom processing, object property axiom processing, data property axiom
	 * processing and individual axiom processing.
	 */
	private void parsing() {
		processClassAxioms();
		processObjectPropertyAxioms();
		processDataPropertyAxioms();
		processIndividualAxioms();
	}

	/**
	 * This function defines class axiom processing.
	 */
	private void processClassAxioms() {
		logger.info("Begin extracting OWL class axioms...");
		for (OWLClass owlClass : ont.classesInSignature(Imports.INCLUDED).collect(Collectors.toSet())) {
			// Here we use visitor design pattern to visit OWL class axioms of the specified
			// OWL name class through COWLClassAxiomVisitor.
			COWLClassAxiomVisitor visitor = new COWLClassAxiomVisitor(this, owlClass);
			for (OWLClassAxiom owlClassAxiom : ont.axioms(owlClass, Imports.INCLUDED).collect(Collectors.toSet())) {
				owlClassAxiom.accept(visitor);
			}
		}
		logger.info("Extract OWL class axioms successfully!");
	}

	/**
	 * This function defines object property axiom processing.
	 */
	private void processObjectPropertyAxioms() {
		logger.info("Begin extracting object property axioms...");
		for (OWLObjectProperty owlObjectProperty : ont.objectPropertiesInSignature(Imports.INCLUDED)
				.collect(Collectors.toSet())) {
			// logger.info("The IRI of the object property is " +
			// owlObjectProperty.getIRI().getIRIString());
			// COWLObjectPropertyImpl cowlObjectPropertyImpl =
			// getObjectPropertyMap().get(owlObjectProperty);
			// Here we use visitor design pattern to visit OWL object property axioms of the
			// specified OWL object property through COWLObjectPropertyAxiomVisitor.
			COWLObjectPropertyAxiomVisitor cowlObjectPropertyAxiomVisitor = new COWLObjectPropertyAxiomVisitor(this,
					owlObjectProperty);
			Set<COWLClassImpl> cowlDomSet = new HashSet<>();
			// Set<COWLClassImpl> cowlRanSet = new HashSet<>();
			OWLClassExpression range = null;
			for (OWLObjectPropertyAxiom owlObjectPropertyAxiom : ont.axioms(owlObjectProperty, Imports.INCLUDED)
					.collect(Collectors.toSet())) {
				if (owlObjectPropertyAxiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
					// Set<OWLClass> domSet =
					// reasoner.objectPropertyDomains(owlObjectProperty).collect(Collectors.toSet());
					Set<OWLClass> domSet = owlObjectPropertyAxiom.classesInSignature().collect(Collectors.toSet());
					for (OWLClass oc : domSet) {
						COWLClassImpl cowlClass = getClassMap().get(oc);
						if (cowlClass == null) {
							logger.error("An object property " + owlObjectProperty.getIRI().getIRIString()
									+ " has an invalid domain: " + oc.getIRI().getIRIString());
							continue;
						}
						// logger.info("\t\tOne of the domain is " + oc.getIRI().getIRIString());
						cowlDomSet.add(cowlClass);
					}
				} else if (owlObjectPropertyAxiom.isOfType(AxiomType.OBJECT_PROPERTY_RANGE)) {
					// Set<OWLClass> ranSet =
					// reasoner.objectPropertyRanges(owlObjectProperty).collect(Collectors.toSet());
					/*
					 * Set<OWLClass> ranSet =
					 * owlObjectPropertyAxiom.classesInSignature().collect(Collectors.toSet());
					 * for(OWLClass oc : ranSet) { COWLClassImpl cowlClass = getClassMap().get(oc);
					 * if(cowlClass == null) { logger.error("An object property " +
					 * owlObjectProperty.getIRI().getIRIString() + " has an invalid range: " +
					 * oc.getIRI().getIRIString()); continue; }
					 * //logger.info("\t\tOne of the range is " + oc.getIRI().getIRIString());
					 * cowlRanSet.add(cowlClass); }
					 */
					range = ((OWLObjectPropertyRangeAxiom) owlObjectPropertyAxiom).getRange();
				} else
					owlObjectPropertyAxiom.accept(cowlObjectPropertyAxiomVisitor);
			}
			if (cowlDomSet.isEmpty() || range == null) {
				logger.warn("An object property " + owlObjectProperty.getIRI().getIRIString()
						+ " has no domains or ranges and will be ignored");
				continue;
			}
			for (COWLClassImpl ds : cowlDomSet)
				ds.addAnObjectPropertyRangesPair(owlObjectProperty, range);
		}
		logger.info("Extract object property axioms successfully!");
	}

	/**
	 * This function defines data property axiom processing.
	 */
	private void processDataPropertyAxioms() {
		logger.info("Begin extracting data property axioms...");
		for (OWLDataProperty owlDataProperty : ont.dataPropertiesInSignature(Imports.INCLUDED)
				.collect(Collectors.toSet())) {
			// logger.info("The IRI of the data property is " +
			// owlDataProperty.getIRI().getIRIString());
			// COWLDataPropertyImpl cowlDataPropertyImpl =
			// getDataPropertyMap().get(owlDataProperty);
			// Here we use visitor design pattern to visit OWL data property axioms of the
			// specified OWL data property through COWLDataPropertyAxiomVisitor.
			COWLDataPropertyAxiomVisitor cowlDataPropertyAxiomVisitor = new COWLDataPropertyAxiomVisitor(this,
					owlDataProperty);
			Set<COWLClassImpl> cowlDomSet = new HashSet<>();
			OWLDataRange ran = null;
			for (OWLDataPropertyAxiom owlDataPropertyAxiom : ont.axioms(owlDataProperty, Imports.INCLUDED)
					.collect(Collectors.toSet())) {
				if (owlDataPropertyAxiom.isOfType(AxiomType.DATA_PROPERTY_DOMAIN)) {
					Set<OWLClass> domSet = owlDataPropertyAxiom.classesInSignature().collect(Collectors.toSet());
					// Set<OWLClass> domSet =
					// reasoner.dataPropertyDomains(owlDataProperty).collect(Collectors.toSet());
					for (OWLClass oc : domSet) {
						COWLClassImpl cowlClass = getClassMap().get(oc);
						if (cowlClass == null) {
							logger.error("A data property " + owlDataProperty.getIRI().getIRIString()
									+ " has an invalid domain: " + oc.getIRI().getIRIString());
							continue;
						}
						// logger.info("\t\tOne of the domain is " + oc.getIRI().getIRIString());
						cowlDomSet.add(cowlClass);
					}
				} else if (owlDataPropertyAxiom.isOfType(AxiomType.DATA_PROPERTY_RANGE)) {
					ran = ((OWLDataPropertyRangeAxiom) owlDataPropertyAxiom).getRange();
					dataPropertyMap.get(owlDataProperty).setOWLDataRange(ran);
					// logger.info("\t\tThe range is " + ran.toString());
				} else
					owlDataPropertyAxiom.accept(cowlDataPropertyAxiomVisitor);
			}
			if (cowlDomSet.isEmpty() || ran == null) {
				logger.warn("An data property " + owlDataProperty.getIRI().getIRIString()
						+ " has no domains or ranges and will be ignored");
				continue;
			}
			for (COWLClassImpl ds : cowlDomSet)
				ds.addADataPropertyRangesPair(owlDataProperty, ran);
		}
		logger.info("Extract data property axioms successfully!");
	}

	/**
	 * This function defines individual axiom processing.
	 */
	private void processIndividualAxioms() {
		logger.info("Begin extracting individual axioms...");
		for (OWLNamedIndividual ind : existingIndividuals) {
			// Here we use visitor design pattern to visit OWL individual axioms of the
			// specified OWL named individual through COWLIndividualAxiomVisitor.
			COWLIndividualAxiomVisitor cowlIndividualAxiomVisitor = new COWLIndividualAxiomVisitor(this, ind);
			for (OWLIndividualAxiom owlIndividualAxiom : ont.axioms(ind, Imports.INCLUDED).collect(Collectors.toSet()))
				owlIndividualAxiom.accept(cowlIndividualAxiomVisitor);
		}
		logger.info("Extract individual axioms successfully!");
	}

	/**
	 * This function defines new knowledge reasoning process.
	 */
	private void postParsing() {
		logger.info("Begin extracting implicit knowledge...");
		for (Entry<OWLClass, COWLClassImpl> classEntry : classMap.entrySet()) {
			OWLClass owlClass = classEntry.getKey();
			COWLClassImpl cowlClassImpl = classEntry.getValue();
			// Get subclasses excluding owl:Nothing of each class
			for (OWLClass subClass : reasoner.subClasses(owlClass).collect(Collectors.toSet())) {
				if (!subClass.isOWLNothing())
					cowlClassImpl.getSubClasses().add(classMap.get(subClass));
			}
			// Get superclasses excluding owl:Thing of each class
			for (OWLClass superClass : reasoner.superClasses(owlClass).collect(Collectors.toSet())) {
				if (!superClass.isOWLThing())
					cowlClassImpl.getSuperClasses().add(classMap.get(superClass));
			}
			// Get equivalent classes of each class
			for (OWLClass equivalentClass : reasoner.equivalentClasses(owlClass).collect(Collectors.toSet())) {
				if (!owlClass.equals(equivalentClass))
					cowlClassImpl.getEquivalentClasses().add(equivalentClass);
			}
			// Get disjoint classes of each class
			for (OWLClass disjointClass : reasoner.disjointClasses(owlClass).collect(Collectors.toSet())) {
				if (!disjointClass.equals(owlClass))
					cowlClassImpl.getDisjointClasses().add(disjointClass);
			}
			// Get individuals of each class
			for (OWLNamedIndividual ind : reasoner.instances(owlClass).collect(Collectors.toSet())) {
				LinkedList<OWLNamedIndividual> individuals = cowlClassImpl.getNamedIndividuals();
				if (!individuals.contains(ind))
					individuals.add(ind);
			}

			/*
			 * Set<OWLAnonymousClassExpression> anonyClassExpSet = new
			 * HashSet<>(cowlClassImpl.getAnonymousSuperClasses().size() +
			 * cowlClassImpl.getSuperAnonymousSuperClasses().size());
			 * anonyClassExpSet.addAll(cowlClassImpl.getAnonymousSuperClasses());
			 * anonyClassExpSet.addAll(cowlClassImpl.getSuperAnonymousSuperClasses());
			 * cowlClassImpl.setAnonymousSuperClasses(anonyClassExpSet);
			 */
		}

		for (Entry<OWLDataProperty, COWLDataPropertyImpl> propertyEntry : dataPropertyMap.entrySet()) {
			OWLDataProperty owlDataProperty = propertyEntry.getKey();
			COWLDataPropertyImpl cowlDataPropertyImpl = propertyEntry.getValue();
			// Get subdataproperties of each data property
			for (OWLDataProperty subProperty : reasoner.subDataProperties(owlDataProperty)
					.collect(Collectors.toSet())) {
				if (!subProperty.isOWLBottomDataProperty())
					cowlDataPropertyImpl.getSubOWLProperties().add(dataPropertyMap.get(subProperty));
			}
			// Get superdataproperties of each data property
			for (OWLDataProperty superProperty : reasoner.superDataProperties(owlDataProperty)
					.collect(Collectors.toSet())) {
				if (!superProperty.isOWLTopDataProperty())
					cowlDataPropertyImpl.getSuperOWLProperties().add(dataPropertyMap.get(superProperty));
			}
			// Get equivalent properties of each data property
			for (OWLDataProperty equivalentProperty : reasoner.equivalentDataProperties(owlDataProperty)
					.collect(Collectors.toSet())) {
				if (!owlDataProperty.equals(equivalentProperty))
					cowlDataPropertyImpl.getEquivalentProperties().add(dataPropertyMap.get(equivalentProperty));
			}

			// Get disjoint properties of each data property
			/*
			 * for (OWLDataProperty disjointProperty :
			 * reasoner.disjointDataProperties(owlDataProperty)
			 * .collect(Collectors.toSet()))
			 * cowlDataPropertyImpl.getDisjointProperties().add(dataPropertyMap.get(
			 * disjointProperty));
			 */
		}

		for (Entry<OWLObjectProperty, COWLObjectPropertyImpl> propertyEntry : objectPropertyMap.entrySet()) {
			OWLObjectProperty owlObjectProperty = propertyEntry.getKey();
			COWLObjectPropertyImpl cowlObjectPropertyImpl = propertyEntry.getValue();
			// Get subobjectproperties of each object property
			// logger.info("Subproperties (direct and inferred) of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			for (OWLObjectPropertyExpression subProperty : reasoner.subObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (subProperty instanceof OWLObjectProperty && !subProperty.isOWLBottomObjectProperty())
					// logger.info("\t" + subProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getSubOWLProperties().add(objectPropertyMap.get(subProperty));
			}

			// Get superobjectproperties of each object property
			// logger.info("Superproperties (direct and inferred) of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			for (OWLObjectPropertyExpression superProperty : reasoner.superObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (superProperty instanceof OWLObjectProperty && !superProperty.isOWLTopObjectProperty())
					// logger.info("\t" + superProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getSuperOWLProperties().add(objectPropertyMap.get(superProperty));
			}
			// Get equivalent properties of each object property
			// logger.info("Equivalalent properties of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			for (OWLObjectPropertyExpression equivalentProperty : reasoner.equivalentObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (equivalentProperty instanceof OWLObjectProperty && !owlObjectProperty.equals(equivalentProperty))
					// logger.info("\t" +
					// equivalentProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getEquivalentProperties().add(objectPropertyMap.get(equivalentProperty));
			}
			// Get disjoint properties of each object property
			// logger.info("Disjoint properties of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");
			/*
			 * for (OWLObjectPropertyExpression disjointProperty :
			 * reasoner.disjointObjectProperties(owlObjectProperty)
			 * .collect(Collectors.toSet())) { if (disjointProperty instanceof
			 * OWLObjectProperty)
			 * cowlObjectPropertyImpl.getDisjointProperties().add(objectPropertyMap.get(
			 * disjointProperty)); }
			 */

			// Get inverse properties of each object property
			// logger.info("Inverse properties of " +
			// owlObjectProperty.getIRI().getShortForm() + " are shown as follows:");

			for (OWLObjectPropertyExpression inverseProperty : reasoner.inverseObjectProperties(owlObjectProperty)
					.collect(Collectors.toSet())) {
				if (inverseProperty instanceof OWLObjectProperty)
					// logger.info("\t" +
					// inverseProperty.getNamedProperty().getIRI().getIRIString());
					cowlObjectPropertyImpl.getInverseProperties().add(objectPropertyMap.get(inverseProperty));
			}
		}

		for (COWLClassImpl cowlClassImpl : classMap.values())
			recursiveExtractAnonymousSuperClasses(cowlClassImpl);

		for (COWLDataPropertyImpl propImpl : dataPropertyMap.values()) {
			recursiveExtractImplicitDisjointProperties(propImpl);
			processEquivalentProperties(propImpl);
		}

		for (COWLDataPropertyImpl outerPropImpl : dataPropertyMap.values()) {
			outerPropImpl.setRelevantProperties();
			for (COWLDataPropertyImpl innerPropImpl : dataPropertyMap.values())
				innerPropImpl.setVisited(false);
		}

		for (COWLObjectPropertyImpl propImpl : objectPropertyMap.values()) {
			recursiveExtractImplicitDisjointProperties(propImpl);
			processEquivalentProperties(propImpl);
		}

		for (COWLObjectPropertyImpl outerPropImpl : objectPropertyMap.values()) {
			outerPropImpl.setRelevantProperties();
			for (COWLObjectPropertyImpl innerPropImpl : objectPropertyMap.values())
				innerPropImpl.setVisited(false);
		}

		for (COWLClassImpl cowlClassImpl : classMap.values())
			cowlClassImpl.setVisited(false);

		logger.info("Extract implicit knowledge successfully!");
		// logger.info("\n" + toString());
	}

	/**
	 * This function extracts anonymous super classes of the OWL class as input
	 * argument. It retrieves anonymous super classes of each of its direct named
	 * super classes recursively, and then merges them in the output.
	 * 
	 * @param owlClassImpl
	 *            OWL class.
	 */
	private void recursiveExtractAnonymousSuperClasses(COWLClassImpl owlClassImpl) {
		if (owlClassImpl.isVisited())
			return;

		Set<OWLAnonymousClassExpression> superAnonymousSuperClasses = new HashSet<>();
		for (COWLClassImpl superClass : owlClassImpl.getDirectSuperClasses()) {
			if (!superClass.isVisited())
				recursiveExtractAnonymousSuperClasses(superClass);
			// owlClassImpl.getAnonymousSuperClasses().addAll(superClass.getAnonymousSuperClasses());
			superAnonymousSuperClasses.addAll(superClass.getAnonymousSuperClasses());
		}
		// owlClassImpl.getAnonymousSuperClasses().addAll(owlClassImpl.getDirectAnonymousSuperClasses());
		Set<OWLAnonymousClassExpression> anonymousSuperClasses = new HashSet<>();
		anonymousSuperClasses.addAll(superAnonymousSuperClasses);
		anonymousSuperClasses.addAll(owlClassImpl.getDirectAnonymousSuperClasses());

		owlClassImpl.getAnonymousSuperClasses().addAll(anonymousSuperClasses);
		owlClassImpl.setVisited(true);
	}

	/**
	 * This function retrieves inferred disjoint/inverse properties of a property
	 * based on its equivalent properties.
	 * 
	 * @param propImpl
	 *            OWL property.
	 */
	private void processEquivalentProperties(COWLPropertyImpl propImpl) {
		if (!propImpl.getEquivalentProperties().isEmpty()) {
			if (!propImpl.getDisjointProperties().isEmpty()) {
				Set<COWLPropertyImpl> disjointProperties = new HashSet<>();
				disjointProperties.addAll(propImpl.getDisjointProperties());
				for (COWLPropertyImpl propImpl1 : propImpl.getEquivalentProperties())
					disjointProperties.addAll(propImpl1.getDisjointProperties());
				propImpl.setDisjointProperties(disjointProperties);
				for (COWLPropertyImpl propImpl2 : propImpl.getEquivalentProperties())
					propImpl2.setDisjointProperties(disjointProperties);
			}
			if (propImpl instanceof COWLObjectPropertyImpl
					&& !((COWLObjectPropertyImpl) propImpl).getInverseProperties().isEmpty()) {
				Set<COWLPropertyImpl> inverseProperties = new HashSet<>();
				inverseProperties.addAll(((COWLObjectPropertyImpl) propImpl).getInverseProperties());
				for (COWLPropertyImpl propImpl1 : propImpl.getEquivalentProperties())
					inverseProperties.addAll(((COWLObjectPropertyImpl) propImpl1).getInverseProperties());
				propImpl.setDisjointProperties(inverseProperties);
				for (COWLPropertyImpl propImpl2 : propImpl.getEquivalentProperties())
					propImpl2.setDisjointProperties(inverseProperties);
			}
		}
	}

	/**
	 * This function recursively extracts disjoint properties from direct super
	 * properties of the specified OWL property. The extracted disjoint properties
	 * are added into the specified OWL properties as its inferred disjoint
	 * properties.
	 * 
	 * @param property
	 *            OWL property.
	 */
	private void recursiveExtractImplicitDisjointProperties(COWLPropertyImpl property) {
		if (property.isVisited())
			return;

		for (COWLPropertyImpl superProp : property.getDirectSuperOWLProperties()) {
			if (!superProp.isVisited())
				recursiveExtractImplicitDisjointProperties(superProp);
			property.getDisjointProperties().addAll(superProp.getDisjointProperties());
		}

		for (COWLPropertyImpl prop : property.getDirectDisjointProperties()) {
			property.getDisjointProperties().add(prop);
			for (COWLPropertyImpl sub : prop.getSubOWLProperties()) {
				property.getDisjointProperties().add(sub);
			}
		}
		property.setVisited(true);
	}

	/**
	 * Add a key-value pair to the container, where OWL API interface OWLClass is
	 * the key and the customized class COWLClassImpl is the value.
	 * 
	 * @param oc
	 *            OWL class.
	 * @param cocImpl
	 *            Customized OWL class.
	 */
	public void addClass(OWLClass oc, COWLClassImpl cocImpl) {
		classMap.put(oc, cocImpl);
	}

	/**
	 * Add a key-value pair to the container, where OWL API interface
	 * OWLObjectProperty is the key and the customized class COWLObjectPropertyImpl
	 * is the value.
	 * 
	 * @param oop
	 *            OWL object property.
	 * @param coopImpl
	 *            Customized object property.
	 */
	public void addObjectProperty(OWLObjectProperty oop, COWLObjectPropertyImpl coopImpl) {
		objectPropertyMap.put(oop, coopImpl);
	}

	/**
	 * Add a key-value pair to the container, where OWL API interface
	 * OWLDataProperty is the key and the customized class COWLDataPropertyImpl is
	 * the value.
	 * 
	 * @param odp
	 *            OWL data property.
	 * @param codpImpl
	 *            Customized data property.
	 */
	public void addDataProperty(OWLDataProperty odp, COWLDataPropertyImpl codpImpl) {
		dataPropertyMap.put(odp, codpImpl);
	}

	/**
	 * Add an OWL individual into the collection of OWL named individuals of the
	 * input ontology.
	 * 
	 * @param ind
	 *            OWL individual.
	 */
	public void addAnIndividual(OWLNamedIndividual ind) {
		existingIndividuals.add(ind);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Summary of the extracted ontology:\n");

		for (COWLClassImpl owlClassImpl : classMap.values()) {
			sb.append("A class IRI is : <" + owlClassImpl.getIRI().getIRIString() + ">\n");
			Set<COWLClassImpl> directSubClasses = owlClassImpl.getDirectSubClasses();
			if (!directSubClasses.isEmpty()) {
				sb.append("\t Direct subclasses of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : directSubClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<COWLClassImpl> subClasses = owlClassImpl.getSubClasses();
			if (!subClasses.isEmpty()) {
				sb.append("\t Subclasses (direct and inferred) of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : subClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<COWLClassImpl> directSuperClasses = owlClassImpl.getDirectSuperClasses();
			if (!directSuperClasses.isEmpty()) {
				sb.append("\t Direct superclasses of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : directSuperClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<COWLClassImpl> superClasses = owlClassImpl.getSuperClasses();
			if (!superClasses.isEmpty()) {
				sb.append(
						"\t Superclasses (direct and inferred) of the class in IRI short form are shown as follows:\n");
				for (COWLClassImpl ocImpl : superClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<OWLAnonymousClassExpression> anonymousSuperClasses = owlClassImpl.getAnonymousSuperClasses();
			if (!anonymousSuperClasses.isEmpty()) {
				sb.append("\t Anonymous superclasses (directed and inferred) of the class are shown as follows:\n");
				for (OWLAnonymousClassExpression acImpl : anonymousSuperClasses)
					sb.append("\t\t" + acImpl.toString() + "\n");
			}

			Set<OWLAnonymousClassExpression> directAnonymousSuperClasses = owlClassImpl
					.getDirectAnonymousSuperClasses();
			if (!directAnonymousSuperClasses.isEmpty()) {
				sb.append("\t Direct anonymous superclasses of the class are shown as follows:\n");
				for (OWLAnonymousClassExpression acImpl : directAnonymousSuperClasses)
					sb.append("\t\t" + acImpl.toString() + "\n");
			}

			Set<OWLClassExpression> equivalentClasses = owlClassImpl.getEquivalentClasses();
			if (!equivalentClasses.isEmpty()) {
				sb.append("\t Equivalent classes (direct and inferred) of the class are shown as follows:\n");
				for (OWLClassExpression exp : equivalentClasses)
					sb.append("\t\t" + exp.toString() + "\n");
			}

			Set<OWLClassExpression> disjointClasses = owlClassImpl.getDisjointClasses();
			if (!disjointClasses.isEmpty()) {
				sb.append("\t Disjoint classes (direct and inferred) of the class are shown as follows:\n");
				for (OWLClassExpression exp : disjointClasses)
					sb.append("\t\t" + exp.toString() + "\n");
			}

			Set<COWLClassImpl> relevantNamedClasses = owlClassImpl.getRelevantNamedClasses(classMap);
			if (!relevantNamedClasses.isEmpty()) {
				sb.append(
						"\t Relevant named classes (equivalent classes (direct and inferred), subclasses (direct and inferred), superclasses (direct and inferred)) and disjoint classes (direct and inferred) of the class and itself are shown as follows:\n");
				for (COWLClassImpl ocImpl : relevantNamedClasses)
					sb.append("\t\t" + ocImpl.getIRI().getShortForm() + "\n");
			}

			Set<OWLAnonymousClassExpression> anonymousClassRestrictions = owlClassImpl.getAnonymousClassRestrictions();
			if (!anonymousClassRestrictions.isEmpty()) {
				sb.append(
						"\t Anonymous class restrictions (anonymous superclass expressions (direct and inferred), anonymous equivalent class expressions (direct and inferred)) and disjoint class expressions (direct and inferred) of the class are shown as follows:\n");
				for (OWLAnonymousClassExpression acImpl : anonymousClassRestrictions)
					sb.append("\t\t" + acImpl.toString() + "\n");
			}

			Set<Entry<OWLObjectProperty, OWLClassExpression>> objectPropertyEntrySet = owlClassImpl
					.getObjectPropertyRangesPairs().entrySet();
			if (!objectPropertyEntrySet.isEmpty()) {
				sb.append("\t Object properties of the class are shown as follows:\n");
				for (Entry<OWLObjectProperty, OWLClassExpression> ope : objectPropertyEntrySet) {
					sb.append(
							"\t\t Object property in IRI short form : " + ope.getKey().getIRI().getShortForm() + "\n");
					sb.append("\t\t\t Property value is : " + ope.getValue().toString() + "\n");
				}
			}

			Set<Entry<OWLDataProperty, OWLDataRange>> dataPropertyEntrySet = owlClassImpl.getDataPropertyRangesPairs()
					.entrySet();
			if (!dataPropertyEntrySet.isEmpty()) {
				sb.append("\t Data properties of the class are shown as follows:\n");
				for (Entry<OWLDataProperty, OWLDataRange> dpe : dataPropertyEntrySet) {
					sb.append("\t\t Data property in IRI short form : " + dpe.getKey().getIRI().getShortForm() + "\n");
					sb.append("\t\t\t Property value is : " + dpe.getValue().toString() + "\n");
				}
			}

			LinkedList<OWLNamedIndividual> individuals = owlClassImpl.getNamedIndividuals();
			if (!individuals.isEmpty()) {
				sb.append(
						"\t Named individuals asserted or inferred to be of the type of the class in IRI short form are shown as follows:\n");
				for (OWLNamedIndividual ind : individuals) {
					sb.append("\t\t" + ind.getIRI().getShortForm() + "\n");
				}
			}

		}

		sb.append("\n=====================================================\n\n");
		for (COWLObjectPropertyImpl cowlObjectPropertyImpl : getObjectPropertyMap().values()) {
			sb.append("An object property in IRI short form is : " + cowlObjectPropertyImpl.getIRI().getShortForm()
					+ "\n");
			Set<COWLPropertyImpl> directSubObjectProperties = cowlObjectPropertyImpl.getDirectSubOWLProperties();
			if (!directSubObjectProperties.isEmpty()) {
				sb.append("\t Direct subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : directSubObjectProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> subObjectProperties = cowlObjectPropertyImpl.getSubOWLProperties();
			if (!subObjectProperties.isEmpty()) {
				sb.append("\t Subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : subObjectProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directSuperObjectProperties = cowlObjectPropertyImpl.getDirectSuperOWLProperties();
			if (!directSuperObjectProperties.isEmpty()) {
				sb.append("\t Direct superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : directSuperObjectProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> superObjectProperties = cowlObjectPropertyImpl.getSuperOWLProperties();
			if (!superObjectProperties.isEmpty()) {
				sb.append("\t Superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : superObjectProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> equivalentObjectProperties = cowlObjectPropertyImpl.getEquivalentProperties();
			if (!equivalentObjectProperties.isEmpty()) {
				sb.append(
						"\t Equivalent properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl equivalentProperty : equivalentObjectProperties)
					sb.append("\t\t" + equivalentProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> disjointObjectProperties = cowlObjectPropertyImpl.getDisjointProperties();
			if (!disjointObjectProperties.isEmpty()) {
				sb.append(
						"\t Disjoint properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : disjointObjectProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directDisjointObjectProperties = cowlObjectPropertyImpl.getDirectDisjointProperties();
			if (!directDisjointObjectProperties.isEmpty()) {
				sb.append("\t Direct disjoint properties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : directDisjointObjectProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> relevantObjectProperties = cowlObjectPropertyImpl.getRelevantProperties();
			if (!relevantObjectProperties.isEmpty()) {
				sb.append("\t Relevant object properties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl relatedProperty : relevantObjectProperties)
					sb.append("\t\t" + relatedProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLObjectPropertyImpl> inverseProperties = cowlObjectPropertyImpl.getInverseProperties();
			if (!inverseProperties.isEmpty()) {
				sb.append(
						"\t Inverse properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLObjectPropertyImpl inverseProperty : inverseProperties)
					sb.append("\t\t" + inverseProperty.getIRI().getShortForm() + "\n");
			}

			Set<AxiomType<? extends OWLAxiom>> propertyAttributes = cowlObjectPropertyImpl.getPropertyAttributes();
			if (!propertyAttributes.isEmpty()) {
				sb.append("\t Characteristics of the property are shown as follows:\n");
				for (AxiomType<? extends OWLAxiom> axiomType : propertyAttributes)
					sb.append("\t\t" + axiomType.getName() + "\n");
			}
		}

		sb.append("\n=====================================================\n\n");
		for (COWLDataPropertyImpl cowlDataPropertyImpl : getDataPropertyMap().values()) {
			sb.append("A data property in IRI short form is : " + cowlDataPropertyImpl.getIRI().getShortForm() + "\n");
			Set<COWLPropertyImpl> directSubDataProperties = cowlDataPropertyImpl.getDirectSubOWLProperties();
			if (!directSubDataProperties.isEmpty()) {
				sb.append("\t Direct subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : directSubDataProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> subDataProperties = cowlDataPropertyImpl.getSubOWLProperties();
			if (!subDataProperties.isEmpty()) {
				sb.append("\t Subproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl subProperty : subDataProperties)
					sb.append("\t\t" + subProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directSuperDataProperties = cowlDataPropertyImpl.getDirectSuperOWLProperties();
			if (!directSuperDataProperties.isEmpty()) {
				sb.append("\t Direct superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : directSuperDataProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> superDataProperties = cowlDataPropertyImpl.getSuperOWLProperties();
			if (!superDataProperties.isEmpty()) {
				sb.append("\t Superproperties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl superProperty : superDataProperties)
					sb.append("\t\t" + superProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> equivalentDataProperties = cowlDataPropertyImpl.getEquivalentProperties();
			if (!equivalentDataProperties.isEmpty()) {
				sb.append(
						"\t Equivalent properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl equivalentProperty : equivalentDataProperties)
					sb.append("\t\t" + equivalentProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> disjointDataProperties = cowlDataPropertyImpl.getDisjointProperties();
			if (!disjointDataProperties.isEmpty()) {
				sb.append(
						"\t Disjoint properties (direct and inferred) of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : disjointDataProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> directDisjointDataProperties = cowlDataPropertyImpl.getDisjointProperties();
			if (!directDisjointDataProperties.isEmpty()) {
				sb.append("\t Direct disjoint properties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl disjointProperty : directDisjointDataProperties)
					sb.append("\t\t" + disjointProperty.getIRI().getShortForm() + "\n");
			}

			Set<COWLPropertyImpl> relevantDataProperties = cowlDataPropertyImpl.getRelevantProperties();
			if (!relevantDataProperties.isEmpty()) {
				sb.append("\t Relevant properties of the property in IRI short form are shown as follows:\n");
				for (COWLPropertyImpl relatedProperty : relevantDataProperties)
					sb.append("\t\t" + relatedProperty.getIRI().getShortForm() + "\n");
			}

			Set<AxiomType<? extends OWLAxiom>> propertyAttributes = cowlDataPropertyImpl.getPropertyAttributes();
			if (!propertyAttributes.isEmpty()) {
				sb.append("\t Characteristics of the property are shown as follows:\n");
				for (AxiomType<? extends OWLAxiom> axiomType : propertyAttributes)
					sb.append("\t\t" + axiomType.getName() + "\n");
			}
		}
		return sb.toString();
	}
}
