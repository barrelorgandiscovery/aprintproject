package org.barrelorgandiscovery.bookimage;

import java.awt.image.BufferedImage;

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