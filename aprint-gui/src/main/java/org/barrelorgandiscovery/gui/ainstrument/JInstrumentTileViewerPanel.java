package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;

import com.jeta.forms.components.panel.FormPanel;

public class JInstrumentTileViewerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8887181747741579271L;

	private static Logger logger = Logger.getLogger(JInstrumentTileViewerPanel.class);

	private InstrumentSelectedListener instrumentSelectedListener;
	private String buttonLabel;
	private Repository2 repository;
	private Instrument currentSelectedInstrument;

	public JInstrumentTileViewerPanel(InstrumentSelectedListener instrumentSelectedListener, String buttonLabel,
			Repository2 repository) throws Exception {

		assert instrumentSelectedListener != null;

		this.instrumentSelectedListener = instrumentSelectedListener;
		this.buttonLabel = buttonLabel;
		this.repository = repository;

		initComponent();
	}

	protected void initComponent() throws Exception {
		InputStream formInputStream = JInstrumentTileViewerPanel.class
				.getResourceAsStream("instrumenttiledchooser.jfrm"); //$NON-NLS-1$
		assert formInputStream != null;
		FormPanel fp = new FormPanel(formInputStream);

		JInstrumentTileViewerWithFilter instrumentChooser = new JInstrumentTileViewerWithFilter(repository);
		fp.getFormAccessor().replaceBean("tile", instrumentChooser); //$NON-NLS-1$

		JButton choose = (JButton) fp.getButton("choose"); //$NON-NLS-1$
		choose.setText(buttonLabel);
		choose.setEnabled(false); // disable the button

		instrumentChooser.setInstrumentSelectedListener(

				new InstrumentSelectedListener() {

					@Override
					public void instrumentSelected(Instrument ins) {
						currentSelectedInstrument = ins;
						choose.setEnabled(true);
					}

					@Override
					public void instrumentDoubleClicked(Instrument ins) {
						currentSelectedInstrument = ins;
						instrumentSelectedListener.instrumentDoubleClicked(ins);
					}
				});

		choose.addActionListener(e -> {
			try {
				if (currentSelectedInstrument == null) {
					logger.warn("bad implementation, this should not happend"); //$NON-NLS-1$
					return;
				}
				instrumentSelectedListener.instrumentSelected(currentSelectedInstrument);
			} catch (Exception ex) {
				logger.error("error while processing the instrument selection event handler :" + ex.getMessage(), //$NON-NLS-1$
						ex);
			}
		});

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);
	}
}
