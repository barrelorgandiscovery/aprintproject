package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.PipeStop;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.PipeStopListReference;

import com.jeta.forms.components.panel.FormPanel;

public class InstrumentPipeStopDescriptionComponent extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3454343138796776364L;

	private static final Logger logger = Logger
			.getLogger(InstrumentPipeStopDescriptionComponent.class);

	private JTree registertree;

	/** Limits combo preferred width so the organ tab does not force huge minimum size. */
	private static final int TOOLBAR_COMBO_MAX_WIDTH_PX = 168;

	private JComboBox<String> toolbarRegisterGroupCombo;
	private JComboBox<String> toolbarPipeStopCombo;
	private JButton toolbarAddGroupButton;
	private JButton toolbarAddStopButton;
	private JButton toolbarAddStopControlledButton;
	private JButton toolbarRemoveButton;

	class RegisterSetListTreeModel implements TreeModel {

		private PipeStopGroupList rsl = null;

		private Vector<TreeModelListener> listener = new Vector<TreeModelListener>();

		public RegisterSetListTreeModel(PipeStopGroupList rsl) {
			super();
			this.rsl = rsl;
		}

		public void addTreeModelListener(TreeModelListener l) {
			logger.debug("addTreeModelListener " + l); //$NON-NLS-1$
			listener.add(l);
		}

		public void fireNodeChanged(TreePath tp) {

			for (int i = 0; i < listener.size(); i++) {
				TreeModelListener tms = (TreeModelListener) listener.get(i);
				TreeModelEvent tme = new TreeModelEvent(this, tp);
				tms.treeNodesChanged(tme);
			}
			repaint();
		}

		public PipeStopGroupList getRegisterSetList() {
			return rsl;
		}

		public void fireNodeInserted(TreePath tp) {
			logger.debug("fireNodeInserted " + tp); //$NON-NLS-1$
			for (int i = 0; i < listener.size(); i++) {
				TreeModelListener tms = (TreeModelListener) listener.get(i);
				TreeModelEvent tme = new TreeModelEvent(this, tp);
				tms.treeNodesInserted(tme);
			}

		}

		public void fireNodeDeleted(TreePath tp) {

			for (int i = 0; i < listener.size(); i++) {
				TreeModelListener tms = (TreeModelListener) listener.get(i);
				TreeModelEvent tme = new TreeModelEvent(this, tp);
				tms.treeNodesRemoved(tme);
			}
		}

		public void fireTreeStructureChanged(TreePath tp) {

			for (int i = 0; i < listener.size(); i++) {
				TreeModelListener tms = (TreeModelListener) listener.get(i);
				TreeModelEvent tme = new TreeModelEvent(this, tp);
				tms.treeStructureChanged(tme);
			}

			fireRegisterSetListChanged();
		}

		public Object getChild(Object parent, int index) {

			logger.debug("getChild " + parent + " : " + index); //$NON-NLS-1$ //$NON-NLS-2$
			if (parent == null)
				return null;

			if (parent instanceof PipeStopGroupList) {
				PipeStopGroupList rl = (PipeStopGroupList) parent;

				return rl.get(index);
			} else if (parent instanceof PipeStopGroup) {
				PipeStopGroup rs = (PipeStopGroup) parent;
				return rs.getPipeStops()[index];
			}

			return null;
		}

		public int getChildCount(Object parent) {
			logger.debug("get Child Count " + parent); //$NON-NLS-1$
			if (parent instanceof PipeStopGroupList) {
				PipeStopGroupList rl = (PipeStopGroupList) parent;
				return rl.size();
			} else if (parent instanceof PipeStopGroup) {
				PipeStopGroup rs = (PipeStopGroup) parent;
				return rs.getPipeStops().length;
			}
			return 0;
		}

		public int getIndexOfChild(Object parent, Object child) {

			if (parent == null || child == null)
				return -1;

			logger.debug("getIndexOfChild " + parent + " " + child); //$NON-NLS-1$ //$NON-NLS-2$

			if (parent instanceof PipeStopGroupList) {
				PipeStopGroupList rl = (PipeStopGroupList) parent;

				for (int i = 0; i < rl.size(); i++) {
					if (rl.get(i) == child) {
						return i;
					}
				}

			} else if (parent instanceof PipeStopGroup) {
				PipeStopGroup rs = (PipeStopGroup) parent;
				PipeStop[] t = rs.getPipeStops();
				for (int i = 0; i < t.length; i++) {
					if (t[i].equals(child))
						return i;
				}

			}

			logger.debug("getIndexOFChild return -1"); //$NON-NLS-1$
			return -1;
		}

		public Object getRoot() {
			return rsl;
		}

		public boolean isLeaf(Object node) {
			return node instanceof PipeStop;
		}

		public void removeTreeModelListener(TreeModelListener l) {
			listener.remove(l);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {

		}

	}

	/**
	 * Constructeurs
	 */
	public InstrumentPipeStopDescriptionComponent() {
		initComponents();
	}

	/**
	 * initialisation des composants
	 */
	private void initComponents() {

		FormPanel p = null;

		try {
			p = new FormPanel(getClass().getResourceAsStream(
					"instrumentPipeStopDescriptionComponent.jfrm")); //$NON-NLS-1$
		} catch (Exception ex) {
			logger.error("error creating form " + ex.getMessage(), ex); //$NON-NLS-1$
			throw new RuntimeException(
					"error creating form " + ex.getMessage(), ex); //$NON-NLS-1$
		}

		registertree = new JTree();
		// registertree.setShowsRootHandles(false);
		setRegisterSetList(null);

		setLayout(new BorderLayout());

		JPanel organTreePanel = new JPanel(new BorderLayout());
		organTreePanel.add(createOrganCompositionToolbar(), BorderLayout.NORTH);
		organTreePanel.add(new JScrollPane(registertree), BorderLayout.CENTER);

		p.getFormAccessor().replaceBean(
				p.getComponentByName("organcomposition"), organTreePanel); //$NON-NLS-1$


		// Définition du rendu ...
		registertree.setCellRenderer(new TreeCellRenderer() {

			private Hashtable<Object, JLabel> labelhash = new Hashtable<Object, JLabel>();

			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {

				logger.debug("render " + value + " selected : " + selected); //$NON-NLS-1$ //$NON-NLS-2$

				JLabel l = null;

				if (labelhash.contains(value)) {
					l = labelhash.get(value);
				} else {
					l = new JLabel();
				}

				if (selected) {
					l.setFont(l.getFont().deriveFont(Font.BOLD));
				} else {
					l.setFont(l.getFont().deriveFont(Font.PLAIN));
				}

				if (value instanceof PipeStopGroupList) {
					l.setText(Messages.getString("RegisterSetComponent.10")); //$NON-NLS-1$
				} else if (value instanceof PipeStopGroup) {
					l.setText(Messages.getString("RegisterSet." //$NON-NLS-1$
							+ ((PipeStopGroup) value).getName()));
				} else if (value instanceof PipeStop) {

					PipeStop ps = (PipeStop) value;

					String text = Messages
							.getString("Register." + ps.getName()); //$NON-NLS-1$

					if (ps.isRegisteredControlled())
						text += "  " + Messages.getString("InstrumentPipeStopDescriptionComponent.100"); //$NON-NLS-1$ //$NON-NLS-2$

					l.setText(text);
				}

				labelhash.put(value, l);

				return l;
			}
		});

		registertree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				logger.debug("selection changed "); //$NON-NLS-1$
				TreePath tp = registertree.getSelectionPath();
				if (tp != null) {
					logger.debug(registertree.getLastSelectedPathComponent());
				}
				updateOrganCompositionToolbarState();
			}
		});

		registertree.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				handleOrganCompositionPopupTrigger(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				handleOrganCompositionPopupTrigger(e);
			}
		});

		registertree.setShowsRootHandles(true);
		
		JTextArea infos = new JTextArea();
		infos
				.setText(Messages.getString("InstrumentPipeStopDescriptionComponent.101")); //$NON-NLS-1$
		// Without wrapping, one long line forces a huge preferred/min width and pins
		// JSplitPane / tab layouts; wrap so the hint fits narrow columns.
		infos.setLineWrap(true);
		infos.setWrapStyleWord(true);
		infos.setRows(3);
		infos.setColumns(32);
		infos.setMargin(new Insets(4, 4, 4, 4));
		infos.setMinimumSize(new Dimension(0, 0));
		infos.setEnabled(true);
		infos.setEditable(false);
		infos.setFocusable(false);

		p.getFormAccessor().replaceBean(p.getComponentByName("helppanel"), //$NON-NLS-1$
				infos);

		add(p, BorderLayout.CENTER);

	}

	/**
	 * Compact two-row toolbar: capped combo widths and short button labels (full
	 * wording in tooltips) so sibling panels are not forced to a huge minimum
	 * width.
	 */
	private JPanel createOrganCompositionToolbar() {
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
		bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

		JPanel rowGroups = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		JPanel rowStops = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

		PipeStopListReference ref = new PipeStopListReference();
		toolbarRegisterGroupCombo = new JComboBox<String>();
		for (String s : ref.getRegisterSetList()) {
			toolbarRegisterGroupCombo.addItem(s);
		}
		toolbarRegisterGroupCombo.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel c = (JLabel) super.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);
				if (value != null) {
					c.setText(Messages.getString("RegisterSet." + value)); //$NON-NLS-1$
				}
				return c;
			}
		});

		toolbarPipeStopCombo = new JComboBox<String>();
		for (String s : ref.getRegisterList()) {
			toolbarPipeStopCombo.addItem(s);
		}
		toolbarPipeStopCombo.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel c = (JLabel) super.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);
				if (value != null) {
					c.setText(Messages.getString("Register." + value)); //$NON-NLS-1$
				}
				return c;
			}
		});

		toolbarAddGroupButton = new JButton(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarCompactAddGroup")); //$NON-NLS-1$
		toolbarAddStopButton = new JButton(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarCompactAddStop")); //$NON-NLS-1$
		toolbarAddStopControlledButton = new JButton(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarCompactAddControlled")); //$NON-NLS-1$
		toolbarRemoveButton = new JButton(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarCompactRemove")); //$NON-NLS-1$

		toolbarAddGroupButton.setToolTipText(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarAddGroup")); //$NON-NLS-1$
		toolbarAddStopButton.setToolTipText(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarAddStop")); //$NON-NLS-1$
		toolbarAddStopControlledButton.setToolTipText(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarAddControlled")); //$NON-NLS-1$
		toolbarRemoveButton.setToolTipText(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarRemove")); //$NON-NLS-1$

		toolbarRegisterGroupCombo.setToolTipText(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarRegisterGroupLabel") //$NON-NLS-1$
						+ " " //$NON-NLS-1$
						+ Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarComboGroupHint")); //$NON-NLS-1$
		toolbarPipeStopCombo.setToolTipText(
				Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarPipeStopLabel") //$NON-NLS-1$
						+ " " //$NON-NLS-1$
						+ Messages.getString("InstrumentPipeStopDescriptionComponent.toolbarComboStopHint")); //$NON-NLS-1$

		narrowToolbarCombo(toolbarRegisterGroupCombo);
		narrowToolbarCombo(toolbarPipeStopCombo);

		toolbarAddGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addRegisterGroupFromToolbar();
			}
		});
		toolbarAddStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPipeStopFromToolbar(false);
			}
		});
		toolbarAddStopControlledButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPipeStopFromToolbar(true);
			}
		});
		toolbarRemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeOrganNodeFromToolbar();
			}
		});

		rowGroups.add(toolbarRegisterGroupCombo);
		rowGroups.add(toolbarAddGroupButton);
		rowStops.add(toolbarPipeStopCombo);
		rowStops.add(toolbarAddStopButton);
		rowStops.add(toolbarAddStopControlledButton);
		rowStops.add(toolbarRemoveButton);

		bar.add(rowGroups);
		bar.add(rowStops);

		updateOrganCompositionToolbarState();
		return bar;
	}

	private static void narrowToolbarCombo(JComboBox<?> combo) {
		Dimension p = combo.getPreferredSize();
		int w = Math.min(TOOLBAR_COMBO_MAX_WIDTH_PX, Math.max(96, p.width));
		int h = p.height;
		combo.setPreferredSize(new Dimension(w, h));
		combo.setMaximumSize(new Dimension(TOOLBAR_COMBO_MAX_WIDTH_PX, h));
	}

	private void updateOrganCompositionToolbarState() {
		if (toolbarAddGroupButton == null) {
			return;
		}
		TreePath tp = registertree.getSelectionPath();
		Object node = tp == null ? null : tp.getLastPathComponent();

		boolean rootSel = node instanceof PipeStopGroupList;
		boolean groupSel = node instanceof PipeStopGroup;
		boolean stopSel = node instanceof PipeStop;

		toolbarRegisterGroupCombo.setEnabled(rootSel);
		toolbarAddGroupButton.setEnabled(rootSel);

		toolbarPipeStopCombo.setEnabled(groupSel);
		toolbarAddStopButton.setEnabled(groupSel);
		toolbarAddStopControlledButton.setEnabled(groupSel);

		toolbarRemoveButton.setEnabled(groupSel || stopSel);
	}

	private void addRegisterGroupFromToolbar() {
		String registersetname = (String) toolbarRegisterGroupCombo
				.getSelectedItem();
		if (registersetname == null) {
			return;
		}
		TreePath sel = registertree.getSelectionPath();
		if (sel == null || !(sel.getLastPathComponent() instanceof PipeStopGroupList)) {
			RegisterSetListTreeModel m = (RegisterSetListTreeModel) registertree
					.getModel();
			Object root = m.getRoot();
			if (root != null) {
				sel = new TreePath(root);
				registertree.setSelectionPath(sel);
			}
		}
		if (sel != null
				&& sel.getLastPathComponent() instanceof PipeStopGroupList) {
			addRegisterGroup(registersetname, sel);
		}
	}

	private void addPipeStopFromToolbar(boolean controlled) {
		String registername = (String) toolbarPipeStopCombo.getSelectedItem();
		if (registername == null) {
			return;
		}
		TreePath sel = registertree.getSelectionPath();
		if (sel != null && sel.getLastPathComponent() instanceof PipeStopGroup) {
			addPipeStopToGroup((PipeStopGroup) sel.getLastPathComponent(),
					registername, controlled, sel);
		}
	}

	private void removeOrganNodeFromToolbar() {
		TreePath sel = registertree.getSelectionPath();
		if (sel == null) {
			return;
		}
		Object s = sel.getLastPathComponent();
		if (s instanceof PipeStopGroup) {
			deletePipeStopGroup((PipeStopGroup) s, sel);
		} else if (s instanceof PipeStop) {
			deletePipeStop((PipeStop) s, sel);
		}
	}

	private void addRegisterGroup(String registersetname, TreePath sel) {
		PipeStopGroupList l = (PipeStopGroupList) sel.getLastPathComponent();
		PipeStopGroup rs = new PipeStopGroup(registersetname, null);
		l.put(rs);
		((RegisterSetListTreeModel) registertree.getModel())
				.fireTreeStructureChanged(sel);
	}

	private void addPipeStopToGroup(PipeStopGroup rs, String registername,
			boolean controlled, TreePath sel) {
		rs.add(new PipeStop(registername, controlled));
		((RegisterSetListTreeModel) registertree.getModel())
				.fireTreeStructureChanged(sel);
	}

	private void deletePipeStopGroup(PipeStopGroup rs, TreePath sel) {
		PipeStopGroupList rsl = (PipeStopGroupList) sel.getParentPath()
				.getLastPathComponent();
		rsl.remove(rs.getName());
		((RegisterSetListTreeModel) registertree.getModel())
				.fireTreeStructureChanged(sel.getParentPath());
	}

	private void deletePipeStop(PipeStop registername, TreePath sel) {
		PipeStopGroup rs = (PipeStopGroup) sel.getParentPath()
				.getLastPathComponent();
		rs.remove(registername.getName());
		((RegisterSetListTreeModel) registertree.getModel())
				.fireTreeStructureChanged(sel.getParentPath());
	}

	private void handleOrganCompositionPopupTrigger(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showOrganCompositionPopup(e);
		}
	}

	private void showOrganCompositionPopup(MouseEvent e) {
		TreePath pathUnder = registertree.getPathForLocation(e.getX(), e.getY());
		if (pathUnder != null) {
			registertree.setSelectionPath(pathUnder);
		}
		final TreePath sel = registertree.getSelectionPath();
		if (sel == null) {
			return;
		}

		JPopupMenu popup = new JPopupMenu(
				Messages.getString("RegisterSetComponent.15")); //$NON-NLS-1$

		final Object s = sel.getLastPathComponent();
		if (s instanceof PipeStopGroupList) {

			JMenu mi = new JMenu(
					Messages.getString("RegisterSetComponent.16")); //$NON-NLS-1$
			String[] registersetlist = new PipeStopListReference()
					.getRegisterSetList();
			for (int i = 0; i < registersetlist.length; i++) {
				final String registersetname = registersetlist[i];
				JMenuItem it = new JMenuItem(Messages
						.getString("RegisterSet." + registersetname)); //$NON-NLS-1$
				it.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						addRegisterGroup(registersetname, sel);
					}
				});
				mi.add(it);

			}
			popup.add(mi);
		} else if (s instanceof PipeStopGroup) {

			final PipeStopGroup rs = (PipeStopGroup) s;

			JMenu mi = new JMenu(
					Messages.getString("RegisterSetComponent.19")); //$NON-NLS-1$
			String[] registerlist = new PipeStopListReference()
					.getRegisterList();
			for (int i = 0; i < registerlist.length; i++) {
				final String registername = registerlist[i];

				JMenuItem it = new JMenuItem(Messages
						.getString("Register." + registername)); //$NON-NLS-1$
				it.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						addPipeStopToGroup(rs, registername, false, sel);
					}
				});

				mi.add(it);

				JMenuItem itrc = new JMenuItem(
						Messages.getString("Register." + registername) //$NON-NLS-1$
								+ " " //$NON-NLS-1$
								+ Messages.getString("RegisterSetComponent.8")); //$NON-NLS-1$
				itrc.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						addPipeStopToGroup(rs, registername, true, sel);
					}
				});

				mi.add(itrc);

			}
			popup.add(mi);

			JMenuItem midelete = new JMenuItem(
					Messages.getString("RegisterSetComponent.22") //$NON-NLS-1$
							+ " " //$NON-NLS-1$
							+ Messages.getString("RegisterSet." //$NON-NLS-1$
									+ rs.getName()));
			midelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					deletePipeStopGroup(rs, sel);
				}
			});

			popup.add(midelete);

		} else if (s instanceof PipeStop) {

			final PipeStop registername = (PipeStop) s;
			JMenuItem midelete = new JMenuItem(
					Messages.getString("RegisterSetComponent.24") //$NON-NLS-1$
							+ " " //$NON-NLS-1$
							+ Messages.getString("Register." //$NON-NLS-1$
									+ registername.getName()));
			midelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					deletePipeStop(registername, sel);
				}
			});
			popup.add(midelete);
		}

		popup.show((JComponent) e.getSource(), e.getX(), e.getY());
	}

	/**
	 * Redéfini la liste des registres
	 * 
	 * @param newlist
	 */
	public void setRegisterSetList(PipeStopGroupList newlist) {

		if (newlist == null) {
			newlist = new PipeStopGroupList();
		}

		RegisterSetListTreeModel rt = new RegisterSetListTreeModel(newlist);
		registertree.setModel(rt);

		fireRegisterSetListChanged();
		updateOrganCompositionToolbarState();

	}

	/**
	 * Get the component underlying RegisterSetList
	 * 
	 * @return
	 */
	public PipeStopGroupList getRegisterSetList() {
		RegisterSetListTreeModel rt = (RegisterSetListTreeModel) registertree
				.getModel();
		return rt.getRegisterSetList();
	}

	/**
	 * ajoute un écouteur de changement
	 */
	public void addRegisterSetListChangeListener(
			RegisterSetListChangeListener listener) {
		if (listener != null)
			this.listeners.add(listener);
	}

	/**
	 * supprime un écouteur de changement
	 */
	public void removeRegisterSetListChangeListener(
			RegisterSetListChangeListener listener) {
		if (listener != null)
			this.listeners.remove(listener);
	}

	private Vector<RegisterSetListChangeListener> listeners = new Vector<RegisterSetListChangeListener>();

	/**
	 * Déclenche le changement de jeu de registres
	 */
	private void fireRegisterSetListChanged() {
		if (listeners.size() != 0) {
			logger.debug("fireRegisterSetListChanged"); //$NON-NLS-1$
			for (RegisterSetListChangeListener listener : listeners) {
				try {
					listener.registerSetListChanged(getRegisterSetList());
				} catch (Exception ex) {
					logger.error(ex);
				}
			}

		}
	}

}
