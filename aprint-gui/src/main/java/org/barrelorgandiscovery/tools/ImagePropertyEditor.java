package org.barrelorgandiscovery.tools;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.beans.editor.FilePropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;
import com.l2fprod.common.swing.UserPreferences;
import com.l2fprod.common.util.ResourceManager;

/**
 * Property editor for images
 * 
 * @author use
 * 
 */
public class ImagePropertyEditor extends AbstractPropertyEditor {

	protected JLabel imageLabel;
	private JButton button;
	private JButton cancelButton;

	private Image image = null;

	public ImagePropertyEditor() {
		this(true);
	}

	public ImagePropertyEditor(boolean asTableEditor) {
		editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0)) {
			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				imageLabel.setEnabled(enabled);
				button.setEnabled(enabled);
				cancelButton.setEnabled(enabled);
			}
		};
		((JPanel) editor).add("*", imageLabel = new JLabel());
		((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		((JPanel) editor).add(cancelButton = ComponentFactory.Helper.getFactory().createMiniButton());
		cancelButton.setText("X");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNull();
			}
		});

	}

	private void changeLabelImageValue(Image im) {
		if (im == null) {
			imageLabel.setText("<Null>");
			imageLabel.setIcon(null);
		} else {
			imageLabel.setText(null);
			BufferedImage bufimage;
			try {
				bufimage = ImageTools.crop(30, 30, im);
			} catch (Exception ex) {
				bufimage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
			}
			imageLabel.setIcon(new ImageIcon(bufimage));
		}
	}

	public Object getValue() {
		return image;
	}

	public void setValue(Object value) {
		if (value instanceof Image) {
			changeLabelImageValue((Image) value);
			image = (Image) value;
		} else {
			image = null;
			changeLabelImageValue(image);
		}
	}

	protected void selectFile() {
		ResourceManager rm = ResourceManager.all(FilePropertyEditor.class);

		APrintFileChooser chooser = new APrintFileChooser();
		chooser.setDialogTitle(rm.getString("FilePropertyEditor.dialogTitle"));
		chooser.setApproveButtonText(rm.getString("FilePropertyEditor.approveButtonText"));
		chooser.setApproveButtonMnemonic(rm.getChar("FilePropertyEditor.approveButtonMnemonic"));
		customizeFileChooser(chooser);

		if (APrintFileChooser.APPROVE_OPTION == chooser.showOpenDialog(editor)) {

			AbstractFileObject afo = chooser.getSelectedFile();
			try {
				File newFile = VFSTools.convertToFile(afo);

				Image currentImage = Toolkit.getDefaultToolkit().createImage(newFile.getAbsolutePath());
				changeLabelImageValue(currentImage);
				Image oldImage = (Image) getValue();
				image = currentImage;
				firePropertyChange(oldImage, newFile);
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
	}

	@Override
	public String getAsText() {
		if (image == null)
			return "<Null>";

		assert image != null;

		return "Image " + image.getWidth(null) + "x" + image.getHeight(null);
	}

	/**
	 * Placeholder for subclasses to customize the JFileChooser shown to select a
	 * file.
	 * 
	 * @param chooser
	 */
	protected void customizeFileChooser(APrintFileChooser chooser) {

		chooser.setFileFilter(new VFSFileNameExtensionFilter("Image", new String[] { "jpg", "png" }));

	}

	protected void selectNull() {
		Object oldImage = getValue();
		image = null;
		changeLabelImageValue(image);
		firePropertyChange(oldImage, null);
	}

	public static void main(String[] args) {

		BasicConfigurator.configure(new LF5Appender());

		JFrame f = new JFrame();
		PropertySheetPanel p = new PropertySheetPanel();
		p.setDescriptionVisible(true);
		p.setSortingCategories(true);

		DefaultProperty defaultProperty = new DefaultProperty();
		defaultProperty.setType(Image.class);
		defaultProperty.setEditable(true);

		defaultProperty.setDisplayName("File ... ");

		PropertyEditorRegistry pr = new PropertyEditorRegistry();
		// pr.registerDefaults();
		pr.registerEditor(Image.class, ImagePropertyEditor.class);

		PropertyRendererRegistry propertyRendererRegistry = new PropertyRendererRegistry();
		propertyRendererRegistry.registerRenderer(Image.class, ImageCellRenderer.class);

		p.setProperties(new Property[] { defaultProperty });

		p.setEditorFactory(pr);
		p.setRendererFactory(propertyRendererRegistry);

		f.getContentPane().add(p, BorderLayout.CENTER);
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

}
