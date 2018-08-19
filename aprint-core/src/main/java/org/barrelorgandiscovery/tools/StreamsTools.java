package org.barrelorgandiscovery.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import org.apache.xmlbeans.impl.util.Base64;

/**
 * Tools associated to streams
 * 
 * @author Freydiere Patrice
 * 
 */
public class StreamsTools {

	/**
	 * Copy a stream, the input stream is fully read but not closed
	 * 
	 * @param inputStreamToCopy
	 * @param outputStream
	 * @throws IOException
	 */
	public static void copyStream(InputStream inputStreamToCopy,
			OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[4096];
		int cpt;
		while ((cpt = inputStreamToCopy.read(buffer)) != -1) {
			outputStream.write(buffer, 0, cpt);
		}
	}

	/**
	 * recusively delete folders and the file inside ...
	 * 
	 * @param f
	 */
	public static void recurseDelete(File f) {
		if (f.isDirectory()) {
			File[] subfiles = f.listFiles();
			for (int i = 0; i < subfiles.length; i++) {
				File file = subfiles[i];
				recurseDelete(file);
			}
		}
		f.delete();
	}

	/**
	 * Read the stream in an UTF8 string
	 * 
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public static String fullyReadUTF8StringFromStream(InputStream s)
			throws IOException {

		InputStreamReader r = new InputStreamReader(s, Charset.forName("UTF-8"));
		char[] buffer = new char[1024];
		StringBuffer sb = new StringBuffer();
		int cpt;
		while ((cpt = r.read(buffer, 0, buffer.length)) != -1) {
			sb.append(buffer, 0, cpt);
		}

		return sb.toString();
	}

	/**
	 * Write a string into the stream
	 * 
	 * @param string
	 * @param s
	 * @throws IOException
	 */
	public static void fullyWriteUTF8StringIntoStream(String string,
			OutputStream s) throws IOException {

		OutputStreamWriter w = new OutputStreamWriter(s,
				Charset.forName("UTF-8"));
		w.write(string);
		w.close();

	}

	/**
	 * Compute the SHA digest for a file
	 * 
	 * @param inputFile
	 * @return
	 * @throws Exception
	 */
	public static byte[] computeSHADigest(File inputFile) throws Exception {

		InputStream fis = new BufferedInputStream(
				new FileInputStream(inputFile));
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.reset();

			byte[] c = new byte[10000];
			int cpt;
			while ((cpt = fis.read(c)) != -1) {
				md.update(c, 0, cpt);
			}

			byte[] digest = Base64.encode(md.digest());

			return digest;

		} finally {
			fis.close();
		}
	}

}
