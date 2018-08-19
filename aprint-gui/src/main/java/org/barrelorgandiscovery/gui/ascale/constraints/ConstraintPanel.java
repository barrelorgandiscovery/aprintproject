package org.barrelorgandiscovery.gui.ascale.constraints;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractScaleConstraint;
import org.barrelorgandiscovery.scale.ConstraintList;


/**
 * Panel for editing constraints associated to the scale
 * 
 * @author Freydiere Patrice
 * 
 */
public class ConstraintPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 543617557917020743L;

	private static Logger logger = Logger.getLogger(ConstraintPanel.class);

	public ConstraintPanel() {

		initComponents();
	}

	private JList jconstraintList;

	private JComboBox comboAddConstraint;

	private JPanel panelConstraint = null;

	private JSplitPane splitPanel;

	private static class ConstraintItem {

		private AbstractScaleConstraintComponent sc;

		public ConstraintItem(AbstractScaleConstraintComponent sc) {
			this.sc = sc;
		}

		@Override
		public String toString() {
			return sc.getLabel();
		}

		public AbstractScaleConstraintComponent getConstraintComponent() {
			return this.sc;
		}

	}

	/**
	 * Init the internal components of the panel
	 */
	private void initComponents() {

		removeAll();

		setLayout(new BorderLayout());

		DefaultListModel lm = new DefaultListModel();

		jconstraintList = new JList(lm);
		jconstraintList.setBorder(new TitledBorder(Messages.getString("ConstraintPanel.0"))); //$NON-NLS-1$

		jconstraintList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				logger.debug("valuechanged"); //$NON-NLS-1$

				int itemindex = ((JList) e.getSource()).getSelectedIndex();

				logger.debug("index :" + itemindex); //$NON-NLS-1$

				panelConstraint.removeAll();

				if (itemindex < getListModel().size() && itemindex >= 0) {
					ConstraintItem constraintitem = (ConstraintItem) getListModel()
							.getElementAt(itemindex);

					panelConstraint.add(
							constraintitem.getConstraintComponent(),
							BorderLayout.CENTER);

					logger.debug("constraintItem added"); //$NON-NLS-1$

				}

				panelConstraint.invalidate();
				panelConstraint.revalidate();
				panelConstraint.repaint();

			}
		});

		JButton addConstraint = new JButton();

		addConstraint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selectedItem = comboAddConstraint.getSelectedItem();
				if (selectedItem != null) {
					ConstraintItem selconstraint = (ConstraintItem) selectedItem;
					addConstraintItem(selconstraint.getConstraintComponent());
				}
			}
		});

		addConstraint.setIcon(new ImageIcon(getClass().getResource(
				"viewmag+.png"))); //$NON-NLS-1$
		addConstraint.setToolTipText(Messages.getString("ConstraintPanel.5")); //$NON-NLS-1$

		JButton removeConstraint = new JButton();
		removeConstraint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = jconstraintList.getSelectedIndex();
				if (index != -1) {
					logger.debug("remove item at :" + index); //$NON-NLS-1$
					removeConstraintItem(index);
				}
			}
		});

		removeConstraint.setIcon(new ImageIcon(getClass().getResource(
				"cancel.png"))); //$NON-NLS-1$
		removeConstraint.setToolTipText(Messages.getString("ConstraintPanel.8")); //$NON-NLS-1$

		comboAddConstraint = new JComboBox(new DefaultComboBoxModel());

		panelConstraint = new JPanel();
		panelConstraint.setLayout(new BorderLayout());
		panelConstraint.setBorder(new TitledBorder(Messages.getString("ConstraintPanel.9"))); //$NON-NLS-1$

		splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jconstraintList,
				panelConstraint);

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(comboAddConstraint);
		buttonPanel.add(addConstraint);
		buttonPanel.add(removeConstraint);

		add(buttonPanel, BorderLayout.NORTH);
		add(splitPanel, BorderLayout.CENTER);

		splitPanel.setDividerLocation(100);
		revalidate();

		syncComboFromList();
	}

	private void addConstraintItem(AbstractScaleConstraintComponent c) {
		DefaultListModel lm = getListModel();
		c.setConstraintChangedListener(new ConstraintChangedListener() {
			public void constraintChanged(AbstractScaleConstraint constraint) {
				fireConstraintListChanged();
			}
		});

		lm.addElement(new ConstraintItem(c));
		syncComboFromList();
		fireConstraintListChanged();
	}

	private DefaultListModel getListModel() {
		DefaultListModel lm = (DefaultListModel) jconstraintList.getModel();
		return lm;
	}

	private DefaultComboBoxModel getComboModel() {
		DefaultComboBoxModel cm = (DefaultComboBoxModel) comboAddConstraint
				.getModel();
		return cm;
	}

	private void removeConstraintItem(int index) {
		getListModel().removeElementAt(index); // perhaps memory leak ... not
		// implemented, and no consequences
		syncComboFromList();
		fireConstraintListChanged();
	}

	private void syncComboFromList() {

		logger.debug("syncComboFromList"); //$NON-NLS-1$

		AbstractScaleConstraintComponent[] c = ConstraintPanelFactory
				.getAllComponents();
		TreeSet<AbstractScaleConstraintComponent> ts = new TreeSet<AbstractScaleConstraintComponent>(
				new Comparator<AbstractScaleConstraintComponent>() {
					public int compare(AbstractScaleConstraintComponent o1,
							AbstractScaleConstraintComponent o2) {
						return o1.getClass().getName().compareTo(
								o2.getClass().getName());
					}
				});

		for (int i = 0; i < c.length; i++) {
			AbstractScaleConstraintComponent abstractScaleConstraintComponent = c[i];
			ts.add(abstractScaleConstraintComponent);
		}

		DefaultListModel listModel = getListModel();
		for (int i = 0; i < listModel.size(); i++) {

			ConstraintItem ci = (ConstraintItem) listModel.elementAt(i);

			AbstractScaleConstraintComponent constraintComponent = ci
					.getConstraintComponent();

			if (ts.contains(constraintComponent))
				ts.remove(constraintComponent);
		}

		DefaultComboBoxModel comboModel = getComboModel();
		comboModel.removeAllElements();
		for (Iterator<AbstractScaleConstraintComponent> iterator = ts
				.iterator(); iterator.hasNext();) {

			AbstractScaleConstraintComponent abstractScaleConstraintComponent = iterator
					.next();
			logger.debug("Adding " + abstractScaleConstraintComponent); //$NON-NLS-1$
			comboModel.addElement(new ConstraintItem(
					abstractScaleConstraintComponent));
		}

	}

	public ConstraintList getConstraintList() {
		ConstraintList cl = new ConstraintList();

		DefaultListModel listModel = getListModel();

		for (int i = 0; i < listModel.size(); i++) {
			ConstraintItem ci = (ConstraintItem) listModel.elementAt(i);
			AbstractScaleConstraintComponent constraintComponent = ci
					.getConstraintComponent();
			cl.add(constraintComponent.getInstance());
		}
		return cl;
	}

	/**
	 * Defined the modified constraint list for the component ...
	 * 
	 * @param c
	 */
	public void setConstraintList(ConstraintList c) {

		DefaultListModel listModel = getListModel();
		listModel.removeAllElements();

		if (c != null) {
			for (Iterator<AbstractScaleConstraint> iterator = c.iterator(); iterator
					.hasNext();) {
				AbstractScaleConstraint sc = iterator.next();

				// recherche du composant ...
				try {
					AbstractScaleConstraintComponent componentAssociatedToConstraint = ConstraintPanelFactory
							.getComponentAssociatedToConstraint(sc.getClass());

					componentAssociatedToConstraint.load(sc);

					listModel.addElement(new ConstraintItem(
							componentAssociatedToConstraint));

				} catch (Exception ex) {
					logger.error("setConstraintList", ex); //$NON-NLS-1$
				}
			}
		}
		syncComboFromList();

	}

	private ConstraintListChangeListener listener = null;

	public void setConstraintListListener(ConstraintListChangeListener listener) {
		this.listener = listener;
	}

	public ConstraintListChangeListener getConstraintListListener() {
		return this.listener;
	}

	protected void fireConstraintListChanged() {
		if (listener != null) {
			logger.debug("fireConstraintlistChanged"); //$NON-NLS-1$
			listener.constraintListChanged(getConstraintList());
		}
	}

}
