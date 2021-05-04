package org.barrelorgandiscovery.prefs;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

public interface IPrefsStorage {

	/**
	 * Cette fonction lit les propriétés stockées dans le fichier de propriétés
	 * 
	 * @throws IOException
	 */
	public abstract void load() throws IOException;

	/**
	 * Sauvegarde les propriétés
	 * 
	 * @throws IOException
	 */
	public abstract void save();

	/**
	 * get a boolean property
	 * 
	 * @param propertyname
	 * @param defaultvalue
	 * @return
	 */
	public abstract boolean getBooleanProperty(String propertyname,
			boolean defaultvalue);

	/**
	 * get an integer
	 * @param propertyname
	 * @param defaultvalue
	 * @return
	 */
	public abstract int getIntegerProperty(String propertyname, int defaultvalue);

	/**
	 * define an integer
	 * @param propertyname
	 * @param value
	 */
	public abstract void setIntegerProperty(String propertyname, int value);

	/**
	 * define a boolean
	 * @param propertyname
	 * @param value
	 */
	public abstract void setBooleanProperty(String propertyname, boolean value);

	/**
	 * get a file property
	 * @param propertyname
	 * @param defaultvalue
	 * @return
	 */
	public abstract File getFileProperty(String propertyname, File defaultvalue);

	/**
	 * define a file property
	 * @param propertyname
	 * @param value
	 */
	public abstract void setFileProperty(String propertyname, File value);

	/**
	 * get a string property
	 * @param propertyname
	 * @param defaultvalue
	 * @return
	 */
	public abstract String getStringProperty(String propertyname,
			String defaultvalue);

	/**
	 * set a string property
	 * @param propertyname
	 * @param value
	 */
	public abstract void setStringProperty(String propertyname, String value);

	/**
	 * set a double property
	 * @param propertyname
	 * @param value
	 */
	public abstract void setDoubleProperty(String propertyname, double value);

	/**
	 * get a double property
	 * @param propertyname
	 * @param defaultvalue
	 * @return
	 */
	public abstract double getDoubleProperty(String propertyname,
			double defaultvalue);

	public abstract void setDimension(String propertyname, Dimension dimension);

	public abstract Dimension getDimension(String propertyName);

	public abstract Point getPoint(String propertyName);

	public abstract void setPoint(String propertyName, Point point);

}