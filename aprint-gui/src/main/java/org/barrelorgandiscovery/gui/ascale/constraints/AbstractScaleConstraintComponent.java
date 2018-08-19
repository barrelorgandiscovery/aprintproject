package org.barrelorgandiscovery.gui.ascale.constraints;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractScaleConstraint;


/**
 * All constraint components derived from this abstract class
 * 
 * @author Freydiere Patrice
 * 
 */
public abstract class AbstractScaleConstraintComponent extends JPanel {

	private static Logger logger = Logger
			.getLogger(AbstractScaleConstraintComponent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 3356368600256051295L;

	/**
	 * @return the constraint objet handled by this component
	 */
	public abstract AbstractScaleConstraint getInstance();

	/**
	 * Get a label for the constraint
	 * 
	 * @return
	 */
	public abstract String getLabel();

	/**
	 * Load the content of the component from the contraint object
	 * 
	 * @param constraint
	 * @throws Exception
	 */
	public abstract void load(AbstractScaleConstraint constraint)
			throws Exception;

	/**
	 * this internal method call the listener if defined, this permit the
	 * derived class to fire a message when the contraint component changed
	 */
	protected void fireConstraintChanged() {

		if (listener != null) {
			logger.debug("fireConstraintChanged"); //$NON-NLS-1$
			listener.constraintChanged(getInstance());
		}
	}

	private ConstraintChangedListener listener;

	/**
	 * define the listener for handling constraint changes
	 * 
	 * @param listener
	 */
	public void setConstraintChangedListener(ConstraintChangedListener listener) {
		this.listener = listener;
	}

	/**
	 * get the listener
	 * 
	 * @return
	 */
	public ConstraintChangedListener getConstraintChangedListener() {
		return this.listener;
	}

}
