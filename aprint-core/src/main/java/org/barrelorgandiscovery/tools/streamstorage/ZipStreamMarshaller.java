package org.barrelorgandiscovery.tools.streamstorage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.barrelorgandiscovery.tools.StreamsTools;


public class ZipStreamMarshaller implements StreamStorageMarshaller {

	public void pack(StreamStorage inStorage, OutputStream os) throws Exception {

		ZipOutputStream zos = new ZipOutputStream(os);

		String[] types = inStorage.listTypes();
		for (int i = 0; i < types.length; i++) {
			String type = types[i];

			String[] streamsToSave = inStorage.listStreams(type);
			for (int j = 0; j < streamsToSave.length; j++) {
				String stream = streamsToSave[j];

				zos.putNextEntry(new ZipEntry(stream));

				InputStream is = inStorage.openStream(stream);
				StreamsTools.copyStream(is, zos);
				is.close();
				zos.closeEntry();
				
			}

		}
		zos.close();

	}

	public void unpack(InputStream is, StreamStorage outStorage) throws Exception{

		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry nextEntry = zis.getNextEntry();
		while (nextEntry != null)
		{
			String name = nextEntry.getName();
			int pos = name.lastIndexOf(".");
			if (pos != -1)
			{
				String namewithoutType = name.substring(0, pos);
				String type = name.substring(pos + 1);
				outStorage.saveStream(namewithoutType, type, zis);
			}
			 nextEntry = zis.getNextEntry();
		}
		
	}

}
