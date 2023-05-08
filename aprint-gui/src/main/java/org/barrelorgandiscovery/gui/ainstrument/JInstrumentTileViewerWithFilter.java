package org.barrelorgandiscovery.gui.ainstrument;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.repository.Repository2;

public class JInstrumentTileViewerWithFilter extends JPanel {

	private JInstrumentTileViewer instrumentTileViewer;

	private Repository2 repository;

	private JTextField filter;

	public JInstrumentTileViewerWithFilter(Repository2 repository) throws Exception {
		super();

		this.repository = repository;

		initComponents();

	}

	protected void initComponents() throws Exception {
		setLayout(new BorderLayout());
		this.instrumentTileViewer = new JInstrumentTileViewer(repository);

		this.filter = new JTextField();

		add(filter, BorderLayout.NORTH);
		add(instrumentTileViewer, BorderLayout.CENTER);

		filter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String sfilter = filter.getText();
				instrumentTileViewer.setNameFilter(sfilter);
			}
		});

		instrumentTileViewer.setInstrumentSelectedListener(new InstrumentSelectedListener() {
			
			@Override
			public void instrumentSelected(Instrument ins) {
				if (instrumentSelectedListener != null) {
					instrumentSelectedListener.instrumentSelected(ins);
				}
			}

			@Override
			public void instrumentDoubleClicked(Instrument ins) {
				if (instrumentSelectedListener != null) {
					instrumentSelectedListener.instrumentDoubleClicked(ins);
				}
			}
		});
	}

	InstrumentSelectedListener instrumentSelectedListener;

	public void setInstrumentSelectedListener(InstrumentSelectedListener instrumentSelectedListener) {
		this.instrumentSelectedListener = instrumentSelectedListener;
	}

}
