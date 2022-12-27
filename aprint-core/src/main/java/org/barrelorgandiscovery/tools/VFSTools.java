package org.barrelorgandiscovery.tools;

import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.AbstractFileObject;

/**
 * Tools for interacting between VFS and Java file API
 * 
 * @author pfreydiere
 *
 */
public class VFSTools {

	static StandardFileSystemManager FSMANAGER = null;

	public static StandardFileSystemManager getManager() throws Exception {
		if (FSMANAGER != null) {
			return FSMANAGER;
		}
		StandardFileSystemManager fsManager = new StandardFileSystemManager();
		URL providerResourceUrl = VFSTools.class.getClassLoader().getResource("providers.xml");
		fsManager.setConfiguration(providerResourceUrl);

		fsManager.init();
		FSMANAGER = fsManager;
		VFS.setManager(FSMANAGER);
		return FSMANAGER;
	}

	public static String decodeURIEncoding(String encodedURL) throws Exception {
		try {
			String result = java.net.URLDecoder.decode(encodedURL, StandardCharsets.UTF_8.name());
			return result;
		} catch (UnsupportedEncodingException e) {
			// not going to happen - value came from JDK's own StandardCharsets
			return encodedURL;
		}
	}

	public static AbstractFileObject fromRegularFile(File file) throws Exception {

		FileObject f = getManager().resolveFile(file.toURL().toString());
		if (!f.exists()) {
			throw new Exception("file " + file + " does not exists");
		}
		assert f.isAttached();
		return (AbstractFileObject) f;

	}

	public static File convertToFile(FileObject fo) throws Exception {
		if (fo == null) {
			return null;
		}
		String uri = fo.getName().toString();
		if (!uri.startsWith("file://")) {
			throw new Exception("file object " + fo + " is not a file");
		}
		return new File(uri.substring("file://".length()));
	}

	public static AbstractFileObject ensureExtensionIs(AbstractFileObject f, String extension) throws Exception {
		if (f == null) {
			return null;
		}

		FileName filename = f.getName();
		if (filename.getBaseName().toLowerCase().endsWith("." + extension.toLowerCase())) {
			// ok for the extension
			return f;
		}
		try {
			return (AbstractFileObject) f.getFileSystem().resolveFile(filename.toString() + "." + extension);
		} finally {
			f.close();
		}
	}

	public static OutputStream transactionalWrite(AbstractFileObject file) throws Exception {

		FileName fileName = file.getName();
		String relname = fileName.getBaseName() + ".bak";

		try {
			AbstractFileObject bakFile = (AbstractFileObject) file.getParent().resolveFile(relname);
			// delete old bak
			bakFile.delete();
			file.moveTo(bakFile);
		} catch (Exception ex) {
			// could not rename .bak
		}

		return file.getOutputStream();

	}

}
