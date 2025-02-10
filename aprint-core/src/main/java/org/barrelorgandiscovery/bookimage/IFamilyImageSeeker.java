package org.barrelorgandiscovery.bookimage;

import java.awt.image.BufferedImage;

/**
 * interface implemented by bookimage to get a specific image
 * 
 * @author pfreydiere
 *
 */
public interface IFamilyImageSeeker {

	/**
	 * image number
	 * 
	 * @return
	 */
	public int getImageCount();

	/**
	 * load an individual image
	 * 
	 * @param imageNumber
	 * @return
	 * @throws Exception
	 */
	public BufferedImage loadImage(int imageNumber) throws Exception;

}