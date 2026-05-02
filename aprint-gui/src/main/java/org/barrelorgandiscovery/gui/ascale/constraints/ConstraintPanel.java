package org.barrelorgandiscovery.gui.ascale.constraints;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
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

	private JTextArea emptyHint;

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

		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		DefaultListModel lm = new DefaultListModel();

		jconstraintList = new JList(lm);
		jconstraintList.setVisibleRowCount(5);
		jconstraintList.setBorder(new TitledBorder(
				Messages.getString("ConstraintPanel.0"))); //$NON-NLS-1$

		jconstraintList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				refreshParameterPanel();
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
		addConstraint.setText(Messages.getString("ConstraintPanel.addButton")); //$NON-NLS-1$
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
		removeConstraint.setText(Messages.getString("ConstraintPanel.removeButton")); //$NON-NLS-1$
		removeConstraint.setToolTipText(Messages.getString("ConstraintPanel.8")); //$NON-NLS-1$

		comboAddConstraint = new JComboBox(new DefaultComboBoxModel());
		comboAddConstraint.setPreferredSize(new Dimension(280, 30));
		comboAddConstraint.setToolTipText(Messages.getString("ConstraintPanel.comboTooltip")); //$NON-NLS-1$

		panelConstraint = new JPanel();
		panelConstraint.setLayout(new BorderLayout());
		panelConstraint.setBorder(new TitledBorder(
				Messages.getString("ConstraintPanel.9"))); //$NON-NLS-1$

		emptyHint = new JTextArea();
		emptyHint.setEditable(false);
		emptyHint.setOpaque(false);
		emptyHint.setLineWrap(true);
		emptyHint.setWrapStyleWord(true);
		emptyHint.setFont(emptyHint.getFont().deriveFont(Font.PLAIN,
				emptyHint.getFont().getSize2D()));
		emptyHint.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		JScrollPane listScroll = new JScrollPane(jconstraintList);
		listScroll.setMinimumSize(new Dimension(80, 100));

		splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listScroll,
				panelConstraint);
		splitPanel.setResizeWeight(0.35);
		splitPanel.setDividerSize(9);
		splitPanel.setContinuousLayout(true);
		splitPanel.setOneTouchExpandable(true);
		splitPanel.setBorder(BorderFactory.createEmptyBorder());

		JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		toolbarPanel.add(new JLabel(Messages.getString("ConstraintPanel.addComboLabel"))); //$NON-NLS-1$
		toolbarPanel.add(comboAddConstraint);
		toolbarPanel.add(addConstraint);
		toolbarPanel.add(removeConstraint);

		JPanel helpPanel = buildHelpPanel();

		JPanel northStack = new JPanel(new BorderLayout(0, 6));
		northStack.add(helpPanel, BorderLayout.NORTH);
		northStack.add(toolbarPanel, BorderLayout.SOUTH);

		add(northStack, BorderLayout.NORTH);
		add(splitPanel, BorderLayout.CENTER);

		revalidate();
		syncComboFromList();
		refreshParameterPanel();
	}

	private JPanel buildHelpPanel() {
		StringBuilder full = new StringBuilder();
		full.append(Messages.getString("ConstraintPanel.intro")); //$NON-NLS-1$
		full.append("\n\n"); //$NON-NLS-1$
		full.append(Messages.getString("ConstraintPanel.availableTypesHeader")); //$NON-NLS-1$
		full.append("\n\n"); //$NON-NLS-1$
		AbstractScaleConstraintComponent[] all = ConstraintPanelFactory
				.getAllComponents();
		for (int i = 0; i < all.length; i++) {
			AbstractScaleConstraintComponent c = all[i];
			full.append("\u2022 ").append(c.getLabel()).append(" \u2014 ") //$NON-NLS-1$ //$NON-NLS-2$
					.append(c.getLongDescription());
			if (i < all.length - 1) {
				full.append("\n"); //$NON-NLS-1$
			}
		}

		JTextArea helpText = new JTextArea(full.toString());
		ConstraintSketches.configureWrappingDescription(helpText);
		helpText.setRows(10);

		// Modest default width for layout; the column still stretches with the split.
		final int helpPrefW = 360;
		final int helpPrefH = 200;
		JScrollPane scroll = ConstraintSketches.wrapHelpIntro(helpText, helpPrefW,
				helpPrefH);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.setBorder(BorderFactory.createTitledBorder(
				Messages.getString("ConstraintPanel.helpTitle"))); //$NON-NLS-1$

		JPanel wrap = new JPanel(new BorderLayout());
		wrap.add(scroll, BorderLayout.CENTER);
		return wrap;
	}

	private void refreshParameterPanel() {
		panelConstraint.removeAll();

		DefaultListModel listModel = getListModel();
		int n = listModel.size();
		int sel = jconstraintList.getSelectedIndex();

		if (n == 0) {
			emptyHint.setText(Messages.getString("ConstraintPanel.emptyListHint")); //$NON-NLS-1$
			panelConstraint.add(emptyHint, BorderLayout.CENTER);
		} else if (sel < 0 || sel >= n) {
			emptyHint.setText(Messages.getString("ConstraintPanel.emptySelectionHint")); //$NON-NLS-1$
			panelConstraint.add(emptyHint, BorderLayout.CENTER);
		} else {
			ConstraintItem constraintitem = (ConstraintItem) listModel
					.getElementAt(sel);
			panelConstraint.add(constraintitem.getConstraintComponent(),
					BorderLayout.CENTER);
		}

		panelConstraint.invalidate();
		panelConstraint.revalidate();
		panelConstraint.repaint();
	}

	private void addConstraintItem(AbstractScaleConstraintComponent c) {
		DefaultListModel lm = getListModel();
		c.setConstraintChangedListener(new ConstraintChangedListener() {
			public void constraintChanged(AbstractScaleConstraint constraint) {
				fireConstraintListChanged();
			}
		});

		lm.addElement(new ConstraintItem(c));
		int last = lm.size() - 1;
		jconstraintList.setSelectedIndex(last);
		jconstraintList.ensureIndexIsVisible(last);
		syncComboFromList();
		fireConstraintListChanged();
		refreshParameterPanel();
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
		DefaultListModel lm = getListModel();
		if (lm.size() == 0) {
			jconstraintList.clearSelection();
		} else {
			int newSel = Math.min(index, lm.size() - 1);
			jconstraintList.setSelectedIndex(newSel);
		}
		syncComboFromList();
		fireConstraintListChanged();
		refreshParameterPanel();
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
		if (listModel.size() > 0) {
			jconstraintList.setSelectedIndex(0);
		} else {
			jconstraintList.clearSelection();
		}
		refreshParameterPanel();

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
