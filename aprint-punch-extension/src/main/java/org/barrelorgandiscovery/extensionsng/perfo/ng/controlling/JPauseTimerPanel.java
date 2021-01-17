package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.awt.BorderLayout;
import java.io.InputStream;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.PauseTimerState;

import com.jeta.forms.components.panel.FormPanel;

public class JPauseTimerPanel extends JPanel implements PauseTimerGetter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6018333381331727948L;

	private JSpinner spinpause;
	private JSpinner spinduration;

	private JCheckBox checkBoxActivate;

	public JPauseTimerPanel() throws Exception {
		initComponents();
	}

	private void initComponents() throws Exception {

		InputStream panelResource = getClass().getResourceAsStream("pausetimerpanel.jfrm");//$NON-NLS-1$

		FormPanel f = new FormPanel(panelResource);

		checkBoxActivate = f.getCheckBox("chkboxActivatePauseTimer"); //$NON-NLS-1$
		checkBoxActivate.setText("Activate Pause During Punch");
		checkBoxActivate.getModel().setSelected(false);

		f.getLabel("lblpauseevery") //$NON-NLS-1$
				.setText("pause every");

		f.getLabel("lblpauseduration") //$NON-NLS-1$
				.setText("pause duration");

		spinpause = new JSpinner(new SpinnerNumberModel(20.0, 2.0, 300.0, 0.1));
		f.getFormAccessor().replaceBean("spinpause", spinpause); //$NON-NLS-1$

		// configure
		spinduration = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 300.0, 0.1));
		f.getFormAccessor().replaceBean("spinduration", spinduration); //$NON-NLS-1$

		setLayout(new BorderLayout());
		add(f, BorderLayout.CENTER);

	}

	/**
	 * construct the timer object , depending on the times configured
	 * 
	 * @return
	 */
	public PauseTimerState getNullOrConstructedTimer() {

		if (!checkBoxActivate.getModel().isSelected()) {
			return null;
		}

		PauseTimerState ts = new PauseTimerState();
		Number spinpausenumber = ((SpinnerNumberModel) spinpause.getModel()).getNumber();

		Number spindurationnumber = ((SpinnerNumberModel) spinduration.getModel()).getNumber();
		ts.setConfiguredPauseTimeInMs((long) (spindurationnumber.doubleValue() * 60.0 * 1000));
		ts.setConfiguredPauseIntervalInMs((long) (spinpausenumber.doubleValue() * 60.0 * 1000));

		return ts;
	}

}
