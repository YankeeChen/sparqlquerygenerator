# SQG - SPARQL Query Generator
SQG is a generic SPARQL query generator for requesting cognitive radio capabilities. It is implemented in Java with the use of the OWL API, a high level Application Programming Interface for working with OWL ontologies, and the Jena ARQ, a query engine for Jena that supports the SPARQL RDF query language. It is able to generate large numbers of synthetic random SPARQL queries against RDF device descriptions that comply with a given RF ontology schema.

## SQG Command Line Interface

#### Download SQG
You can get a copy of it stored in Github repository with the following command:
```console
$ git clone https://github.com/YankeeChen/sparqlquerygenerator.git
```

#### Usage
```console
# argument help to see all available options
$ java -jar sparqlquerygenerator-1.0-SNAPSHOT.jar -h

# Simple call (Load target RDF device descriptions based on its ontology IRI or document IRI.
$ java -jar sparqlquerygenerator-1.0-SNAPSHOT.jar -rootIRI <IRI> -datasetURI <URI> [-IRIMapping <IRIMapping1,IRIMapping2,IRIMapping3...>] [-queryNumber <NUMBER>] [-outputDirectoryPath <PATH>] [-ramSeed <SEED>] [-classConstraintSelectionProbability <PROBABILITY>] [-classAssertionProbability <PROBABILITY>] [-objectPropertyAssertionProbability <PROBABILITY>] [-dataPropertyAssertionProbability <PROBABILITY>] [-inverseObjectPropertySelectionProbability <PROBABILITY>] [-newVariableProbability <PROBABILITY>] [-linkToIndividualProbability <PROBABILITY>] [-filterProbability <PROBABILITY>] [-conjunctionGraphPatternProbability <PROBABILITY>] [-optionalGraphPatternProbability <PROBABILITY>] [-unionGraphPatternProbability <PROBABILITY>] [-negationGraphPatternProbability <PROBABILITY>]

```
`-rootIRI <IRI>` e.g. http://www.loa-cnr.it/ontologies/DUL.owl#PhysicalObject 
is required in all cases and states a IRI that relates to the root class of the input ontology. 

`-datasetURI <URI>` e.g. file:/Users/yanji/Dropbox/workspace/devicedescriptiongenerator/evaluationresults/SDROntology/Datasets/DeviceDescription3000.rdf
is required in all cases and states a URI that relates to the target RDF device descriptions.

`-IRIMapping <IRIMapping1,IRIMapping2,IRIMapping3...>` e.g. http://purl.oclc.org/NET/ssnx/ssn,file:ontologies/WM30Ontology/SSN.owl;http://purl.oclc.org/NET/ssnx/qu/qu,file:ontologies/WM30Ontology/qu.owl
is optional and states ontology IRIs to document IRIs mapping. Mappings are splitted by semicolon whereas ontology IRI and document IRI within each mapping are splitted by comma.

`-queryNumber <NUMBER>` 
is optional and states the number of queries; 1 by default.

`-outputDirectoryPath <PATH>` 
is optional and states a local directory of the generated queries; SPARQLqueries/ by default.

`-ramSeed <SEED>` 
is optional and states random seed used for random query generation; 0 by default.

`-classConstraintSelectionProbability <PROBABILITY>`
is optional and states the probability of selecting an OWL class constraint (anonymous super class expression) of an OWL class; 0.9 by default.

`-classAssertionProbability <PROBABILITY>`
is optional and states the probablity of generating class assertion axioms for each named individual; 1.0 by default.

`-objectPropertyAssertionProbability <PROBABILITY>`
is optional and states the probability of creating a new object property assertion triple pattern; 0.5 by default.

`-dataPropertyAssertionProbability <PROBABILITY>`
is optional and states the probability of creating a new data property assertion triple pattern; 0.5 by default.

`-inverseObjectPropertySelectionProbability <PROBABILITY>`
is optional and states the probability of selecting an inverse property (direct or inferred) of an object property for object property assertion triple pattern generation; 0.8 by default.

`-newVariableProbability <PROBABILITY>`
is optional and states the probability of creating a new variable; 0.5 by default.

`-linkToIndividualProbability <PROBABILITY>`
is optional and states the probablity of linking a variable to an individual; 0.2 by default.

`-filterProbability <PROBABILITY>`
is optional and states the probability of creating a filter expression that restricts the solutions of a graph pattern; 0.5 by default.

`-conjunctionGraphPatternProbability <PROBABILITY>`
is optional and states the probability of creating a conjunction graph pattern; 0.8 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.

`-optionalGraphPatternProbability <PROBABILITY>`
is optional and states the probability of creating an optional graph pattern; 0 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.

`-unionGraphPatternProbability <PROBABILITY>`
is optional and states the probability of creating an union graph pattern; 0.15 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.

`-negationGraphPatternProbability <PROBABILITY>`
is optional and states the probability of creating a negation graph pattern; 0.05 by default. Note that the sum of four types of graph patterns (conjunction, optional, union and negation) MUST be 1.

What SQG will do is:
1. Load target RDF device descriptions and background knowledge.
2. Process ontology (that includes Process Entities, Process Axioms and Infer New Knowledge). 
3. Generate queries for matching devices.
4. Collect evaluation metrics and dump them into file with local path evaluationresults/QueryEvaluationResults_\<QUERY-NUMBER\>Queries.txt

## Contact
Yanji Chen

Lab of Info and Software Fusion

ECE Department, Northeastern University, MA, USA

Email:chen.yanj@husky.neu.edu

Personal website: https://sites.google.com/view/yanjichen0101/home?authuser=0

