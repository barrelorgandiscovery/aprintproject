package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
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

		p.getFormAccessor().replaceBean(
				p.getComponentByName("organcomposition"), registertree); //$NON-NLS-1$


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
			}
		});

		registertree.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);

				if ((e.getButton() & MouseEvent.BUTTON2) != 0) {
					logger.debug("right click"); //$NON-NLS-1$

					final TreePath sel = registertree.getLeadSelectionPath();
					if (sel == null)
						return;

					JPopupMenu popup = new JPopupMenu(Messages
							.getString("RegisterSetComponent.15")); //$NON-NLS-1$
					popup.setVisible(false);

					popup.removeAll();

					final Object s = sel.getLastPathComponent();
					if (s instanceof PipeStopGroupList) {

						JMenu mi = new JMenu(Messages
								.getString("RegisterSetComponent.16")); //$NON-NLS-1$
						String[] registersetlist = new PipeStopListReference()
								.getRegisterSetList();
						for (int i = 0; i < registersetlist.length; i++) {
							final String registersetname = registersetlist[i];
							JMenuItem it = new JMenuItem(Messages
									.getString("RegisterSet." //$NON-NLS-1$
											+ registersetname));
							it.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									logger.debug("add " + registersetname //$NON-NLS-1$
											+ " in root "); //$NON-NLS-1$
									PipeStopGroupList l = (PipeStopGroupList) s;
									PipeStopGroup rs = new PipeStopGroup(
											registersetname, null);
									l.put(rs);

									((RegisterSetListTreeModel) registertree
											.getModel())
											.fireTreeStructureChanged(sel);
								}
							});
							mi.add(it);

						}
						popup.add(mi);
					} else if (s instanceof PipeStopGroup) {

						final PipeStopGroup rs = (PipeStopGroup) s;

						// Proposition de l'affichage
						JMenu mi = new JMenu(Messages
								.getString("RegisterSetComponent.19")); //$NON-NLS-1$
						String[] registerlist = new PipeStopListReference()
								.getRegisterList();
						for (int i = 0; i < registerlist.length; i++) {
							final String registername = registerlist[i];

							JMenuItem it = new JMenuItem(Messages
									.getString("Register." + registername)); //$NON-NLS-1$
							it.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									logger.debug("add " + registername //$NON-NLS-1$
											+ " in root "); //$NON-NLS-1$
									rs.add(new PipeStop(registername, false));

									((RegisterSetListTreeModel) registertree
											.getModel())
											.fireTreeStructureChanged(sel);
								}
							});

							mi.add(it);

							JMenuItem itrc = new JMenuItem(
									Messages
											.getString("Register." + registername) //$NON-NLS-1$
											+ " " + Messages //$NON-NLS-1$
													.getString("RegisterSetComponent.8")); //$NON-NLS-1$
							itrc.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									logger.debug("add " + registername //$NON-NLS-1$
											+ " in root "); //$NON-NLS-1$
									rs.add(new PipeStop(registername, true));

									((RegisterSetListTreeModel) registertree
											.getModel())
											.fireTreeStructureChanged(sel);
								}
							});

							mi.add(itrc);

						}
						popup.add(mi);

						// supression du register set ...
						JMenuItem midelete = new JMenuItem(Messages
								.getString("RegisterSetComponent.22") //$NON-NLS-1$
								+ " " //$NON-NLS-1$
								+ Messages.getString("RegisterSet." //$NON-NLS-1$
										+ rs.getName()));
						midelete.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								logger.debug("delete the register set ... "); //$NON-NLS-1$

								PipeStopGroupList rsl = (PipeStopGroupList) sel
										.getParentPath().getLastPathComponent();
								rsl.remove(rs.getName());

								((RegisterSetListTreeModel) registertree
										.getModel())
										.fireTreeStructureChanged(sel
												.getParentPath());

							}
						});

						popup.add(midelete);

					} else if (s instanceof PipeStop) {

						final PipeStop registername = (PipeStop) s;
						// supression du pipe stop.
						JMenuItem midelete = new JMenuItem(Messages
								.getString("RegisterSetComponent.24") //$NON-NLS-1$
								+ " " //$NON-NLS-1$
								+ Messages.getString("Register." //$NON-NLS-1$
										+ registername.getName()));
						midelete.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								logger.debug("delete the register set ... "); //$NON-NLS-1$

								PipeStopGroup rs = (PipeStopGroup) sel
										.getParentPath().getLastPathComponent();
								rs.remove(registername.getName());

								((RegisterSetListTreeModel) registertree
										.getModel())
										.fireTreeStructureChanged(sel
												.getParentPath());

							}
						});

						popup.add(midelete);

					}

					//					
					// Point p = ((JComponent)e.getSource()).getLocation();
					// p.move(e.getX(), e.getY());
					// popup.setLocation(p);

					popup.show((JComponent) e.getSource(), e.getX(), e.getY());

				}

			}
		});

		registertree.setShowsRootHandles(true);
		
		JTextArea infos = new JTextArea();
		infos
				.setText(Messages.getString("InstrumentPipeStopDescriptionComponent.101")); //$NON-NLS-1$
		infos.setPreferredSize(new Dimension(100, 100));
		infos.setEnabled(true);
		infos.setEditable(false);

		p.getFormAccessor().replaceBean(p.getComponentByName("helppanel"), //$NON-NLS-1$
				infos);

		add(p, BorderLayout.CENTER);

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
