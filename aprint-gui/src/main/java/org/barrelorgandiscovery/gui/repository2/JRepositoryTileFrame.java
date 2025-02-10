package org.barrelorgandiscovery.gui.repository2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.gui.ainstrument.InstrumentSelectedListener;
import org.barrelorgandiscovery.gui.ainstrument.JInstrumentTileViewer;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.gui.aprintng.JFrameWaitable;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.tools.JMessageBox;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * Present the repositories and the instruments in a tiled view
 * 
 * @author use
 * 
 */
public class JRepositoryTileFrame extends JFrameWaitable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5376719836414346599L;

	private static Logger logger = Logger.getLogger(JRepositoryTileFrame.class);

	private Repository2 repository;

	/**
	 * Current editor Panel ...
	 */

	private JFrame owner;

	private APrintProperties props;

	private JInstrumentTileViewer tv;

	private JComboBox cbrepository;

	private JButton newbutton;

	private JButton modifybutton;

	private JButton deletebutton;

	private JButton importbutton;

	private JButton exportbutton;

	private JButton duplicateButton;
	
	private JButton properties;

	private JLabel labelrepository;
	
	private JTextField filter;

	public JRepositoryTileFrame(JFrame owner, Repository2 repository,
			APrintProperties props) throws Exception {

		this.repository = repository;
		this.owner = owner;
		this.props = props;

		this.repository
				.addRepositoryChangedListener(new RepositoryChangedListener() {
					public void instrumentsChanged() {
						try {
							loadRepositories();
							checkState();
						} catch (Exception ex) {
							logger.error(ex.getMessage(), ex);
							JMessageBox
									.showError(JRepositoryTileFrame.this, ex);
						}
					}

					public void scalesChanged() {
					}

					public void transformationAndImporterChanged() {
					}
				});

		initComponents();
		loadRepositories();
		checkState();
	}

	private static class RepositoryDisplay {

		public RepositoryDisplay(Repository2 r) {
			this.rep = r;
		}

		private Repository2 rep;

		public Repository2 getRepository() {
			return rep;
		}

		@Override
		public String toString() {
			if (rep == null)
				return Messages.getString("JRepositoryTileFrame.0"); //$NON-NLS-1$
			return rep.getLabel();
		}
	}

	private void addRepository(Repository2 r, Vector<RepositoryDisplay> ret) {
		if (r instanceof Repository2Collection) {
			Repository2Collection rCol = (Repository2Collection) r;
			for (int i = 0; i < rCol.getRepositoryCount(); i++) {
				addRepository(rCol.getRepository(i), ret);
			}
		} else {
			ret.add(new RepositoryDisplay(r));
		}

	}

	private void loadRepositories() throws Exception {

		Vector<RepositoryDisplay> v = new Vector<JRepositoryTileFrame.RepositoryDisplay>();
		v.add(new RepositoryDisplay(null));
		addRepository(repository, v);

		Object selectedItem = cbrepository.getSelectedItem();

		DefaultComboBoxModel model = new DefaultComboBoxModel(v);
		cbrepository.setModel(model);
		cbrepository.setMinimumSize(new Dimension(250,10));

		// replace the selected item
		if (selectedItem != null) {

			assert selectedItem instanceof RepositoryDisplay;
			RepositoryDisplay old = (RepositoryDisplay) selectedItem;
			RepositoryDisplay[] list = v
					.toArray(new RepositoryDisplay[v.size()]);
			for (RepositoryDisplay r : list) {
				if (r.toString().equals(old.toString())) {
					cbrepository.setSelectedItem(r);
					break;
				}
			}

		}

	}

	private class InternalCurrentRepositoryInformations implements
			CurrentRepositoryInformations {

		public Frame getOwner() {
			return JRepositoryTileFrame.this;
		}

		public APrintProperties getAPrintProperties() {
			return props;
		}

		public IAPrintWait getWaitInterface() {
			return JRepositoryTileFrame.this;
		}

		public Instrument getCurrentInstrument() {
			return JRepositoryTileFrame.this.tv.getSelectedInstrument();
		}

		public Repository2 getCurrentInstrumentRepository2() {
			return findRepositoryAssociatedTo(getCurrentInstrument());
		}

		public EditableInstrumentManagerRepository getCurrentInstrumentEditableInstrumentManagerRepository() {
			return findEditableRepositoryAssociatedTo(getCurrentInstrument());
		}

		public EditableInstrumentManagerRepository getCurrentEditableInstrumentManagerRepository() {
			return JRepositoryTileFrame.this
					.getCurrentEditableInstrumentManagerRepository();
		}

		public Repository2 getCurrentRepository2() {
			return JRepositoryTileFrame.this.tv.getCurrentRepository();
		}
	}

	private InternalCurrentRepositoryInformations currentInternalCurrentRepositoryInformations = new InternalCurrentRepositoryInformations();

	protected void initComponents() throws Exception {

		InputStream resourceAsStream = getClass().getResourceAsStream(
				"repositoryframepanel.jfrm"); //$NON-NLS-1$
		if (resourceAsStream == null)
			throw new Exception("instrumentForm not found"); //$NON-NLS-1$
		FormPanel fp = new FormPanel(resourceAsStream);

		logger.debug("get the form components"); //$NON-NLS-1$

		FormAccessor header = fp.getFormAccessor("header");//$NON-NLS-1$
		
		cbrepository = (JComboBox) header.getComponentByName("repository"); //$NON-NLS-1$
		assert cbrepository != null;

		cbrepository.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					RepositoryDisplay r = (RepositoryDisplay) e.getItem();
					if (r.getRepository() == null) {
						tv.changeRepository(repository);
					} else {
						tv.changeRepository(r.getRepository());
					}

					checkState();
				}

			}
		});
		
		filter = (JTextField) fp.getComponentByName("filter");  //$NON-NLS-1$

		newbutton = (JButton) fp.getComponentByName("new"); //$NON-NLS-1$
		assert newbutton != null;
		newbutton.setAction(new NewInstrumentAction(this,
				currentInternalCurrentRepositoryInformations));
		newbutton.setText(Messages.getString("JRepositoryTileFrame.4")); //$NON-NLS-1$
		newbutton.setToolTipText(Messages
				.getString("JRepositoryTileFrame.10000")); //$NON-NLS-1$
		newbutton.setIcon(new ImageIcon(getClass().getResource("filenew.png")));//$NON-NLS-1$

		modifybutton = (JButton) fp.getComponentByName("modify"); //$NON-NLS-1$
		assert modifybutton != null;
		final ModifyInstrumentAction modifyInstrumentAction = new ModifyInstrumentAction(
				this, currentInternalCurrentRepositoryInformations);
		modifybutton.setAction(modifyInstrumentAction);
		modifybutton.setText(Messages.getString("JRepositoryTileFrame.6")); //$NON-NLS-1$
		modifybutton.setFont(modifybutton.getFont().deriveFont(Font.BOLD));
		modifybutton.setIcon(new ImageIcon(getClass().getResource(
				"configure.png")));//$NON-NLS-1$
		modifybutton.setToolTipText(Messages
				.getString("JRepositoryTileFrame.10001")); //$NON-NLS-1$

		
		duplicateButton = (JButton) fp.getComponentByName("duplicate"); //$NON-NLS-1$
		assert duplicateButton != null;
		final DuplicateInstrumentAction duplicateInstrumentAction = new DuplicateInstrumentAction(
				this, currentInternalCurrentRepositoryInformations);
		duplicateButton.setAction(duplicateInstrumentAction);
		duplicateButton.setText("Duplicate ..."); 
		duplicateButton.setIcon(new ImageIcon(getClass().getResource(
				"configure.png")));//$NON-NLS-1$
		duplicateButton.setToolTipText("duplicate selected instrument with a new name"); 
		
		
		deletebutton = (JButton) fp.getComponentByName("delete"); //$NON-NLS-1$
		assert deletebutton != null;
		deletebutton.setAction(new DeleteAction(this,
				currentInternalCurrentRepositoryInformations));
		deletebutton.setText(Messages.getString("JRepositoryTileFrame.8")); //$NON-NLS-1$
		deletebutton
				.setIcon(new ImageIcon(getClass().getResource("cancel.png"))); //$NON-NLS-1$
		deletebutton.setToolTipText(Messages
				.getString("JRepositoryTileFrame.10002")); //$NON-NLS-1$

		importbutton = (JButton) fp.getComponentByName("import"); //$NON-NLS-1$
		assert importbutton != null;
		importbutton.setAction(new ImportAction(this,
				currentInternalCurrentRepositoryInformations));
		importbutton.setText(Messages.getString("JRepositoryTileFrame.10")); //$NON-NLS-1$
		importbutton.setIcon(new ImageIcon(getClass().getResource("undo.png")));//$NON-NLS-1$
		importbutton
				.setToolTipText("import an instrument in the repository (the repository must be writable");//$NON-NLS-1$

		exportbutton = (JButton) fp.getComponentByName("export"); //$NON-NLS-1$
		assert exportbutton != null;
		exportbutton.setAction(new ExportAction(this,
				currentInternalCurrentRepositoryInformations));
		exportbutton.setText(Messages.getString("JRepositoryTileFrame.12")); //$NON-NLS-1$
		exportbutton.setIcon(new ImageIcon(getClass().getResource("redo.png"))); //$NON-NLS-1$
		exportbutton.setToolTipText(Messages
				.getString("JRepositoryTileFrame.10003")); //$NON-NLS-1$

		properties = (JButton) header.getComponentByName("properties"); //$NON-NLS-1$
		assert properties != null;
		properties.setAction(new EditRepositoryProperties(this,
				currentInternalCurrentRepositoryInformations));
		properties.setText(Messages.getString("JRepositoryTileFrame.14")); //$NON-NLS-1$

		labelrepository = (JLabel) header.getComponentByName("labelrepository"); //$NON-NLS-1$
		assert labelrepository != null;
		labelrepository.setText(Messages.getString("JRepositoryTileFrame.16")); //$NON-NLS-1$

		tv = new JInstrumentTileViewer(repository);
		tv.setPreferredSize(new Dimension(600, 500));

		tv.setInstrumentSelectedListener(new InstrumentSelectedListener() {

			public void instrumentSelected(Instrument ins) {
				checkState();
			}
			
			@Override
			public void instrumentDoubleClicked(Instrument ins) {
				checkState();
			}
		});

		// double click
		tv.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					// edit instrument if possible ...
					modifyInstrumentAction.actionPerformed(null);
				}
			}
		});

		fp.getFormAccessor().replaceBean(fp.getComponentByName("tileviewer"), //$NON-NLS-1$
				tv);

		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(fp, BorderLayout.CENTER);

		filter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String sfilter = filter.getText();
				tv.setNameFilter(sfilter);
			}
		});
		
	
	}
	
	public void setFocusOnFilter() {
		// focus on the filter first
		boolean gainFocus = filter.requestFocusInWindow();
		logger.debug(gainFocus);
	}

	/**
	 * Get the selected repository
	 * 
	 * @return null if all has been selected in the combo, or the reference of
	 *         the repository otherwise
	 */
	protected Repository2 getCurrentRepository() {
		return tv.getCurrentRepository();
	}

	/**
	 * Return the current editableInstrmentManagerRepository, if not support the
	 * editable instruments, return null
	 * 
	 * @return
	 */
	protected EditableInstrumentManagerRepository getCurrentEditableInstrumentManagerRepository() {
		Repository2 r = tv.getCurrentRepository();
		if (r instanceof EditableInstrumentManagerRepository)
			return (EditableInstrumentManagerRepository) r;
		return null;
	}

	/**
	 * Check the UI State
	 */
	protected void checkState() {

		newbutton.setEnabled(false);
		modifybutton.setEnabled(false);
		duplicateButton.setEnabled(false);
		
		deletebutton.setEnabled(false);
		importbutton.setEnabled(false);
		exportbutton.setEnabled(false);
		properties.setEnabled(false);

		Instrument selectedInstrument = tv.getSelectedInstrument();
		if (selectedInstrument != null) {
			exportbutton.setEnabled(true);

			if (repository instanceof Repository2Collection) {
				Repository2Collection rc = (Repository2Collection) repository;

				Repository2 ri = rc
						.findRepositoryAssociatedTo(selectedInstrument);

				if (ri instanceof EditableInstrumentManagerRepository) {
					deletebutton.setEnabled(true);
					modifybutton.setEnabled(true);
					
					
				}

			}
			duplicateButton.setEnabled(true);

		}

		Repository2 currentRepository = getCurrentRepository();
		if (currentRepository != null) {
			if (currentRepository instanceof EditableInstrumentManagerRepository) {
				importbutton.setEnabled(true);
				newbutton.setEnabled(true);
				properties.setEnabled(true);
			}
		}

	}

	private Repository2 findRepositoryAssociatedTo(Instrument ins) {
		if (ins == null)
			return null;

		if (repository instanceof Repository2Collection) {
			Repository2Collection c = (Repository2Collection) repository;
			return c.findRepositoryAssociatedTo(ins);

		}
		return null;
	}

	private EditableInstrumentManagerRepository findEditableRepositoryAssociatedTo(
			Instrument ins) {

		Repository2 found = findRepositoryAssociatedTo(ins);

		if (found instanceof EditableInstrumentManagerRepository)
			return (EditableInstrumentManagerRepository) found;

		return null;
	}

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure(new LF5Appender());

		// javax.swing.UIManager
		//				.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel"); //$NON-NLS-1$
		//		LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
		// // LiquidLookAndFeel.setStipples(false);
		// LiquidLookAndFeel.setToolbarFlattedButtons(true);

		Properties p = new Properties();
		p.setProperty("repositorytype", "folder"); //$NON-NLS-1$ //$NON-NLS-2$
		p.setProperty("folder", //$NON-NLS-1$
				"/home/use/aprintstudio/private"); //$NON-NLS-1$

		APrintProperties aprintproperties = new APrintProperties(true);

		Repository2 repository = Repository2Factory.create(p, aprintproperties);

		JFrame f = new JRepositoryTileFrame(null, repository, aprintproperties);

		f.setSize(800, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
