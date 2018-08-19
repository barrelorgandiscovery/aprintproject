package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;


public class TrackDefComboPane extends AbstractGlobalTrackDefComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -855228647217767346L;

	private static Logger logger = Logger.getLogger(TrackDefComboPane.class);

	private static class AbstractTrackDefComponentDisplayer {

		private AbstractTrackDefComponent c;

		public AbstractTrackDefComponentDisplayer(AbstractTrackDefComponent c) {
			this.c = c;
		}

		@Override
		public String toString() {
			return c.getTitle();
		}

		public AbstractTrackDefComponent getAbstractTrackDefComponent() {
			return c;
		}

	}

	private JComboBox trackDefChoiceCombo;

	private JPanel p;

	public TrackDefComboPane(InstrumentPipeStopDescriptionComponent rsc) {
		initInternal(rsc);
		initComponent();
	}

	private boolean isUserChanged = true;

	private JComponent currentViewedComponent = null;

	@Override
	protected void initComponent() {

		super.initComponent();

		logger.debug("initComponent"); //$NON-NLS-1$

		Vector<AbstractTrackDefComponentDisplayer> v = new Vector<AbstractTrackDefComponentDisplayer>();
		for (int i = 0; i < tdc.size(); i++) {
			AbstractTrackDefComponent c = tdc.get(i);
			v.add(new AbstractTrackDefComponentDisplayer(c));
		}

		trackDefChoiceCombo = new JComboBox(v);
		trackDefChoiceCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				if (!isUserChanged
						|| e.getStateChange() == ItemEvent.DESELECTED)
					return;

				logger.debug("item changed"); //$NON-NLS-1$
				JComboBox tp = (JComboBox) e.getSource();
				if (tp.getSelectedIndex() != -1) {
					logger.debug("sending trackdefinition from ...." //$NON-NLS-1$
							+ (AbstractTrackDefComponentDisplayer) tp
									.getSelectedItem());
					((AbstractTrackDefComponentDisplayer) tp.getSelectedItem())
							.getAbstractTrackDefComponent().sendTrackDef();
					activateCurrentTrackDef(tp.getSelectedIndex());
				}
			}
		});

		p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.NORTH, trackDefChoiceCombo);

		add(BorderLayout.CENTER, p);

		JLabel c = new JLabel();
		c.setPreferredSize(new Dimension(50, 200));
		p.add(BorderLayout.CENTER, c);
		currentViewedComponent = c;
	}

	@Override
	protected void activateCurrentTrackDef(int index) {

		logger.debug("activateCurrentTrackDef " + index); //$NON-NLS-1$

		isUserChanged = false;
		trackDefChoiceCombo.setSelectedIndex(index);
		isUserChanged = true;

		AbstractTrackDefComponentDisplayer itemAt = (AbstractTrackDefComponentDisplayer) trackDefChoiceCombo
				.getItemAt(index);

		AbstractTrackDefComponent comp = itemAt.getAbstractTrackDefComponent();

		if (currentViewedComponent != null)
			p.remove(currentViewedComponent);

		comp.setPreferredSize(new Dimension(50, 200));
		p.add(BorderLayout.CENTER, comp);
		currentViewedComponent = comp;

		// p.revalidate();
		p.invalidate();
		p.validate();
		p.repaint();

	}

}
