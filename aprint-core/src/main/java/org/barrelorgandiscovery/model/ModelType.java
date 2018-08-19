package org.barrelorgandiscovery.model;



public interface ModelType {

	/**
	 * model type description
	 * @return
	 */
	String getDescription();

	/**
	 * does this parameter is assignable from this type
	 * 
	 * @param type
	 * @return
	 */
	boolean isAssignableFrom(ModelType type);

	/**
	 * Safe serialization in string
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * get i18n name
	 * @return
	 */
	String getLabel();

	/**
	 * Check if the value belong to this type instance
	 * 
	 * @param value
	 * @return
	 */
	boolean doesValueBelongToThisType(Object value);

	/**
	 * Permit serialize the form of the object for loading
	 * 
	 * @return
	 */
	String serializedForm() throws Exception;

}
