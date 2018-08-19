package org.barrelorgandiscovery.gui.repository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.editableinstrument.EditableInstrument;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentConstants;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManager;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentManagerRepository2Adapter;
import org.barrelorgandiscovery.editableinstrument.EditableInstrumentStorage;
import org.barrelorgandiscovery.editableinstrument.IEditableInstrument;
import org.barrelorgandiscovery.editableinstrument.StreamStorageEditableInstrumentManager;
import org.barrelorgandiscovery.gaerepositoryclient.GAESynchronizedRepository2;
import org.barrelorgandiscovery.gaerepositoryclient.SynchronizationFeedBack;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchroElement;
import org.barrelorgandiscovery.gaerepositoryclient.synchroreport.SynchronizationReport;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.gaerepositoryclient.GaeConnectionPropertiesPanel;
import org.barrelorgandiscovery.gui.gaerepositoryclient.GaeRepositoryClientConnection;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.httpxmlrepository.HttpXmlRepository;
import org.barrelorgandiscovery.tools.FileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.tools.streamstorage.FolderStreamStorage;
import org.barrelorgandiscovery.tools.streamstorage.StreamStorage;
import org.barrelorgandiscovery.ui.animation.InfiniteProgressPanel;

import com.jeta.forms.components.panel.FormPanel;

/**
 * Form presenting some functionnality associated to a repository ...
 * 
 * @author Freydiere Patrice
 * 
 */
public class JRepositoryForm extends JAbstractRepositoryForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6922466667697395983L;

	private static Logger logger = Logger.getLogger(JRepositoryForm.class);

	/**
	 * Frame owner (main application) for showing modal dialogs ... aso
	 */
	private Object owner;

	/**
	 * The edited repository ...
	 */
	private Repository2 editedRepository;

	private APrintProperties properties;

	public JRepositoryForm(Object owner, Repository2 repository,
			APrintProperties properties) throws Exception {

		super(owner, repository, properties);

		this.owner = owner;
		this.editedRepository = repository;
		this.properties = properties;

		initComponents();
	}

	private void initComponents() throws Exception {

		InputStream resourceAsStream = getClass().getResourceAsStream(
				"repositoryForm.jfrm"); //$NON-NLS-1$
		if (resourceAsStream == null)
			throw new Exception("instrumentForm not found"); //$NON-NLS-1$
		FormPanel fp = new FormPanel(resourceAsStream);

		JButton buttonForImage = new JButton(""); //$NON-NLS-1$

		JTextPane tp = (JTextPane) fp
				.getComponentByName("repositorydescription"); //$NON-NLS-1$

		tp.setEnabled(false);

		if (editedRepository instanceof EditableInstrumentManagerRepository2Adapter) {
			buttonForImage.setIcon(new ImageIcon(getClass().getResource(
					"folder_green.png"))); //$NON-NLS-1$

			if (editedRepository instanceof EditableInstrumentManagerRepository2Adapter) {
				EditableInstrumentManagerRepository2Adapter adapter = (EditableInstrumentManagerRepository2Adapter) editedRepository;

				String description = Messages.getString("JRepositoryForm.5"); //$NON-NLS-1$
				EditableInstrumentManager editableInstrumentManager = adapter
						.getEditableInstrumentManager();
				if (editableInstrumentManager instanceof StreamStorageEditableInstrumentManager) {
					StreamStorageEditableInstrumentManager streamStorageEditableInstrumentManager = (StreamStorageEditableInstrumentManager) editableInstrumentManager;
					StreamStorage streamStorage = streamStorageEditableInstrumentManager
							.getStreamStorage();

					if (streamStorage instanceof FolderStreamStorage) {
						FolderStreamStorage folderStreamStorage = (FolderStreamStorage) streamStorage;
						description += Messages.getString("JRepositoryForm.6") //$NON-NLS-1$
								+ folderStreamStorage.getFolder()
										.getAbsolutePath();
					} else {
						description += Messages.getString("JRepositoryForm.7"); //$NON-NLS-1$
					}

				}

				tp.setText(description);

			}

		} else if (editedRepository instanceof GAESynchronizedRepository2) {
			buttonForImage.setIcon(new ImageIcon(getClass().getResource(
					"network.png"))); //$NON-NLS-1$

			String description = Messages.getString("JRepositoryForm.9"); //$NON-NLS-1$

			tp.setText(description);

		} else if (editedRepository instanceof HttpXmlRepository) {

			buttonForImage.setIcon(new ImageIcon(getClass().getResource(
					"network.png"))); //$NON-NLS-1$
			String description = Messages.getString("JRepositoryForm.10000"); //$NON-NLS-1$

			tp.setText(description);

		} else {
			logger.debug("Unsupported Repository ...."); //$NON-NLS-1$

			buttonForImage.setIcon(new ImageIcon(getClass().getResource(
					"kservices.png"))); //$NON-NLS-1$

			String description = Messages.getString("JRepositoryForm.12"); //$NON-NLS-1$

			tp.setText(description);
		}

		fp.getFormAccessor().replaceBean(fp.getComponentByName("image"), //$NON-NLS-1$
				buttonForImage);

		JToolBar tb = new JToolBar(JToolBar.VERTICAL);
		tb.setFloatable(false);

		if (editedRepository instanceof EditableInstrumentManagerRepository2Adapter
				|| editedRepository instanceof GAESynchronizedRepository2) {

			JButton newInstrument = new JButton(
					Messages.getString("JRepositoryForm.14")); //$NON-NLS-1$
			newInstrument.setIcon(new ImageIcon(getClass().getResource(
					"wizard.png"))); //$NON-NLS-1$

			newInstrument.setHorizontalAlignment(SwingConstants.LEADING);

			newInstrument.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					newInstrument();
				}
			});

			tb.add(newInstrument);

			JButton importInstrument = new JButton(Messages.getString("JRepositoryForm.10001")); //$NON-NLS-1$
			importInstrument.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					importInstrument();
				}
			});

			tb.add(importInstrument);

		}

		if (editedRepository instanceof GAESynchronizedRepository2) {

			GAESynchronizedRepository2 gaer2 = (GAESynchronizedRepository2) editedRepository;

			JButton synchro = new JButton();
			synchro.setText(Messages.getString("JRepositoryForm.18")); //$NON-NLS-1$

			synchro.setIcon(new ImageIcon(getClass().getResource("remote.png"))); //$NON-NLS-1$
			synchro.setToolTipText(Messages.getString("JRepositoryForm.20")); //$NON-NLS-1$
			synchro.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {

						final InfiniteProgressPanel infinitePanel = new InfiniteProgressPanel(
								null, 10, 0.5f, 1);

						if (owner instanceof JFrame) {
							JFrame fowner = (JFrame) owner;
							fowner.setGlassPane(infinitePanel);
							fowner.invalidate();
							fowner.validate();

							infinitePanel.start(Messages
									.getString("JRepositoryForm.21")); //$NON-NLS-1$

							Runnable r = new Runnable() {

								public void run() {
									try {

										GAESynchronizedRepository2 synchronizedRepository2 = (GAESynchronizedRepository2) editedRepository;
										final SynchronizationReport synchronizeReport = synchronizedRepository2
												.synchronizeRepository(new SynchronizationFeedBack() {
													public void inform(
															String message,
															double progress) {
														infinitePanel
																.setText(message);
													}
												});

										SwingUtilities
												.invokeAndWait(new Runnable() {
													public void run() {
														try {

															if (synchronizeReport
																	.size() > 0) {
																logger.debug("synchronization report some errors :"); //$NON-NLS-1$
																StringBuilder sb = new StringBuilder();
																for (Iterator iterator = synchronizeReport
																		.iterator(); iterator
																		.hasNext();) {
																	SynchroElement synchroElement = (SynchroElement) iterator
																			.next();
																	logger.debug("e :" //$NON-NLS-1$
																			+ synchroElement
																					.getMessage());
																	sb.append(
																			synchroElement
																					.getMessage())
																			.append("\n"); //$NON-NLS-1$
																}

																JMessageBox
																		.showMessage(
																				owner,
																				sb.toString());
															}

															infinitePanel
																	.stop();

														} catch (Exception ex) {
															infinitePanel
																	.stop();
															logger.error(
																	ex.getMessage(),
																	ex);
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

						} else {
							throw new Exception(
									"owner is not a frame ... cannot do a wait feedback "); //$NON-NLS-1$
						}

					} catch (Throwable t) {
						logger.error("error in creating the new instrument ... "); //$NON-NLS-1$
						JMessageBox.showMessage(owner,
								Messages.getString("JRepositoryForm.27")); //$NON-NLS-1$
					}
				}
			});

			// handling connection informations ...

			synchro.setHorizontalAlignment(SwingConstants.LEADING);
			tb.add(synchro);
			tb.add(new GaeConnectionPropertiesPanel(properties, gaer2));

		} 
		
		

		Component oldc = fp.getComponentByName("buttonpanel"); //$NON-NLS-1$
		if (oldc == null)
			logger.error("buttonpanel component not found");//$NON-NLS-1$

		logger.debug("replacing the button panel with custom toolbar"); //$NON-NLS-1$
		fp.getFormAccessor().replaceBean(oldc, tb);

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

	}

	/**
	 * 
	 */
	protected void newInstrument() {
		try {

			EditableInstrumentManager em;
			if (editedRepository instanceof EditableInstrumentManagerRepository2Adapter) {
				em = ((EditableInstrumentManagerRepository2Adapter) editedRepository)
						.getEditableInstrumentManager();
			} else {
				em = ((GAESynchronizedRepository2) editedRepository)
						.getEditableInstrumentManager();
			}

			EditableInstrument editableInstrument = new EditableInstrument();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// f.setAlwaysOnTop(true);
			f.setIconImage(APrintNG.getAPrintApplicationIcon());

			JRepositoryInstrumentEditorPanel p = new JRepositoryInstrumentEditorPanel(
					f);

			Container contentPane = f.getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(p, BorderLayout.CENTER);

			f.setSize(1024, 768);
			SwingUtils.center(f);
			p.edit(editableInstrument, null, em);

			f.setVisible(true);

		} catch (Throwable t) {
			logger.error("error in creating the new instrument ... "); //$NON-NLS-1$
			JMessageBox.showMessage(owner,
					Messages.getString("JRepositoryForm.17")); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 */
	protected void importInstrument() {
		try {

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(
					Messages.getString("JRepositoryForm.10002"), //$NON-NLS-1$
					EditableInstrumentConstants.INSTRUMENT_FILE_EXTENSION));

			int sel = fc.showOpenDialog(JRepositoryForm.this);
			if (sel == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();

				logger.debug("opening file " //$NON-NLS-1$
						+ file.getAbsolutePath());

				EditableInstrumentStorage eis = new EditableInstrumentStorage();
				FileInputStream fis = new FileInputStream(file);
				try {
					IEditableInstrument ei = eis.load(fis,
							"importedinstrument");//$NON-NLS-1$
					((EditableInstrumentManagerRepository) editedRepository)
							.getEditableInstrumentManager()
							.saveEditableInstrument(ei);

					logger.debug("instrument imported ...");//$NON-NLS-1$

					JMessageBox.showMessage(owner,
							Messages.getString("JRepositoryForm.10003")); //$NON-NLS-1$

				} finally {
					fis.close();
				}

			}

		} catch (Exception ex) {
			logger.error(
					"error importing instrument :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
			JMessageBox.showMessage(
					owner,
					Messages.getString("JRepositoryForm.10004") //$NON-NLS-1$
							+ ex.getMessage());
		}
	}
}
