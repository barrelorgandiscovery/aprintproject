package org.barrelorgandiscovery.prefs;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.StringTools;

public class PrefixedNamePrefsStorage implements IPrefsStorage {

	private static Logger logger = Logger
			.getLogger(PrefixedNamePrefsStorage.class);

	private IPrefsStorage ps;
	private String prefix;

	public PrefixedNamePrefsStorage(String domain, IPrefsStorage main) {

		assert main != null;
		assert domain != null;

		this.ps = main;

		this.prefix = domain;
	}

	/**
	 * Construct the property key
	 * 
	 * @param propertyname
	 * @return
	 */
	private String constructKey(String propertyname) {
		return StringTools.convertToPhysicalName(this.prefix, true) + "."
				+ propertyname;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getBooleanProperty(java.
	 * lang.String, boolean)
	 */
	public boolean getBooleanProperty(String propertyname, boolean defaultvalue) {
		return ps.getBooleanProperty(constructKey(propertyname), defaultvalue);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getDoubleProperty(java.lang
	 * .String, double)
	 */
	public double getDoubleProperty(String propertyname, double defaultvalue) {
		return ps.getDoubleProperty(constructKey(propertyname), defaultvalue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getFileProperty(java.lang
	 * .String, java.io.File)
	 */
	public File getFileProperty(String propertyname, File defaultvalue) {
		return ps.getFileProperty(constructKey(propertyname), defaultvalue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getIntegerProperty(java.
	 * lang.String, int)
	 */
	public int getIntegerProperty(String propertyname, int defaultvalue) {
		return ps.getIntegerProperty(constructKey(propertyname), defaultvalue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getStringProperty(java.lang
	 * .String, java.lang.String)
	 */
	public String getStringProperty(String propertyname, String defaultvalue) {
		return ps.getStringProperty(constructKey(propertyname), defaultvalue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.prefs.IPrefsStorage#load()
	 */
	public void load() throws IOException {
		ps.load();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.prefs.IPrefsStorage#save()
	 */
	public void save() {
		ps.save();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setBooleanProperty(java.
	 * lang.String, boolean)
	 */
	public void setBooleanProperty(String propertyname, boolean value) {
		ps.setBooleanProperty(constructKey(propertyname), value);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setDoubleProperty(java.lang
	 * .String, double)
	 */
	public void setDoubleProperty(String propertyname, double value) {
		ps.setDoubleProperty(constructKey(propertyname), value);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setFileProperty(java.lang
	 * .String, java.io.File)
	 */
	public void setFileProperty(String propertyname, File value) {
		ps.setFileProperty(constructKey(propertyname), value);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setIntegerProperty(java.
	 * lang.String, int)
	 */
	public void setIntegerProperty(String propertyname, int value) {
		ps.setIntegerProperty(constructKey(propertyname), value);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setStringProperty(java.lang
	 * .String, java.lang.String)
	 */
	public void setStringProperty(String propertyname, String value) {
		ps.setStringProperty(constructKey(propertyname), value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setDimension(java.lang.String
	 * , java.awt.Dimension)
	 */
	public void setDimension(String propertyname, Dimension dimension) {
		ps.setDimension(constructKey(propertyname), dimension);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getDimension(java.lang.String
	 * )
	 */
	public Dimension getDimension(String propertyName) {
		return ps.getDimension(constructKey(propertyName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getPoint(java.lang.String)
	 */
	public Point getPoint(String propertyName) {
		return ps.getPoint(constructKey(propertyName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setPoint(java.lang.String,
	 * java.awt.Point)
	 */
	public void setPoint(String propertyName, Point point) {
		ps.setPoint(constructKey(propertyName), point);
	}
	
	
}
