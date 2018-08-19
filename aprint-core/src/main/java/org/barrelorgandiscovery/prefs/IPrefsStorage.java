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

	public abstract int getIntegerProperty(String propertyname, int defaultvalue);

	public abstract void setIntegerProperty(String propertyname, int value);

	public abstract void setBooleanProperty(String propertyname, boolean value);

	public abstract File getFileProperty(String propertyname, File defaultvalue);

	public abstract void setFileProperty(String propertyname, File value);

	public abstract String getStringProperty(String propertyname,
			String defaultvalue);

	public abstract void setStringProperty(String propertyname, String value);

	public abstract void setDoubleProperty(String propertyname, double value);

	public abstract double getDoubleProperty(String propertyname,
			double defaultvalue);

	public abstract void setDimension(String propertyname, Dimension dimension);

	public abstract Dimension getDimension(String propertyName);

	public abstract Point getPoint(String propertyName);

	public abstract void setPoint(String propertyName, Point point);

}