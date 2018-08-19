package org.barrelorgandiscovery.prefs;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.Serializable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.SerializeTools;

/**
 * Base implementation for preferences storage
 * 
 * @author use
 * 
 */
public abstract class AbstractPrefsStorage implements IPrefsStorage {

	private static Logger logger = Logger.getLogger(AbstractPrefsStorage.class);

	/**
	 * Mémorisation des préférences utilisateur
	 */
	protected Properties userproperties = new Properties();

	public AbstractPrefsStorage() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getBooleanProperty(java.
	 * lang.String, boolean)
	 */
	public boolean getBooleanProperty(String propertyname, boolean defaultvalue) {

		String propvalue = getStringProperty(propertyname, null);
		if (propvalue == null)
			return defaultvalue;

		return Boolean.parseBoolean(propvalue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getIntegerProperty(java.
	 * lang.String, int)
	 */
	public int getIntegerProperty(String propertyname, int defaultvalue) {
		String propvalue = getStringProperty(propertyname, null);
		if (propvalue == null)
			return defaultvalue;

		try {
			return Integer.parseInt(propvalue);

		} catch (NumberFormatException ex) {
			return defaultvalue;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setIntegerProperty(java.
	 * lang.String, int)
	 */
	public void setIntegerProperty(String propertyname, int value) {
		setStringProperty(propertyname, "" + value); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setBooleanProperty(java.
	 * lang.String, boolean)
	 */
	public void setBooleanProperty(String propertyname, boolean value) {
		setStringProperty(propertyname, Boolean.toString(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getFileProperty(java.lang
	 * .String, java.io.File)
	 */
	public File getFileProperty(String propertyname, File defaultvalue) {
		String propvalue = getStringProperty(propertyname, null);
		if (propvalue == null)
			return defaultvalue;

		return new File(propvalue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setFileProperty(java.lang
	 * .String, java.io.File)
	 */
	public void setFileProperty(String propertyname, File value) {
		if (value == null) {
			setStringProperty(propertyname, null);
			save();
			return;
		}
		setStringProperty(propertyname, value.getAbsolutePath());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getStringProperty(java.lang
	 * .String, java.lang.String)
	 */
	public String getStringProperty(String propertyname, String defaultvalue) {

		String propvalue = userproperties.getProperty(propertyname);
		if (propvalue == null) {
			return defaultvalue;
		}
		// sinon
		return propvalue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setStringProperty(java.lang
	 * .String, java.lang.String)
	 */
	public void setStringProperty(String propertyname, String value) {
		if (value == null) {
			userproperties.remove(propertyname);
		} else {
			// définition de la valeur
			userproperties.setProperty(propertyname, value);
			logger.debug("saving properties");
			save();

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#setDoubleProperty(java.lang
	 * .String, double)
	 */
	public void setDoubleProperty(String propertyname, double value) {
		setStringProperty(propertyname, "" + value); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.barrelorgandiscovery.prefs.IPrefsStorage#getDoubleProperty(java.lang
	 * .String, double)
	 */
	public double getDoubleProperty(String propertyname, double defaultvalue) {
		String propvalue = getStringProperty(propertyname, null);
		if (propvalue == null)
			return defaultvalue;

		try {
			return Double.parseDouble(propvalue);

		} catch (NumberFormatException ex) {
			return defaultvalue;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.barrelorgandiscovery.prefs.IPrefsStorage#save()
	 */
	public abstract void save();

	public Dimension getDimension(String propertyName) {
		String sValue = getStringProperty(propertyName, null);
		if (sValue != null) {
			try {
				String[] elements = sValue.split("x");
				if (elements.length > 1) {
					double width = Double.parseDouble(elements[0]);
					double height = Double.parseDouble(elements[1]);
					logger.debug("prefs dimension :" + propertyName + " "
							+ width + "x" + height);
					Dimension d = new Dimension();
					d.setSize(width, height);
					return d;
				}
			} catch (Exception ex) {
				logger.error("error in getting dimension :" + ex.getMessage(), ex);
				return null;
			}
		}
		return null;
	}

	public void setDimension(String propertyname, Dimension dimension) {
		String sDimension = null;
		if (dimension != null && dimension.getWidth() > 0
				&& dimension.getHeight() > 0) {
			sDimension = "" + dimension.getWidth() + "x"
					+ dimension.getHeight();
		}
		setStringProperty(propertyname, sDimension);
	}

	public void setPoint(String propertyName, Point point) {
		String sPosition = null;
		if (point != null) {
			sPosition = "" + point.getX() + "x" + point.getY();
		}
		setStringProperty(propertyName, sPosition);

	}

	public Point getPoint(String propertyName) {
		String sValue = getStringProperty(propertyName, null);
		if (sValue != null) {
			try {
				String[] elements = sValue.split("x");
				if (elements.length > 1) {
					double x = Double.parseDouble(elements[0]);
					double y = Double.parseDouble(elements[1]);
					return new Point((int) x, (int) y);
				}
			} catch (Exception ex) {
				return null;
			}
		}
		return null;
	}
	
}