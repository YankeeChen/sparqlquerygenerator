package edu.neu.ece.sparqlquerygenerator.generator;

import org.apache.jena.sparql.core.Var;

import edu.neu.ece.sparqlquerygenerator.entity.COWLClassImpl;

/**
 * This class defines functions for variable generation.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2019-07-29
 */
public final class VariableGenerator {

	/**
	 * This class cannot be instantiated.
	 */
	private VariableGenerator() {

	}

	/**
	 * A counter that traces the index of next variable that binds to data value.
	 */
	private static long nextBindToDataValueVariableIndex = 0;

	/**
	 * A counter that traces the index of next variable that binds to individuals of
	 * an anonymous class expression.
	 */
	private static long nextBindToAnonymousClassExpressionVariableIndex = 0;

	/**
	 * Generate a variable that binds to individuals of the specified OWL class.
	 * 
	 * @param cls
	 *            OWL class.
	 * @return Variable
	 */
	public static Var generateVariable(COWLClassImpl cls) {
		return Var.alloc(cls.getIRI().getShortForm().replace("-", "_") + "_" + cls.getNextVariableIndex());
	}

	/**
	 * Generate a variable that binds to individuals of the specified OWL class
	 * expression.
	 * 
	 * @return Variable
	 */
	public static Var generateVariable() {
		return Var.alloc("Var" + nextBindToAnonymousClassExpressionVariableIndex++);
	}

	/**
	 * Generate a variable that binds to data value.
	 * 
	 * @return Variable
	 */
	public static Var generateBindToDataValueVariable() {
		return Var.alloc("DataValue" + nextBindToDataValueVariableIndex++);
	}

	/**
	 * Reset index of next bind to data value variable to 0.
	 */
	public static void resetNextBindToDataValueVariableIndex() {
		nextBindToDataValueVariableIndex = 0;
	}

	/**
	 * Reset index of next bind to anonymous class expression variable to 0.
	 */
	public static void resetNextBindToAnonymousClassExpressionVariableIndex() {
		nextBindToAnonymousClassExpressionVariableIndex = 0;
	}
}
