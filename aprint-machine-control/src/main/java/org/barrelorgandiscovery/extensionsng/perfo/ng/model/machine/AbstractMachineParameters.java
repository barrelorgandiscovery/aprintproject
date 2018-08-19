package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine;

import java.io.Serializable;

import javax.swing.JPanel;

import org.barrelorgandiscovery.prefs.IPrefsStorage;

/**
 * parameters associated to machine communication
 * 
 * @author pfreydiere
 * 
 */
public abstract class AbstractMachineParameters implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6871470428634369500L;

	private static final String PARAMETERS_CLASS_SUFFIX = "Parameters";


	/**
	 * create the associated machine instance linked to this parameters
	 * 
	 * @return
	 * @throws Exception
	 */
	public AbstractMachine createAssociatedMachineInstance() throws Exception {

		Class<? extends AbstractMachineParameters> parametersClass = getClass();
		String className = parametersClass.getName();
		assert className.endsWith(PARAMETERS_CLASS_SUFFIX);
		String machineClassName = className.substring(0, className.length()
				- PARAMETERS_CLASS_SUFFIX.length());

		return (AbstractMachine) Class.forName(machineClassName).newInstance();

	}

	/**
	 * get the machine name for selecting the element
	 * 
	 * @return
	 */
	public abstract String getLabelName();

	/**
	 * save parmeters to a string
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void saveParameters(IPrefsStorage ps) throws Exception;

	/**
	 * load parameter from string
	 * 
	 * @throws Exception
	 */
	public abstract void loadParameters(IPrefsStorage ps) throws Exception;

}
