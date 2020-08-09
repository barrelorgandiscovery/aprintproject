package org.barrelorgandiscovery.ui.tools;

import java.io.File;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;

public class VFSTools {

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

}
