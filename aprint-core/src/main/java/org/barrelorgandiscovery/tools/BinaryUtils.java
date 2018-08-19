package org.barrelorgandiscovery.tools;

/**
 * BinaryTools for manipulating some byte streams
 * 
 * @author Freydiere Patrice
 * 
 */
public class BinaryUtils {

	/**
	 * Find the offset of a binary pattern in an array
	 * 
	 * @param find
	 * @param content
	 * @param start
	 * @return
	 */
	public static int nextOccur(byte[] find, byte[] content, int start) {
		if (find == null || content == null)
			return -1;

		if (start > content.length)
			return -1;

		int cpt = start;

		while (cpt < content.length) {

			int i;
			for (i = 0; i < find.length; i++) {

				if (cpt + i >= content.length)
					return -1;

				if (content[cpt + i] != find[i]) {
					break;
				}
			}

			if (i == find.length) {
				return cpt;
			}

			cpt++;
		}

		return -1;

	}

	public static long readInt4bytes(byte[] content, int offset) {
		long taille = readByte(content, offset) << 32
				| readByte(content, offset + 1) << 16
				| readByte(content, offset + 2) << 8
				| readByte(content, offset + 3);
		return taille;
	}

	public static void writeInt4Byte(byte[] content, int offset, long value) {
		content[offset] = (byte) ((value >> 32) & 0xFF);
		content[offset + 1] = (byte) ((value >> 16) & 0xFF);
		content[offset + 2] = (byte) ((value >> 8) & 0xFF);
		content[offset + 3] = (byte) (value & 0xFF);

	}

	public static long readInt2Bytes(byte[] content, int offset) {
		long f = readByte(content, offset) << 8 | readByte(content, offset + 1);
		return f;
	}

	public static int readByte(byte[] content, int offset) {
		if (content[offset] < 0)
			return content[offset] + 256;

		return content[offset];
	}

}
