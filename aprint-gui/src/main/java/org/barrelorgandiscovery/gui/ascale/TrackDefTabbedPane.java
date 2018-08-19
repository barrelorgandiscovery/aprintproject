package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

public class TrackDefTabbedPane extends AbstractGlobalTrackDefComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -684730397817204205L;

	private JTabbedPane tabs;

	static final Logger logger = Logger.getLogger(TrackDefTabbedPane.class);

	public TrackDefTabbedPane(InstrumentPipeStopDescriptionComponent rsc) {

		initInternal(rsc);

		initComponent();
	}

	@Override
	protected void initComponent() {
		tabs = new JTabbedPane();

		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JTabbedPane tp = (JTabbedPane) e.getSource();
				if (tp.getSelectedIndex() != -1)
					((AbstractTrackDefComponent) tp.getSelectedComponent())
							.sendTrackDef();
			}
		});
		super.initComponent();

		add(tabs, BorderLayout.CENTER);
	}

	@Override
	protected void addTrackDefComponentInGui(AbstractTrackDefComponent c) {
		tabs.addTab(c.getTitle(), c);
	}

	@Override
	protected void activateCurrentTrackDef(int index) {
		tabs.setSelectedIndex(index);
	}

}
