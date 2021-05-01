package edu.neu.ece.sparqlquerygenerator.evaluator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;

/**
 * This class models property structure of a SPARQL query.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-07-29
 */
public class QueryProperty {

	/**
	 * Query ID.
	 */
	public int queryID = -1;

	/**
	 * Triple patterns in a SPARQL query.
	 */
	public LinkedList<Triple> triplePatterns = new LinkedList<>();

	/**
	 * Nodes that are OWL named classes represented as an IRI in string.
	 */
	public Set<String> classNodes = new HashSet<>();

	/**
	 * Nodes that are OWL properties represented as an IRI in string.
	 */
	public Set<String> propertyNodes = new HashSet<>();

	/**
	 * The number of each of the six join type in a SPARQL query:
	 * Subject-Subject(SS), Predicate-Predicate(PP), Object-Object(OO),
	 * Subject-Predicate(SP), Subject-Object(SO) and Predicate-Object(PO).
	 */
	public int[] tripleJointypeCount = new int[6];

	/**
	 * A boolean value that measures whether a SPAPRQL query contains keyword
	 * FILTER. True if contains, false otherwise.
	 */
	public boolean containKeywordFILTER = false;

	/**
	 * A boolean value that measures whether a SPAPRQL query contains keyword AND.
	 * True if contains, false otherwise.
	 */
	public boolean containKeywordAND = false;

	/**
	 * A boolean value that measures whether a SPAPRQL query contains keyword UNION.
	 * True if contains, false otherwise.
	 */
	public boolean containKeywordUNION = false;

	/**
	 * A boolean value that measures whether a SPAPRQL query contains keyword
	 * OPTIONAL. True if contains, false otherwise.
	 */
	public boolean containKeywordOPTIONAL = false;

	/**
	 * A boolean value that measures whether a SPAPRQL query contains keyword MINUS.
	 * True if contains, false otherwise.
	 */
	public boolean containKeywordMINUS = false;

	/**
	 * A boolean value that measures whether a SPAPRQL query contains keyword NOT
	 * EXISTS. True if contains, false otherwise.
	 */
	public boolean containKeywordNOTEXISTS = false;

	/**
	 * A boolean value that measures whether a SPAPRQL query contains keyword
	 * EXISTS. True if contains, false otherwise.
	 */
	public boolean containKeywordEXISTS = false;

	/**
	 * Constructor.
	 * 
	 * @param queryID
	 *            Query ID.
	 */
	public QueryProperty(int queryID) {
		this.queryID = queryID;
	}

	/**
	 * Record properties related to triple patterns of a SPARQL query.
	 */
	public void processTriplePatterns() {
		if (triplePatterns.isEmpty())
			return;
		Node s1, s2;
		Node p1, p2;
		Node o1, o2;
		Triple t1, t2;
		for (int i = 0; i < triplePatterns.size() - 1; i++) {
			t1 = triplePatterns.get(i);
			s1 = t1.getSubject();
			p1 = t1.getPredicate();
			o1 = t1.getObject();
			for (int j = i + 1; j < triplePatterns.size(); j++) {
				t2 = triplePatterns.get(j);
				s2 = t2.getSubject();
				p2 = t2.getPredicate();
				o2 = t2.getObject();
				if (s1.equals(s2))
					tripleJointypeCount[0] += 1;
				else if (p1.equals(p2))
					tripleJointypeCount[1] += 1;
				else if (o1.equals(o2))
					tripleJointypeCount[2] += 1;
				else if (s1.equals(p2) || p1.equals(s2))
					tripleJointypeCount[3] += 1;
				else if (s1.equals(o2) || o1.equals(s2))
					tripleJointypeCount[4] += 1;
				else if (p1.equals(o2) || o1.equals(p2))
					tripleJointypeCount[5] += 1;
			}
		}

		Node p;
		for (Triple t : triplePatterns) {
			p = t.getPredicate();
			if (p == RDF.Nodes.type)
				classNodes.add(t.getObject().getURI());
			else if (p instanceof Node_URI)
				propertyNodes.add(p.getURI());
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Summary of query" + queryID + ":\n");
		sb.append("\tcontainKeywordFILTER = " + containKeywordFILTER + "\n");
		sb.append("\tcontainKeywordAND = " + containKeywordAND + "\n");
		sb.append("\tcontainKeywordUNION = " + containKeywordUNION + "\n");
		sb.append("\tcontainKeywordOPTIONAL = " + containKeywordOPTIONAL + "\n");
		sb.append("\tcontainKeywordMINUS = " + containKeywordMINUS + "\n");
		sb.append("\tcontainKeywordNOTEXISTS = " + containKeywordNOTEXISTS + "\n");
		sb.append("\tcontainKeywordEXISTS = " + containKeywordEXISTS + "\n");
		sb.append("\tThe number of each of the six join types is shown below:\n");
		sb.append("\t\tSubject-Subject(SS) = " + tripleJointypeCount[0] + "; Predicate-Predicate(PP) = "
				+ tripleJointypeCount[1] + "; Object-Object(OO) = " + tripleJointypeCount[2]
				+ "; Subject-Predicate(SP) = " + tripleJointypeCount[3] + "; Subject-Object(SO) = "
				+ tripleJointypeCount[4] + "; Predicate-Object(PO) = " + tripleJointypeCount[5] + "\n");
		sb.append("\tTriple patterns are shown below:\n");
		for (Triple t : triplePatterns)
			sb.append("\t\t" + t.toString() + "\n");
		sb.append("\tClass signatures are shown below:\n");
		Iterator<String> iterator = classNodes.iterator();
		boolean flag = true;
		while (iterator.hasNext()) {
			if (flag) {
				flag = false;
				sb.append("\t\t");
			}
			sb.append(iterator.next() + " ");
		}
		sb.append("\n\tProperty signatures are shown below:\n");
		iterator = propertyNodes.iterator();
		flag = true;
		while (iterator.hasNext()) {
			if (flag) {
				flag = false;
				sb.append("\t\t");
			}
			sb.append(iterator.next() + " ");
		}
		sb.append("\n");
		return sb.toString();
	}
}
