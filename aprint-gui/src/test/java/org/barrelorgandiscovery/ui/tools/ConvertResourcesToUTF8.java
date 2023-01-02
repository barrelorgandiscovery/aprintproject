package org.barrelorgandiscovery.ui.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import org.junit.Test;

public class ConvertResourcesToUTF8 {

	// @Test
	public void convert() throws Exception {

		File f = new File(
				"/home/use/aprintproject/aprint-core/src/main/java/org/barrelorgandiscovery/messages/messages_fr.properties");
		try (FileInputStream is = new FileInputStream(f)) {
			Properties props = new Properties();
			InputStreamReader reader = new InputStreamReader(is, Charset.forName("ISO-8859-1"));
			props.load(reader);
			reader.close();

			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), Charset.forName("UTF-8"));
			props.store(writer, "");
			writer.close();
		}

	}

}
