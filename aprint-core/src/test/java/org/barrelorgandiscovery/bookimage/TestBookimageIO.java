package org.barrelorgandiscovery.bookimage;

import java.awt.image.BufferedImage;
import java.io.File;

import org.barrelorgandiscovery.tools.ImageTools;
import org.junit.Test;

public class TestBookimageIO {

	@Test
	public void testWriteLargeImageInBookImage() throws Exception {
		
		File file = new File("/home/use/aprint_contributions/reginald/Ampico 57135 Concert Waltz.tif");
		BufferedImage bi = ImageTools.loadImageWithIO(file);
		File destinationBookImage = new File(file.getParentFile(), file.getName() + ".zip");
		BookImageIO.createBookImage(bi,  destinationBookImage, true, null, null);
		
	}
	
	
}
