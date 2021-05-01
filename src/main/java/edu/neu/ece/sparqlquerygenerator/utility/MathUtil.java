package edu.neu.ece.sparqlquerygenerator.utility;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;

/**
 * Math utility that contains math function implementations.
 * 
 * @author Yanji Chen
 * @version 1.0
 * @since 2018-10-02
 */
public final class MathUtil {

	/**
	 * This class cannot be instantiated.
	 */
	private MathUtil() {
	}

	/**
	 * Get a random integer confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random integer value.
	 * @param max
	 *            The bound (exclusive) of each random integer value.
	 * @param ran
	 *            Random object.
	 * @return A random integer with the given integer range.
	 * @throws Exception
	 *             If the input parameters are invalid.
	 */
	public static int getRandomIntegerInRange(int min, int max, Random ran) throws Exception {
		/*
		 * Not correct, which may cause overflow. return ran.nextInt((max - min) + 1) +
		 * min;
		 */
		// return ThreadLocalRandom.current().nextInt(min, max + 1);
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.ints(min, max).findFirst().getAsInt();
	}

	/**
	 * Get a random long confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random long value.
	 * @param max
	 *            The bound (exclusive) of each random long value.
	 * @param ran
	 *            Random object.
	 * @return A random long with the given long range.
	 * @throws Exception
	 *             If the input parameters are invalid.
	 */
	public static long getRandomLongInRange(long min, long max, Random ran) throws Exception {
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.longs(min, max).findFirst().getAsLong();
	}

	/**
	 * Get a random double confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random double value.
	 * @param max
	 *            The bound (exclusive) of each random double value.
	 * @param ran
	 *            Random object.
	 * @return A random double with the given double range.
	 * @throws Exception
	 *             If the input parameters are invalid.
	 */
	public static double getRandomDoubleInRange(double min, double max, Random ran) throws Exception {
		// return ran.nextDouble()*((max - min) + 1) + min;
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.doubles(min, max).findFirst().getAsDouble();
	}

	/**
	 * Get a random boolean value with equal true/false probability.
	 * 
	 * @param ran
	 *            Random object
	 * @return A random boolean value.
	 */
	public static boolean getRandomBoolean(Random ran) {
		if (ran.nextDouble() < 0.5)
			return true;
		else
			return false;
	}

	/**
	 * Get a random float confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random float value.
	 * @param max
	 *            The bound (exclusive) of each random float value.
	 * @param ran
	 *            Random object.
	 * @return A random float with the given float range.
	 * @throws Exception
	 *             If the input parameters are invalid.
	 */
	public static float getRandomFloatInRange(float min, float max, Random ran) throws Exception {
		if (max == min)
			return min;
		if (max < min)
			throw new Exception();
		return ran.nextFloat() * ((max - min)) + min;
	}

	/**
	 * Get a random string whose length confirms to the given origin (inclusive) and
	 * bound (exclusive).
	 * 
	 * @param minStringLength
	 *            The minimum string length (inclusive).
	 * @param maxStringLength
	 *            The maximum string length (inclusive).
	 * @param ran
	 *            Random object.
	 * @return A random String with the given string length range.
	 */
	public static String getRandomString(int minStringLength, int maxStringLength, Random ran) {
		final String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		int stringLength;
		try {
			stringLength = getRandomIntegerInRange(minStringLength, maxStringLength + 1, ran);
			// length of the random string.
			while (salt.length() < stringLength) {
				int index = (int) (ran.nextFloat() * SALTCHARS.length());
				salt.append(SALTCHARS.charAt(index));
			}
			String saltStr = salt.toString();
			return saltStr;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a random decimal confirming to the given origin (inclusive) and bound
	 * (exclusive).
	 * 
	 * @param min
	 *            The origin (inclusive) of each random decimal value.
	 * @param max
	 *            The bound (exclusive) of each random decimal value.
	 * @param ran
	 *            Random object.
	 * @return A random decimal with the given decimal range.
	 */
	public static String getRandomDecimalInString(double min, double max, Random ran) {
		DecimalFormat df = new DecimalFormat("#.##");
		try {
			String result = df.format(getRandomDoubleInRange(min, max, ran));
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Calculate space coverage metric of a collection of booleans. The metric is
	 * calculated as the ratio of the number boolean values that are true over the
	 * total number of boolean values.
	 * 
	 * @param values
	 *            A collection of boolean values.
	 * @return Space coverage metric.
	 * @throws Exception
	 *             If the collection is null.
	 */
	public static double calculateSpaceCoverage(Collection<Boolean> values) throws Exception {
		if (values == null)
			throw new Exception();
		if (values.isEmpty())
			return 1;
		double results = 0;
		int count = 0;
		for (Boolean value : values)
			if (value.booleanValue() == true)
				count++;
		results = (double) count / values.size();
		return results;
	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * Random ran = new Random();
	 * 
	 * for (int count = 0; count < 200; count++) {
	 * //System.out.printf("A random integer is: " +
	 * getRandomIntegerInRange(Integer.MIN_VALUE, Integer.MAX_VALUE - 1, ran) +
	 * "\n"); //System.out.printf("A random double is: " +
	 * getRandomDoubleInRange(-100, 100, ran) + "\n");
	 * //System.out.printf("A random boolean is: " + getRandomBoolean(ran) + "\n");
	 * //System.out.printf("A random float is: " + getRandomFloatInRange(-100, 100,
	 * ran) + "\n"); //System.out.printf("A random string is: " + getRandomString(4,
	 * 10, ran) + "\n"); //System.out.printf("A random decimal is: " +
	 * getRandomDecimalInString(-100, 100, ran) + "\n"); }
	 * 
	 * String regex = "[ab]{4,6}c"; Generex generator = new Generex(regex); String
	 * result = generator.random(); System.out.println(result); //assert
	 * result.matches(regex); }
	 */
}
