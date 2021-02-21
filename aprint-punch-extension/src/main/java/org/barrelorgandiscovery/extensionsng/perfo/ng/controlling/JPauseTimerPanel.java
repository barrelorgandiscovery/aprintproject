package org.barrelorgandiscovery.extensionsng.perfo.ng.controlling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.barrelorgandiscovery.extensionsng.perfo.ng.messages.Messages;
import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.PauseTimerState;
import org.barrelorgandiscovery.tools.SwingUtils;

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
		checkBoxActivate.setText(Messages.getString("JPauseTimerPanel.0")); //$NON-NLS-1$

	
		f.repaint();

		f.getLabel("lblpauseevery") //$NON-NLS-1$
				.setText(Messages.getString("JPauseTimerPanel.1")); //$NON-NLS-1$
		f.getLabel("lblpauseduration") //$NON-NLS-1$
				.setText(Messages.getString("JPauseTimerPanel.2")); //$NON-NLS-1$
		f.getLabel("lblminutes1") //$NON-NLS-1$
				.setText(Messages.getString("JPauseTimerPanel.3")); //$NON-NLS-1$
		f.getLabel("lblminutes2") //$NON-NLS-1$
				.setText(Messages.getString("JPauseTimerPanel.4")); //$NON-NLS-1$

		spinpause = new JSpinner(new SpinnerNumberModel(20.0, 2.0, 300.0, 0.1));
		f.getFormAccessor().replaceBean("spinpause", spinpause); //$NON-NLS-1$

		// configure
		spinduration = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 300.0, 0.1));
		f.getFormAccessor().replaceBean("spinduration", spinduration); //$NON-NLS-1$

		// enable / disable the timer
		checkBoxActivate.getModel().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				boolean isSelected = checkBoxActivate.getModel().isSelected();

				SwingUtils.recurseSetEnable(f, isSelected);
				checkBoxActivate.setEnabled(true);
				f.repaint();

			}
		});
		
		
		setLayout(new BorderLayout());
		add(f, BorderLayout.CENTER);
		
		checkBoxActivate.getModel().setSelected(false);
		
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
