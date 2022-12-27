package org.barrelorgandiscovery.gui.ascale;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

/**
 * Class for editing an ordered hash property
 * 
 * @author Freydiere Patrice
 * 
 */
public class PropertyHashEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8053888598644160756L;

	private static Logger logger = Logger.getLogger(PropertyHashEditor.class);

	private JTable table;
	private DefaultTableModel tm;

	public PropertyHashEditor() {

		initComponents();
	}

	private void initComponents() {

		table = new JTable();

		tm = (DefaultTableModel) table.getModel();
		tm.addColumn("Property");
		tm.addColumn("Value");

		table.setModel(tm);

		setLayout(new BorderLayout());
		add(table.getTableHeader(), BorderLayout.NORTH);
		add(table, BorderLayout.CENTER);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if (e.getButton() == MouseEvent.BUTTON3) {
					JPopupMenu p = new JPopupMenu();
					JMenuItem add = new JMenuItem("Ajouter une propriété");
					add.addActionListener(new ActionListener() {
						@SuppressWarnings(value = "unchecked")
						public void actionPerformed(ActionEvent e) {
							tm.addRow(new Vector());
						}
					});

					p.add(add);

					JMenuItem remove = new JMenuItem(
							"Supprimer la propriété sélectionnée");
					remove.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int selectedRow = table.getSelectedRow();
							if (selectedRow != -1) {
								tm.removeRow(selectedRow);
							}
						}
					});
					p.add(remove);

					p.show(PropertyHashEditor.this, e.getX(), e.getY());

				}

			}
		});

		table.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {

				HashMap<String, String> h = constructHashFromModel();

				fireHashChanged(h);
			}
		});

		table.getDefaultEditor(Object.class).addCellEditorListener(
				new CellEditorListener() {
					public void editingCanceled(ChangeEvent e) {
						fireHashChanged(constructHashFromModel());
					}

					public void editingStopped(ChangeEvent e) {
						fireHashChanged(constructHashFromModel());
					}
				});
	}

	private Vector<PropertyHashEditorChangedListener> hl = new Vector<PropertyHashEditorChangedListener>();

	public void addPropertyHashEditorChangedListener(
			PropertyHashEditorChangedListener l) {
		if (l != null)
			hl.add(l);
	}

	public void removePropertyHashEditorChangedListener(
			PropertyHashEditorChangedListener l) {
		if (l != null)
			hl.remove(l);
	}

	@SuppressWarnings(value = "unchecked")
	protected void fireHashChanged(HashMap<String, String> hash) {

		logger.debug("firehash Changed");

		for (Iterator iterator = hl.iterator(); iterator.hasNext();) {
			PropertyHashEditorChangedListener l = (PropertyHashEditorChangedListener) iterator
					.next();
			l.hashChanged(hash);

		}
	}

	@SuppressWarnings(value = "unchecked")
	private HashMap<String, String> constructHashFromModel() {
		HashMap<String, String> h = new HashMap<String, String>();
		Vector rows = tm.getDataVector();
		for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
			Vector columns = (Vector) iterator.next();

			String p = (String) columns.get(0);
			String v = (String) columns.get(1);
			if (p != null && !"".equals(p)) {
				h.put(p, v);
			}
		}
		return h;
	}

	@SuppressWarnings(value = "unchecked")
	public void setHash(Map<String, String> hash) {
		while (tm.getRowCount() > 0)
			tm.removeRow(0);

		for (Iterator iterator = hash.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> e = (Entry<String, String>) iterator.next();

			String[] t = new String[] { e.getKey(), e.getValue() };
			tm.addRow(t);
		}
	}

}
