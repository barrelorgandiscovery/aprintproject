package org.barrelorgandiscovery.extensionsng.scannerperfo;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.barrelorgandiscovery.tools.ImageTools;

/**
 * this is a utility class for 
 * folder in wich the images are taken
 * @author pfreydiere
 *
 */
public class PerfoScanFolder {

  private static final int MAX_IMAGE_IN_FOLDER = 500;
  private File folder;
  private int count = 0;

  public PerfoScanFolder(File folder) {
    assert folder != null;
    assert folder.exists();
    assert folder.isDirectory();
    this.folder = folder;
    this.count = getImageCount();
  }

  int getImageCount() {
	  for (int i = MAX_IMAGE_IN_FOLDER ; i>0 ; i--) {
		  if (constructImageFile(i).exists()) {
			  return i;
		  }
	  }
	  return 0;
  }
  
  public int addNewImage(Image image) throws Exception {
	int i = this.count++;
	BufferedImage bi = null;
	if (image instanceof BufferedImage) {
		bi = (BufferedImage)image;
	} else {
      bi = ImageTools.loadImage(image);
	}
    ImageIO.write(bi, "JPEG", constructImageFile(i));
    return i;
  }
  
  public String constructImageName(int sequence) {
	  return "scan_image_" + sequence;
  }
  
  public File constructImageFile(int sequence) {
	  return new File(folder, constructImageName(sequence) + ".jpg");
  }
  
  public BufferedImage loadImage(int sequence) throws Exception {
	  return ImageTools.loadImage(constructImageFile(sequence).toURL());
  }
  
}
