package org.barrelorgandiscovery.gui.imagetests;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Test;

public class TestReadTiffs {

	//@Test
	public void testReadTiff() throws Exception {
		
		File f = new File("/home/use/aprint_contributions/reginald/Ampico 57135 Concert Waltz.tif");
		assert f.exists();
		
		// available formats in image IO
		for (String format : ImageIO.getWriterFormatNames()) {
		    System.out.println("format = " + format);
		}
		
		
		
		BufferedImage result = ImageIO.read(f);
		
		System.out.println(result);
		
	}
	
}
