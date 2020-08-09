package org.barrelorgandiscovery.extensionsng.perfo.cad;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.extensions.ExtensionPoint;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DXFDeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DeviceDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.DeviceGraphicLayerDrawing;
import org.barrelorgandiscovery.extensionsng.perfo.cad.canvas.SVGDeviceDrawing;
import org.barrelorgandiscovery.gui.aedit.GraphicsLayer;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.extensionspoints.VirtualBookFrameToolRegister;
import org.barrelorgandiscovery.gui.aprintng.helper.BaseVirtualBookExtension;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.StringTools;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import com.jeta.forms.components.panel.FormPanel;
import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Extension DXF/SVG
 * 
 * @author pfreydiere
 * 
 */
public class CADExporterExtensionVirtualBook extends BaseVirtualBookExtension
		implements ActionListener, VirtualBookFrameToolRegister {

	private static final String SAVECAD_COMMAND = "SAVECAD";
	private static final String SAVESVG_COMMAND = "SAVESVG";
	private static final String SAVEDXF_COMMAND = "SAVEDXF";

	private static final String DXF_EXTENSION = "dxf";
	private static final String SVG_EXTENSION = "svg";

	private static final String DXF_EXTENSION_DOT = "." + DXF_EXTENSION;
	private static final String SVG_EXTENSION_DOT = "." + SVG_EXTENSION;

	private Logger logger = Logger.getLogger(CADExporterExtensionVirtualBook.class);

	private GraphicsLayer graphicLayer;

	public CADExporterExtensionVirtualBook() throws Exception {
		graphicLayer = new GraphicsLayer("Tracé Vectoriel");
		defaultAboutAuthor = "Patrice Freydiere";
		defaultAboutVersion = "2020.7.08";
		createParametersPanel();
	}

	@Override
	public void informCurrentInstrument(Instrument instrument) {
		super.informCurrentInstrument(instrument);
		touchPreference();
	}

	private void touchPreference() {

		if (this.currentInstrument == null) {
			return;
		}

		logger.debug("change preferences to " + getName() + "_" + currentInstrument.getName());
		APrintProperties aprintproperties = application.getProperties();

		this.extensionPreferences = new FilePrefsStorage(new File(aprintproperties.getAprintFolder(),
				StringTools.convertToPhysicalName(getName() + "_" + currentInstrument.getName()) + ".properties"));

		loadParameters();
		// refreshPanel
		propertySheetPanel.readFromObject(dxfParameters);

	}

	@Override
	protected void setupExtensionPoint(List<ExtensionPoint> initExtensionPoints) throws Exception {
		super.setupExtensionPoint(initExtensionPoints);
		initExtensionPoints.add(createExtensionPoint(VirtualBookFrameToolRegister.class));
	}

	public void informCurrentVirtualBook(VirtualBook vb) {
		this.currentVirtualBook = vb;
		touchPreference();
	}

	@Override
	public JToolBar[] addToolBars() {
		return new JToolBar[] { };
	}

	public void actionPerformed(ActionEvent e) {

		String actionCommand = e.getActionCommand();

		if (SAVECAD_COMMAND.equals(actionCommand)) {

			((Frame) this.currentFrame).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				if (this.currentVirtualBook == null) {
					JMessageBox.showMessage(this.currentFrame, "No Virtual book");
					return;
				}

				try {

					APrintFileChooser choose = new APrintFileChooser();

					choose.setFileSelectionMode(APrintFileChooser.FILES_ONLY);
					VFSFileNameExtensionFilter svgFileFilter = new VFSFileNameExtensionFilter("Fichier SVG", "svg");
					choose.setFileFilter(svgFileFilter); //$NON-NLS-1$ //$NON-NLS-2$
					choose.setFileFilter(new VFSFileNameExtensionFilter("Fichier DXF", "dxf")); //$NON-NLS-1$ //$NON-NLS-2$

					if (choose.showSaveDialog((Component) currentFrame) == APrintFileChooser.APPROVE_OPTION) {

						AbstractFileObject savedfile = choose.getSelectedFile();
						if (savedfile == null) {
							JMessageBox.showMessage(currentFrame.getOwnerForDialog(), "pas de fichier sélectionné");
							return;
						}

						DeviceDrawing current = null;

						if (choose.getSelectedFileFilter() == svgFileFilter) {

							savedfile = VFSTools.ensureExtensionIs(savedfile, SVG_EXTENSION);
							Scale scale = currentVirtualBook.getScale();
							double lengthinmm = scale.timeToMM(currentVirtualBook.getLength());

							current = new SVGDeviceDrawing(lengthinmm, scale.getWidth());
						} else {
							
							savedfile = VFSTools.ensureExtensionIs(savedfile, DXF_EXTENSION);
							current = new DXFDeviceDrawing();
						}

						try {
							java.io.OutputStream os = savedfile.getOutputStream();
							try {
								drawToDrawing(current);
								current.write(os, CADVirtualBookExporter.LAYERS);
							} finally {
								os.close();
							}
							JMessageBox.showMessage(currentFrame.getOwnerForDialog(),
									"File " + savedfile.getName() + " saved");

						} catch (Throwable ex) {

							JMessageBox.showMessage(currentFrame.getOwnerForDialog(), "Error saving file " + savedfile);
							logger.error("save", ex);
							BugReporter.sendBugReport();
						}
					}

				} catch (Throwable ex) {

					JMessageBox.showMessage(currentFrame.getOwnerForDialog(), "Error in saving file");
					logger.error("save error:" + ex.getMessage(), ex);

					BugReporter.sendBugReport();

				}
			} finally {
				((Frame) currentFrame).setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	@Override
	public String getName() {
		return "CAD Exporter";
	}

	@Override
	public void addLayers(JVirtualBookScrollableComponent c) {
		c.addLayer(graphicLayer);
	}

	// ////////////////////////////////////////////////////////////////////
	// Panel for Parameters

	private JPanel parameterPanel;

	private CADParameters dxfParameters = new CADParameters();

	private void createParametersPanel() throws Exception {

		FormPanel panel = new FormPanel(getClass().getResourceAsStream("rightpanel.jfrm")); //$NON-NLS-1$

		parameterPanel = panel;
		parameterPanel.setPreferredSize(new Dimension(400, 400));

		JLabel image = panel.getLabel("laser");//$NON-NLS-1$
		image.setIcon(new ImageIcon(getClass().getResource("laserWarning.jpg")));//$NON-NLS-1$
		image.setText("");
		BeanInfo bi = new CADParametersBeanInfo();

		propertySheetPanel = new PropertySheetPanel();
		propertySheetPanel.setDescriptionVisible(true);
		propertySheetPanel.setSortingCategories(true);

		BeanBinder beanBinder = new BeanBinder(dxfParameters, propertySheetPanel, bi);

		panel.getFormAccessor().replaceBean(panel.getLabel("properties"), propertySheetPanel); //$NON-NLS-1$

		visualiser = (JButton) panel.getButton("view");//$NON-NLS-1$

		visualiser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					((Frame) currentFrame).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {

						updateCurrentCADDrawing();
						visualiser.setText("Visualiser");

					} finally {
						((Frame) currentFrame).setCursor(Cursor.getDefaultCursor());

					}
				} catch (Exception ex) {
					JMessageBox.showMessage(currentFrame.getOwnerForDialog(),
							"Erreur dans la creation de la visualisation");
					logger.error(ex.getMessage(), ex);
					BugReporter.sendBugReport();
				}
			}
		});

		JButton exporterDXF = (JButton) panel.getButton("export"); //$NON-NLS-1$
		exporterDXF.setActionCommand(SAVECAD_COMMAND);
		exporterDXF.addActionListener(this);

		parameterPanel.setPreferredSize(new Dimension(200, 400));

		propertySheetPanel.addPropertySheetChangeListener(

				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						visualiser.setText("Rafraichir la \nvisualisation");
					}
				});
	}

	// ////////////////////////////////////////////////////////////////////
	// Methods on the Dxf Extension

	private PropertySheetPanel propertySheetPanel;

	private JButton visualiser;

	private void saveParameters() {
		try {

			IPrefsStorage ps = this.extensionPreferences;

			ps.setDoubleProperty("pasdepontsilrest", //$NON-NLS-1$
					dxfParameters.getPasDePontSiIlReste());

			ps.setDoubleProperty("pont", dxfParameters.getPont());//$NON-NLS-1$

			ps.setDoubleProperty("taillepagepourpliure", //$NON-NLS-1$
					dxfParameters.getTaillePagePourPliure());
			ps.setDoubleProperty("tailletrous", dxfParameters.getTailleTrous());//$NON-NLS-1$

			ps.setStringProperty("type", dxfParameters.getTypeTrous().name());//$NON-NLS-1$

			ps.setStringProperty("typepliure", dxfParameters.getTypePliure()//$NON-NLS-1$
					.name());

			ps.setIntegerProperty("nombrepageavant", //$NON-NLS-1$
					dxfParameters.getNombreDePlisAAjouterAuDebut());

			ps.setDoubleProperty("margindebut", //$NON-NLS-1$
					dxfParameters.getStartBookAdjustementFromBeginning());

			ps.setBooleanProperty("exportpliures", dxfParameters.isExportPliures());//$NON-NLS-1$

		} catch (Throwable t) {
			logger.error("error in saving preferences :" + t.getMessage(), t);//$NON-NLS-1$
		}
	}

	private void loadParameters() {
		try {

			IPrefsStorage ps = this.extensionPreferences;
			ps.load();

			dxfParameters.setPasDePontSiIlReste(
					ps.getDoubleProperty("pasdepontsilrest", dxfParameters.getPasDePontSiIlReste()));//$NON-NLS-1$

			dxfParameters.setPont(ps.getDoubleProperty("pont", //$NON-NLS-1$
					dxfParameters.getPont()));

			dxfParameters.setTaillePagePourPliure(ps.getDoubleProperty("taillepagepourpliure", //$NON-NLS-1$
					dxfParameters.getTaillePagePourPliure()));

			dxfParameters.setTailleTrous(ps.getDoubleProperty("tailletrous", //$NON-NLS-1$
					dxfParameters.getTailleTrous()));

			dxfParameters
					.setTypeTrous(TrouType.valueOf(ps.getStringProperty("type", TrouType.TROUS_RECTANGULAIRES.name())));//$NON-NLS-1$

			dxfParameters.setTypePliure(TypePliure.valueOf(ps.getStringProperty("typepliure", //$NON-NLS-1$
					TypePliure.POINTILLEE.name())));

			dxfParameters.setNombreDePlisAAjouterAuDebut(ps.getIntegerProperty("nombrepageavant", //$NON-NLS-1$
					dxfParameters.getNombreDePlisAAjouterAuDebut()));

			dxfParameters.setStartBookAdjustementFromBeginning(ps.getDoubleProperty("margindebut", dxfParameters //$NON-NLS-1$
					.getStartBookAdjustementFromBeginning()));

			dxfParameters.setExportPliures(ps.getBooleanProperty("exportpliures", dxfParameters.isExportPliures()));//$NON-NLS-1$

		} catch (Exception ex) {
			logger.error("error in loading parameters :" + ex.getMessage(), ex);//$NON-NLS-1$
		}

	}

	private void drawToDrawing(DeviceDrawing deviceDrawing) throws Exception {
		assert currentVirtualBook != null;

		VirtualBook mergedHolesVirtualBook = currentVirtualBook.flattenVirtualBook();
		CADVirtualBookExporter c = new CADVirtualBookExporter();
		c.export(mergedHolesVirtualBook, dxfParameters, deviceDrawing);

	}

	public void updateCurrentCADDrawing() throws Exception {

		logger.debug("update Current CAD Drawing");

		graphicLayer.clear();
		graphicLayer.setStroke(new BasicStroke(0.2f));
		graphicLayer.setColor(Color.black);

		// on exporte dans les deux (CAD + dessin)
		DeviceGraphicLayerDrawing device = new DeviceGraphicLayerDrawing(graphicLayer,
				currentVirtualBook.getScale().getWidth());
		drawToDrawing(device);

		// ensure the layer is visible,
		graphicLayer.setVisible(true);
		// repaint it
		currentFrame.getPianoRoll().repaint();

		// save prefs
		saveParameters();

	}

	public void registerToolWindow(MyDoggyToolWindowManager manager) {

		// Register a Tool.
		ToolWindow tw = manager.registerToolWindow("ExportDXFParameterPanel", // Id //$NON-NLS-1$
				"Parametres DXF", // Title //$NON-NLS-1$
				null, // Icon
				parameterPanel, // Component
				ToolWindowAnchor.RIGHT); // Anchor

	}

}
