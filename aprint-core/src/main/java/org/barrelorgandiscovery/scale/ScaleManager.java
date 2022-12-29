package org.barrelorgandiscovery.scale;

public interface ScaleManager {

	/**
	 * get the scale names contained in the scale manager.
	 * 
	 * @return
	 */
	public abstract String[] getScaleNames();

	/**
	 * get a scale from its name.
	 * 
	 * @param name
	 * @return
	 */
	public abstract Scale getScale(String name);

	/**
	 * save the scale.
	 * 
	 * @param scale
	 * @throws Exception
	 */
	public void saveScale(Scale scale) throws Exception;

	/**
	 * delete the scale.
	 * 
	 * @param scale
	 * @throws Exception
	 */
	public void deleteScale(Scale scale) throws Exception;

}