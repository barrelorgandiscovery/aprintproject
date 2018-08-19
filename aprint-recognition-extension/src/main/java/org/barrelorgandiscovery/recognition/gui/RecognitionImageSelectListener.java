package org.barrelorgandiscovery.recognition.gui;

/**
 * Listener for image selection
 * 
 * @author pfreydiere
 * 
 */
public interface RecognitionImageSelectListener {

	/**
	 * an image has been selected
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void imageSelected(String name) throws Exception;

}
