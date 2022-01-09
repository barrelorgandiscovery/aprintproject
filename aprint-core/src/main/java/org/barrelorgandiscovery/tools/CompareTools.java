package org.barrelorgandiscovery.tools;

public class CompareTools {

	/**
	 * compare two integers
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public static boolean compare(Integer i, Integer j) {
		if (i == null) {

			return j == null;

		} else {
			assert i != null;

			if (j == null) {
				return false;
			}
			return i.compareTo(j) == 0;
		}
	}

}
