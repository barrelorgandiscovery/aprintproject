package org.barrelorgandiscovery.gui.ascale.constraints;

import java.awt.BorderLayout;
import java.io.InputStream;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractScaleConstraint;
import org.barrelorgandiscovery.scale.ConstraintMinimumInterHoleLength;

import com.jeta.forms.components.panel.FormPanel;


public class MinimumInnerHoleLengthComponent extends AbstractScaleConstraintComponent {

	private JSpinner spinnerMinimum;

	private static Logger logger = Logger
			.getLogger(MinimumHoleLengthConstraintComponent.class);

	public MinimumInnerHoleLengthComponent() {
		FormPanel p = null;
		// load the form ...
		try {

			InputStream is = getClass().getResourceAsStream(
					"minimuminterholelength.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			p = new FormPanel(is);

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
		}

		if (p == null)
			return;

		this.spinnerMinimum = p.getSpinner("spinnerMinimumValue"); //$NON-NLS-1$
		this.spinnerMinimum.setModel(new SpinnerNumberModel(3.0, 0.0, 500.0,
				1.0));
		spinnerMinimum.setEditor(new JSpinner.NumberEditor(
				spinnerMinimum, "0.00")); //$NON-NLS-1$
		spinnerMinimum.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				fireConstraintChanged();
			}});


		setLayout(new BorderLayout());
		add(p, BorderLayout.CENTER);
	}

	@Override
	public AbstractScaleConstraint getInstance() {
		double min = ((Number) spinnerMinimum.getValue()).doubleValue();
		return new ConstraintMinimumInterHoleLength(min);
	}

	@Override
	public String getLabel() {
		return Messages.getString("MinimumInnerHoleLengthComponent.5"); //$NON-NLS-1$
	}

	@Override
	public void load(AbstractScaleConstraint constraint) throws Exception {
		ConstraintMinimumInterHoleLength cminimum = (ConstraintMinimumInterHoleLength) constraint;
		spinnerMinimum
				.setValue(new Double(cminimum.getMinimumInterHoleLength()));

	}

}
