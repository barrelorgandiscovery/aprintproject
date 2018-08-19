package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;

import com.jeta.forms.components.panel.FormPanel;


public class ScaleChooserFromRepositoryComponent extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4507106887915509644L;

	private Repository2 repository;

	private static Logger logger = Logger
			.getLogger(ScaleChooserFromRepositoryComponent.class);

	public ScaleChooserFromRepositoryComponent(Repository2 repository) {

		assert repository != null;
		this.repository = repository;

		initComponent();
		updateListFromRepository();

		add(p, BorderLayout.CENTER);
	}

	private JList scaleList;
	private FormPanel p;

	private void initComponent() {
		try {

			InputStream is = getClass()
					.getResourceAsStream("scaleChooser.jfrm"); //$NON-NLS-1$
			if (is == null)
				throw new Exception("form not found"); //$NON-NLS-1$
			p = new FormPanel(is);

			scaleList = p.getList("scaleList"); //$NON-NLS-1$

			scaleList.setModel(new DefaultListModel());

			scaleList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					JList l = (JList) e.getSource();
					String v = (String) l.getSelectedValue();

					if (v == null) {
						fireScaleChanged(null);
						return;
					}
					// sinon

					fireScaleChanged(repository.getScale(v));

				}
			});

		} catch (Exception ex) {
			logger.error("panel construction", ex); //$NON-NLS-1$
		}

	}

	private void updateListFromRepository() {

		DefaultListModel m = (DefaultListModel) scaleList.getModel();
		m.clear();

		String[] names = repository.getScaleNames();
		for (int i = 0; i < names.length; i++) {
			String string = names[i];
			m.addElement(string);
		}
	}

	private ArrayList<ScaleChooserSelectionListener> listeners = new ArrayList<ScaleChooserSelectionListener>();

	public void addScaleChooserSelectionListener(
			ScaleChooserSelectionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeScaleChooserSelectionListener(
			ScaleChooserSelectionListener listener) {
		listeners.remove(listener);
	}

	@SuppressWarnings(value="unchecked")
	protected void fireScaleChanged(Scale selectedScale) {
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			ScaleChooserSelectionListener type = (ScaleChooserSelectionListener) iterator
					.next();
			type.selectionChanged(selectedScale);
		}
	}

}
