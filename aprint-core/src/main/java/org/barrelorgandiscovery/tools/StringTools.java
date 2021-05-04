package org.barrelorgandiscovery.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;

public class StringTools {

	/**
	 * this function remove all the special characters in the string
	 * 
	 * @param s the string to convert
	 */
	public static String convertToPhysicalNameWithEndingHashCode(String s) {
		if (s == null)
			return null;

		String newname = ""; //$NON-NLS-1$

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			char lc = Character.toLowerCase(c);

			if ((lc >= 'a' && lc <= 'z') || (lc >= '0' && lc <= '9')) {
				newname += lc;
			} else {
				newname += "_"; //$NON-NLS-1$
			}

		}

		newname += "_" + s.hashCode(); //$NON-NLS-1$

		return newname;

	}

	public static String convertToPhysicalName(String s) {
		return convertToPhysicalName(s, false);
	}

	public static String convertToPhysicalName(String s, boolean nospace) {
		if (s == null)
			return null;

		String newname = ""; //$NON-NLS-1$

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			char lc = Character.toLowerCase(c);

			if ((lc >= 'a' && lc <= 'z') || (lc >= '0' && lc <= '9') || (!nospace && lc == ' ') || lc == '-'
					|| lc == '.') {
				newname += lc;
			} else {
				newname += "_"; //$NON-NLS-1$
			}

		}

		return newname;
	}

	public static String removeExtension(String filename, String extensionToRemove) {
		if (filename == null)
			return null;
		if (filename.endsWith(extensionToRemove))
			return filename.substring(0, filename.lastIndexOf('.'));

		return filename;
	}

	private static char[] hexTable = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String toHex(String string) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char charAt = string.charAt(i);

			int first = ((int) charAt) & 0x0F;
			int second = (((int) charAt) >> 4) & 0x0F;

			sb.append(hexTable[second]).append(hexTable[first]);
		}
		return sb.toString();
	}

	private static int hexValue(char c) {

		if (c >= '0' && c <= '9') {
			return (c - '0');

		} else if (c >= 'A' && c <= 'F') {
			return 10 + (c - 'A');
		} else {
			throw new RuntimeException("char " + c + " is not a hex character ...");
		}
	}

	public static String fromHex(String hexString) {

		StringBuffer sb = new StringBuffer();
		int cpt = 0;

		while (cpt < hexString.length()) {

			int i1 = hexValue(hexString.charAt(cpt++));
			int i2 = hexValue(hexString.charAt(cpt++));
			sb.append((char) (i2 + i1 * 16));
		}

		return sb.toString();

	}

	/**
	 * Concatene les éléments en utilisant un delimiteur
	 * 
	 * @param elements
	 * @param delimiter
	 * @return
	 */
	public static String join(String[] elements, String delimiter) {
		if (elements == null || elements.length == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < elements.length; i++) {
			String string = elements[i];
			if (sb.length() > 0)
				sb.append(delimiter);

			sb.append(string);
		}
		return sb.toString();
	}

	public static boolean equals(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		} else {
			return s1.equals(s2);
		}
	}

	/**
	 * @param s1
	 * @param s2
	 */
	public static int compare(String s1, String s2) {
		if (!StringTools.equals(s1, s2)) {
			if (s1 != null) {
				return s1.compareTo(s2);
			} else {
				return -s2.compareTo(s1);
			}
		}
		return 0;
	}

	public static String nullIfEmpty(String s) {
		if ("".equals(s))
			return null;

		return s;
	}

	/**
	 * load utf8 script
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static StringBuffer loadUTF8FileContent(File file) throws Exception {
		FileInputStream istream = new FileInputStream(file);
		try {
			return loadUTF8FileContent(istream);
		} finally {
			istream.close();
		}
	}

	public static StringBuffer loadUTF8FileContent(InputStream file) throws Exception {
		StringBuffer sb = new StringBuffer();

		InputStream istream = file;

		LineNumberReader r = new LineNumberReader(new InputStreamReader(istream, Charset.forName("UTF-8"))); //$NON-NLS-1$
		try {

			String s = r.readLine();
			while (s != null) {
				if (sb.length() > 0)
					sb.append("\n"); //$NON-NLS-1$
				sb.append(s);
				s = r.readLine();
			}
		} finally {
			r.close();
		}

		return sb;
	}

}
