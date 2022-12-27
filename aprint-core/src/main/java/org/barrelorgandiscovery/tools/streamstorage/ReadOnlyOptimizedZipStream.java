package org.barrelorgandiscovery.tools.streamstorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.barrelorgandiscovery.tools.Base64Tools;
import org.barrelorgandiscovery.tools.NotImplementedException;



public class ReadOnlyOptimizedZipStream implements StreamStorage {

	private HashMap<String, byte[]> internalStreams = new HashMap<String, byte[]>();

	public ReadOnlyOptimizedZipStream(ZipInputStream z) throws Exception {

		ZipEntry ze;
		while ((ze = z.getNextEntry()) != null) {
			String name = ze.getName();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int cpt;
			while ((cpt = z.read(buffer)) != -1) {
				baos.write(buffer, 0, cpt);
			}
			internalStreams.put(name, baos.toByteArray());
		}
	}

	public String[] listStreams() {

		ArrayList<String> names = new ArrayList<String>();

		for (Iterator iterator = internalStreams.keySet().iterator(); iterator
				.hasNext();) {
			String string = (String) iterator.next();
			names.add(string);
		}

		return names.toArray(new String[0]);
	}

	public String[] listStreams(String type) {
		ArrayList<String> names = new ArrayList<String>();

		for (Iterator iterator = internalStreams.keySet().iterator(); iterator
				.hasNext();) {
			String string = (String) iterator.next();
			if (string.endsWith("." + type))
				names.add(string);
		}

		return names.toArray(new String[0]);
	}

	public String[] listTypes() {
		Set<String> types = new TreeSet<String>();

		for (Iterator iterator = internalStreams.keySet().iterator(); iterator
				.hasNext();) {
			String string = (String) iterator.next();

			int pos = string.lastIndexOf(".");
			if (pos != -1) {
				types.add(string.substring(pos + 1));
			}

		}

		return types.toArray(new String[0]);

	}
	
	public String getStreamDigest(String stream) throws Exception {
		byte[] content = internalStreams.get(stream);
		if (content == null)
			throw new Exception("stream " + stream + " not found");
		
		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.reset();
		md.update(content);
		
		return Base64Tools.encode(md.digest());
	}

	public InputStream openStream(String stream) throws IOException {

		if (internalStreams.containsKey(stream))
			return new ByteArrayInputStream(internalStreams.get(stream));

		throw new IOException("stream " + stream + " not found");
	}

	public boolean isReadOnly() {
		return true;
	}

	public void deleteStream(String name, String type) throws IOException {
		throw new NotImplementedException();
	}

	public void saveStream(String name, String type, InputStream stream)
			throws IOException {
		throw new NotImplementedException();
	}

}
