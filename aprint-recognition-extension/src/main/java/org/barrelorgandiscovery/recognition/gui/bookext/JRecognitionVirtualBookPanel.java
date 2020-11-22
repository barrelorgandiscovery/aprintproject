package org.barrelorgandiscovery.recognition.gui.bookext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.bookimage.BookImage;
import org.barrelorgandiscovery.bookimage.ZipBookImage;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.CurrentToolChanged;
import org.barrelorgandiscovery.gui.aedit.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.JVirtualBookScrollableComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.images.books.tools.RecognitionTiledImage;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.barrelorgandiscovery.virtualbook.Hole;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

public class JRecognitionVirtualBookPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1619815934643072673L;

	private static Logger logger = Logger.getLogger(JRecognitionVirtualBookPanel.class);

	public JRecognitionVirtualBookPanel() throws Exception {
		initComponents();
	}

	JToggleButton holeCreationToolbtn;
	JToggleButton bookCreationToolbtn;

	
	ImageAndHolesVisualizationLayer backgroundBook = new ImageAndHolesVisualizationLayer();
	
	/**
	 * display the result of the recognitiion
	 */
	ImageAndHolesVisualizationLayer recognitionDisplay = new ImageAndHolesVisualizationLayer();

	/**
	 * display the user input for the model
	 */
	ImageAndHolesVisualizationLayer bookRegionDisplay = new ImageAndHolesVisualizationLayer();
	ImageAndHolesVisualizationLayer holeRegionDisplay = new ImageAndHolesVisualizationLayer();

	JSlider sldtransparencytraining;

	JButton btnlaunchrecognition;
	
	JButton btnloadbookimage;

	private class LayerHoleAdd extends CreationTool {
		public LayerHoleAdd(JEditableVirtualBookComponent virtualBookComponent, ImageAndHolesVisualizationLayer layer)
				throws Exception {
			super(virtualBookComponent, null, null, new CreationTool.CreationToolAction() {
				@Override
				public void handleAction(Hole n, boolean isRemove) {
					ArrayList<Hole> holes = layer.getHoles();
					if (holes == null) {
						holes = new ArrayList<>();
					}
					holes.add(n);
					layer.setHoles(holes);
					virtualBookComponent.repaint();
				}
			});
		}
	}
	
	private class SetBackGroundAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6972644892906915997L;

		public SetBackGroundAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				APrintFileChooser aPrintFileChooser = new APrintFileChooser();
				aPrintFileChooser.addFileFilter(
						new VFSFileNameExtensionFilter("Images PNG", new String[] { "png", "jpg", "jpeg" }));

				aPrintFileChooser.addFileFilter(new VFSFileNameExtensionFilter("Book image",
						new String[] { BookImage.BOOKIMAGE_EXTENSION_WITHOUT_DOT }));

				int returnedValue = aPrintFileChooser.showOpenDialog(virtualBookComponent);
				if (returnedValue == APrintFileChooser.APPROVE_OPTION) {
					AbstractFileObject selectedFile = aPrintFileChooser.getSelectedFile();
					if (selectedFile != null) {
						File f = VFSTools.convertToFile(selectedFile);

						if (f.getName().toLowerCase().endsWith(BookImage.BOOKIMAGE_EXTENSION)) {

							ZipBookImage zbook = new ZipBookImage(f);
							
							backgroundBook.setTiledBackgroundimage(zbook);
						} else if (f.isDirectory()) {

							RecognitionTiledImage tiledImage = new RecognitionTiledImage(
									new File(f.getParentFile(), f.getName()));
							tiledImage.setCurrentImageFamilyDisplay("renormed");

							backgroundBook.setTiledBackgroundimage(tiledImage);
						} else {
							logger.error("cannot display image " + f);
						}
					}
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	LayerHoleAdd holeTool;
	LayerHoleAdd bookTool;

	protected void initComponents() throws Exception {

		// init layers
		bookRegionDisplay.setHolesColor(Color.red);
		holeRegionDisplay.setHolesColor(Color.green);

		InputStream resourceAsStream = getClass().getResourceAsStream("toolspanel.jfrm");
		assert resourceAsStream != null;
		FormPanel fp = new FormPanel(resourceAsStream);

		FormAccessor fa = fp.getFormAccessor();
		holeCreationToolbtn = (JToggleButton) fa.getButton("toolcreatehole");
		assert holeCreationToolbtn != null;
		holeCreationToolbtn.addActionListener((e) -> {
			try {
				if (holeTool == null) {
					holeTool = new LayerHoleAdd(virtualBookComponent, holeRegionDisplay);
				}
				virtualBookComponent.setCurrentTool(holeTool);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		bookCreationToolbtn = (JToggleButton) fa.getButton("toolcreatebook");
		assert bookCreationToolbtn != null;
		bookCreationToolbtn.addActionListener((e) -> {
			try {
				if (bookTool == null)
					bookTool = new LayerHoleAdd(virtualBookComponent, bookRegionDisplay);
				virtualBookComponent.setCurrentTool(bookTool);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});

		sldtransparencytraining = (JSlider) fa.getComponentByName("sldtransparencytraining");
		sldtransparencytraining.addChangeListener((e) -> {

			int transparencyValue = sldtransparencytraining.getValue();
			float t = transparencyValue / 100.0f;

			// add transparency

		});

		btnlaunchrecognition = (JButton) fa.getButton("btnlaunchrecognition");
		btnlaunchrecognition.addActionListener((e) -> {
			try {
				launchRecognition();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});
		
		btnloadbookimage = (JButton)fa.getButton("btnloadbookimage");
		assert btnloadbookimage != null;
		btnloadbookimage.setAction(new SetBackGroundAction("Load BookImage ..."));

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);

	}

	private JEditableVirtualBookComponent virtualBookComponent;

	public void setVirtualBookComponent(JVirtualBookScrollableComponent virtualBookComponent) {
		this.virtualBookComponent = (JEditableVirtualBookComponent) virtualBookComponent;

		this.virtualBookComponent.addCurrentToolChangedListener(new CurrentToolChanged() {
			@Override
			public void currentToolChanged(Tool oldTool, Tool newTool) {
				// un select the tools
				if (oldTool == holeTool) {
					holeCreationToolbtn.setSelected(false);
				}
				if (oldTool == bookTool) {
					bookCreationToolbtn.setSelected(false);
				}

				logger.debug("current tool :" + newTool + " old one :" + oldTool);
			}
		});
	}

	public JVirtualBookScrollableComponent getVirtualBookComponent() {
		return virtualBookComponent;
	}

	private void launchRecognition() {

		
		
	}

}
