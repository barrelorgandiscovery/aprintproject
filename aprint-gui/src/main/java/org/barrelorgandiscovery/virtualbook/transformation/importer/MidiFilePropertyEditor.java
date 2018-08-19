package org.barrelorgandiscovery.virtualbook.transformation.importer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFrame;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.beans.editor.FilePropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * MidiFile Property Editor , work on a MidiFileWithAssociatedFile class type
 * 
 * @author Freydiere Patrice
 * 
 */
public class MidiFilePropertyEditor extends AbstractPropertyEditor {

	private static Logger logger = Logger
			.getLogger(MidiFilePropertyEditor.class);

	private FilePropertyEditor innerPropertyEditor = new FilePropertyEditor();

	private MidiFile value = null;

	public MidiFilePropertyEditor() {
		logger.debug("instanciate MidiFilePropertyEditor");

		innerPropertyEditor
				.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {

						File newFile = (File) evt.getNewValue();

						MidiFileWithAssociatedFile newValue = null;
						try {
							MidiFile n = MidiFileIO.read(newFile);
							newValue = new MidiFileWithAssociatedFile(n,
									newFile);

						} catch (Exception ex) {
							logger.error("error in setting the value ... "
									+ ex.getMessage(), ex);
							throw new IllegalArgumentException(ex.getMessage(),
									ex);
						}

						MidiFile old = value;

						value = newValue;

						firePropertyChange(old, value);

					}
				});

	}

	@Override
	public String getJavaInitializationString() {
		return innerPropertyEditor.getJavaInitializationString();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		logger.debug("setAsText :" + text);
		innerPropertyEditor.setAsText(text);
	}

	@Override
	public Component getCustomEditor() {
		return innerPropertyEditor.getCustomEditor();
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = (MidiFile) value;
		if (this.value != null)

		{
			if (this.value instanceof MidiFileWithAssociatedFile) {
				innerPropertyEditor
						.setValue(((MidiFileWithAssociatedFile) this.value)
								.getAssociatedFile());
			} else {
				innerPropertyEditor.setValue(null);
			}
		} else {
			innerPropertyEditor.setValue(null);
		}
	}

	public static void main(String[] args) {

		BasicConfigurator.configure(new LF5Appender());

		JFrame f = new JFrame();
		PropertySheetPanel p = new PropertySheetPanel();
		p.setDescriptionVisible(true);
		p.setSortingCategories(true);

		DefaultProperty defaultProperty = new DefaultProperty();
		defaultProperty.setType(MidiFile.class);
		defaultProperty.setEditable(true);
		defaultProperty.setDisplayName("File ... ");

		PropertyEditorRegistry pr = new PropertyEditorRegistry();
		// pr.registerDefaults();
		pr.registerEditor(MidiFile.class, MidiFilePropertyEditor.class);

		p.setProperties(new Property[] { defaultProperty });
		p.setEditorFactory(pr);

		f.getContentPane().add(p, BorderLayout.CENTER);
		f.setSize(800, 600);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

	}

}
