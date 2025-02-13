package org.barrelorgandiscovery.extensionsng.scanner.wizard.scanormerge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class SerializeTools {

	/**
	 * Write object in the stream
	 * 
	 * @param object
	 * @param out
	 */
	public static void writeObject(Serializable object, OutputStream out) {
		assert out != null;
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			try {
				oos.writeObject(object);
			} finally {
				oos.close();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * read object from stream
	 * 
	 * @param in
	 * @return
	 */
	public static Serializable readObject(InputStream in) {
		assert in != null;
		try {
			ObjectInputStream ois = new ObjectInputStream(in);
			try {
				return (Serializable) ois.readObject();
			} finally {
				ois.close();
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Clone en profondeur de l'objet
	 * 
	 * si l'objet passé en paramètre est null, null est retourné
	 * 
	 * @param object
	 *            l'objet à cloner
	 * @return la copie de l'objet
	 */
	public static Object deepClone(Serializable object) {

		if (object == null)
			return null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(object);
			} finally {
				oos.close();
			}

			ByteArrayInputStream bais = new ByteArrayInputStream(
					baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			try {
				return ois.readObject();
			} finally {
				ois.close();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Save a serializable objet to file
	 * 
	 * @param object
	 * @param file
	 * @throws Exception
	 */
	public static void save(Object object, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
		} finally {
			fos.close();
		}
	}

	/**
	 * LoadObject
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Object load(File file) throws IOException,
			ClassNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		try {
			ObjectInputStream ois = new ObjectInputStream(fis);
			return ois.readObject();
		} finally {
			fis.close();
		}
	}

	/**
	 * load a base64 encoded object
	 */
	public static Serializable loadBase64(String base64Stream) throws Exception {
		if (base64Stream == null)
			return null;

		Decoder dec = Base64.getDecoder();
		ByteArrayInputStream bais = new ByteArrayInputStream(
				dec.decode(base64Stream));
		try {
			return readObject(bais);
		} finally {
			bais.close();
		}
	}

	/**
	 * save an object as a base 64 string
	 * 
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static String saveBase64(Serializable o) throws Exception {
		if (o == null)
			return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeObject(o, baos);

		Encoder enc = Base64.getEncoder();
		return enc.encodeToString(baos.toByteArray());

	}

}
