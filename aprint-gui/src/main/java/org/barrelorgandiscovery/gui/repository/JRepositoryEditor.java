package org.barrelorgandiscovery.gui.repository;

import java.awt.BorderLayout;

import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.gaerepositoryclient.AbstractEditableInstrumentRepository;

import org.barrelorgandiscovery.gui.ainstrument.RepositoryTreeListener;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.ascale.ScaleComponent;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.FilteredRepositoryCollection;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.ui.animation.InfiniteProgressPanel;

import com.birosoft.liquid.LiquidLookAndFeel;

public class JRepositoryEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6280065610034091340L;

	private static Logger logger = Logger.getLogger(JRepositoryEditor.class);

	private Repository2 repository;

	/**
	 * Current editor Panel ...
	 */
	private JComponent editorPanel;

	private JPanel editorContainerPanel;

	private JFrame owner;

	private APrintProperties props;

	public JRepositoryEditor(JFrame owner, Repository2 repository,
			APrintProperties props) throws Exception {
		this.repository = repository;
		this.owner = owner;
		this.props = props;

		editorContainerPanel = new JPanel();
		editorContainerPanel.setLayout(new BorderLayout());

		this.editorPanel = null;

		initComponents();
	}

	private void initComponents() throws Exception {

		JRepositoryTree tree = new JRepositoryTree();
		tree.setRepository(repository);

		tree.setRepositoryTreeListener(new RepositoryTreeListener() {
			public void repositoryObjectSelected(Object object) {
				try {

					logger.debug("selected object : " + object); //$NON-NLS-1$

					if (object == null)
						return;

					logger.debug("object type :" + object.getClass()); //$NON-NLS-1$

					changeEditedObject(object);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});

		setLayout(new BorderLayout());

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tree, editorContainerPanel);

		add(splitPane, BorderLayout.CENTER);

		changeContentPane(new JPanel());

	}

	protected void changeContentPane(JComponent newPanel) {

		if (editorPanel != null) {
			// ask the user if he is sure to change the panel

			editorContainerPanel.remove(editorPanel);

		}
		editorPanel = newPanel;
		editorContainerPanel.add(editorPanel, BorderLayout.CENTER);

		invalidate();
		validate();
		repaint();

	}

	protected void changeEditedObject(Object selectedObject) throws Exception {

		if (logger.isDebugEnabled()) {

			logger.debug("selected object :" + selectedObject); //$NON-NLS-1$
			if (selectedObject != null) {
				logger.debug("object class :" //$NON-NLS-1$
						+ selectedObject.getClass().getName());
			}

		}

		if (selectedObject instanceof Scale) {

			Scale selectedScale = (Scale) selectedObject;
			showScale(selectedScale);

		} else if (selectedObject instanceof Instrument) {
			Instrument ins = (Instrument) selectedObject;

			showInstrument(ins);

		} else if (selectedObject instanceof Repository2) {
			Repository2 r2 = (Repository2) selectedObject;
			showRepository(r2);
		}
	}

	/**
	 * Show the repository informations associated to it !!
	 * 
	 * @param r
	 * @throws Exception
	 */
	private void showRepository(Repository2 r) throws Exception {

		logger.debug("show repository for :" + r);
		JAbstractRepositoryForm repositoryForm = new RepositoryGUIFormFactory()
				.createAssociatedForm(owner, r, props);

		changeContentPane(repositoryForm);

	}

	private void showScale(Scale selectedScale) {
		ScaleComponent sc = new ScaleComponent();
		sc.setSpeedDraw(true);
		sc.loadScale(selectedScale);
		JScrollPane sp = new JScrollPane(sc,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		//
		// Dimension size = sc.getPreferredSize();
		// sc.setSize(size);
		//
		// sp.add(sc);
		sp.setAutoscrolls(true);

		changeContentPane(sp);

	}

	private void showInstrument(Instrument ins) throws Exception {
		Repository2Collection or = null;

		logger.debug("repository class :" + repository.getClass().getName()); //$NON-NLS-1$

		if (repository instanceof FilteredRepositoryCollection) {
			FilteredRepositoryCollection frc = (FilteredRepositoryCollection) repository;
			or = frc.getRepository();
		} else if (repository instanceof Repository2Collection) {
			or = (Repository2Collection) repository;
		}

		if (or == null)
			return;

		Repository2 ras = or.findRepositoryAssociatedTo(ins);
		logger.debug("ras : " + ras); //$NON-NLS-1$

		logger.debug("loading instrument .... "); //$NON-NLS-1$

		EditableInstrumentManager em = null;
		String editableInstrumentName = null;

		if (ras instanceof EditableInstrumentManagerRepository2Adapter) {
			EditableInstrumentManagerRepository2Adapter eeras = (EditableInstrumentManagerRepository2Adapter) ras;
			em = (eeras).getEditableInstrumentManager();
			editableInstrumentName = eeras.findAssociatedEditableInstrument(ins
					.getName());

		} else if (ras instanceof AbstractEditableInstrumentRepository) {
			AbstractEditableInstrumentRepository gaeras = (AbstractEditableInstrumentRepository) ras;
			em = (gaeras).getEditableInstrumentManager();
			editableInstrumentName = gaeras.findEditableInstrumentName(ins
					.getName());
		}

		if (em == null) {
			// not editable ...
			// showing the default instrument displaying ...
			JInstrumentForm f = new JInstrumentForm();
			f.setInstrument(ins);
			changeContentPane(f);

			return;
		}
		logger.debug("loading instrument :" + ins.getName()); //$NON-NLS-1$

		if (editableInstrumentName == null) {
			logger.warn("fail to find associated name to " + ins.getName()); //$NON-NLS-1$
			return;
		}

		final InfiniteProgressPanel infinitePanel = new InfiniteProgressPanel(
				null, 10, 0.5f, 1);
		owner.setGlassPane(infinitePanel);
		owner.invalidate();
		owner.validate();

		infinitePanel.start(Messages.getString("JRepositoryEditor.9")); //$NON-NLS-1$

		final EditableInstrumentManager finalem = em;
		final String finalEditableInstrumentName = editableInstrumentName;

		Runnable r = new Runnable() {

			public void run() {
				try {
					final IEditableInstrument loadEditableInstrument = finalem
							.loadEditableInstrument(finalEditableInstrumentName);

					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							try {
								JRepositoryInstrumentEditorPanel p = new JRepositoryInstrumentEditorPanel(
										owner);

								p.edit(loadEditableInstrument,
										finalEditableInstrumentName, finalem);

								changeContentPane(p);

								infinitePanel.stop();

							} catch (Exception ex) {
								infinitePanel.stop();
								logger.error(ex.getMessage(), ex);
							}
						}
					});

				} catch (Throwable t) {
					infinitePanel.stop();
					logger.error(t.getMessage(), t);
				}
			}
		};

		new Thread(r).start();
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		javax.swing.UIManager
				.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel"); //$NON-NLS-1$
		LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
		
		// LiquidLookAndFeel.setStipples(false);
		LiquidLookAndFeel.setToolbarFlattedButtons(true);

		Properties p = new Properties();
		p.setProperty("repositorytype", "folder"); //$NON-NLS-1$ //$NON-NLS-2$
		p.setProperty("folder", //$NON-NLS-1$
				"C:\\Documents and Settings\\Freydiere Patrice\\workspace\\APrint\\gammes"); //$NON-NLS-1$

		APrintProperties aprintproperties = new APrintProperties(true);

		Repository2 repository = Repository2Factory.create(p, aprintproperties);

		JFrame f = new JFrame();
		JRepositoryEditor re = new JRepositoryEditor(f, repository,
				aprintproperties);

		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(re, BorderLayout.CENTER);

		f.setSize(800, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}
