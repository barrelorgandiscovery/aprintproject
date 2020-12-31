package org.barrelorgandiscovery.tools;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Base 64 tools, to manage decoding regression, between versions
 * 
 * @author pfreydiere
 *
 */
public class Base64Tools {

	public static byte[] decode(String src) {
		if (src == null || src.length() == 0) {
			return new byte[0];
		}

		Decoder decoder = Base64.getMimeDecoder();
		return decoder.decode(src);
	}

	public static String encode(byte[] bytes) throws Exception {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		Encoder encoder = Base64.getMimeEncoder();

		return new String(encoder.encode(bytes), "UTF-8");
	}

}
