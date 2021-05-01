package edu.neu.ece.sparqlquerygenerator.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

/**
 * Collection utility class that contains methods on collection operations.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public final class CollectionUtil {

	/**
	 * This class cannot be instantiated.
	 */
	private CollectionUtil() {
	}

	/**
	 * Get a random element from a list.
	 * 
	 * @param <T>
	 *            The class of the objects in the list.
	 * @param typeList
	 *            List that contains elements of T type.
	 * @param random
	 *            Used to generate a stream of pseudorandom numbers.
	 * @return Element of T type.
	 * @throws Exception
	 *             If input arguments are invalid.
	 */
	public static <T> T getARandomElementFromList(List<T> typeList, Random random) throws Exception {
		if (typeList == null || random == null)
			throw new Exception("Invalid input arguments!");
		if (typeList.isEmpty())
			return null;
		int index = random.nextInt(typeList.size());
		T t = (T) typeList.get(index);
		return t;
	}

	/**
	 * Get a random element from a set.
	 * 
	 * @param <T>
	 *            The class of the objects in the set.
	 * @param typeSet
	 *            Set of elements of T type.
	 * @param random
	 *            Used to generate a stream of pseudorandom numbers.
	 * @return Element of T type.
	 * @throws Exception
	 *             If input arguments are invalid.
	 */
	public static <T> T getARandomElementFromSet(Set<T> typeSet, Random random) throws Exception {
		if (typeSet == null || random == null)
			throw new Exception("Invalid input arguments!");
		if (typeSet.isEmpty())
			return null;
		int index = random.nextInt(typeSet.size());
		@SuppressWarnings("unchecked")
		T t = (T) typeSet.toArray()[index];
		return t;
	}

	/**
	 * Generate all subset of a set of entries.
	 * 
	 * @param <T1>
	 *            The class of the key of the entry of the set.
	 * @param <T2>
	 *            The class of the value of the entry of the set.
	 * @param set
	 *            A set.
	 * @return A list containing all subset of set.
	 * @deprecated
	 */
	public <T1, T2> List<Set<Entry<T1, T2>>> getAllSubSetsOfASetOfEntries(Set<Entry<T1, T2>> set) {
		int eleNumber = set.size();
		if (eleNumber == 0)
			return new ArrayList<>();
		int subsetNumber = (int) Math.pow(2, eleNumber);
		List<Set<Entry<T1, T2>>> results = new ArrayList<>(subsetNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + "; eleNumber
		// is: " + eleNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		for (int i = 0; i < subsetNumber; i++) {
			results.add(new HashSet<Entry<T1, T2>>());
		}
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		// @SuppressWarnings("unchecked")
		// Set<Entry<T1, Set<T2>>>[] resultsInArray = results.toArray(new
		// HashSet[subsetNumber]);
		@SuppressWarnings("unchecked")
		Entry<T1, T2>[] setInArray = set.toArray(new Entry[eleNumber]);
		for (int i = 0; i < eleNumber; i++)
			for (int j = 0; j < subsetNumber; j++)
				if ((j >> i & 1) == 1) {
					// System.out.println(resultsInArray[j].toString());
					results.get(j).add(setInArray[i]);
				}

		// @SuppressWarnings("unchecked")
		// Set<Set<Entry<T1, Set<T2>>>> f = new HashSet<>((Collection<? extends
		// Set<Entry<T1, Set<T2>>>>) Arrays.asList(results));
		return results;

	}

	/**
	 * Generate all subset of a set.
	 * 
	 * @param <T>
	 *            The class of the objects in the set.
	 * @param set
	 *            Set of elements of T type.
	 * @return List that contains all subset of the set.
	 * @throws Exception
	 *             If input arguments are invalid.
	 */
	public static <T> ArrayList<Set<T>> getAllSubSetsOfASet(Set<T> set) throws Exception {
		if (set == null)
			throw new Exception("Invalid input arguments!");
		int eleNumber = set.size();
		if (eleNumber == 0)
			return new ArrayList<>();
		int subsetNumber = (int) Math.pow(2, eleNumber);
		ArrayList<Set<T>> results = new ArrayList<>(subsetNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + "; eleNumber
		// is: " + eleNumber);
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		for (int i = 0; i < subsetNumber; i++) {
			results.add(new HashSet<T>());
		}
		// System.out.println("subsetNumber is: " + subsetNumber + ";
		// results.size() is: " + results.size());
		// @SuppressWarnings("unchecked")
		// Set<Entry<T1, Set<T2>>>[] resultsInArray = results.toArray(new
		// HashSet[subsetNumber]);
		@SuppressWarnings("unchecked")
		T[] setInArray = (T[]) set.toArray(new Object[eleNumber]);
		for (int i = 0; i < eleNumber; i++)
			for (int j = 0; j < subsetNumber; j++)
				if ((j >> i & 1) == 1) {
					// System.out.println(resultsInArray[j].toString());
					results.get(j).add(setInArray[i]);
				}

		// @SuppressWarnings("unchecked")
		// Set<Set<Entry<T1, Set<T2>>>> f = new HashSet<>((Collection<? extends
		// Set<Entry<T1, Set<T2>>>>) Arrays.asList(results));
		return results;

	}
}
