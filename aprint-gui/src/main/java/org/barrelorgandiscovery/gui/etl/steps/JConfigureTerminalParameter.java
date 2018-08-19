package org.barrelorgandiscovery.gui.etl.steps;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serializable;

import org.barrelorgandiscovery.gui.etl.JConfigurePanel;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelType;
import org.barrelorgandiscovery.model.TerminalParameterModelStep;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.ImageCellRenderer;
import org.barrelorgandiscovery.tools.ScaleChooserPropertyEditor;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFilePropertyEditor;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Panneau de configuration des constantes d'entrée ou de sortie
 * 
 * @author pfreydiere
 * 
 */
public class JConfigureTerminalParameter extends JConfigurePanel {

	private Object[] TYPE_COMBO_CHOICE = new Object[] { new JavaType(String.class), 
			new JavaType(Integer.class), 
			new JavaType(Boolean.class),
			new JavaType(Scale.class), 
			new JavaType(VirtualBook.class), 
			new JavaType(File.class) };

	protected TerminalParameterModelStep tpms;
	protected PropertySheetPanel propertySheetPanel;
	private DefaultProperty valueProperty;
	private DefaultProperty inoutProperty;
	private DefaultProperty typeProperty;
	private DefaultProperty nameProperty;

	public JConfigureTerminalParameter(TerminalParameterModelStep tpms, JConfigurePanelEnvironment env) {
		assert tpms != null;
		this.tpms = tpms;

		// construction des propriétés à partir des paramètres
		propertySheetPanel = new PropertySheetPanel();
		propertySheetPanel.setDescriptionVisible(true);
		propertySheetPanel.setSortingCategories(true);

		PropertyRendererRegistry prr = new PropertyRendererRegistry();
		prr.registerDefaults();
		prr.registerRenderer(Image.class, ImageCellRenderer.class);

		propertySheetPanel.setRendererFactory(prr);

		// préparation des propriétés

		nameProperty = new DefaultProperty();
		nameProperty.setDisplayName(Messages.getString("JConfigureTerminalParameter.0")); //$NON-NLS-1$
		nameProperty.setShortDescription(Messages.getString("JConfigureTerminalParameter.1")); //$NON-NLS-1$
		nameProperty.setEditable(true);
		nameProperty.setType(String.class);
		nameProperty.setName("name"); //$NON-NLS-1$
		nameProperty.setValue(tpms.getName());

		typeProperty = new DefaultProperty();
		typeProperty.setDisplayName(Messages.getString("JConfigureTerminalParameter.3")); //$NON-NLS-1$
		typeProperty.setShortDescription(Messages.getString("JConfigureTerminalParameter.4")); //$NON-NLS-1$
		typeProperty.setEditable(true);
		typeProperty.setType(ModelType.class);
		typeProperty.setName("type"); //$NON-NLS-1$
		
		
		
		
	
		inoutProperty = new DefaultProperty();
		inoutProperty.setDisplayName(Messages.getString("JConfigureTerminalParameter.6")); //$NON-NLS-1$
		inoutProperty.setShortDescription(Messages.getString("JConfigureTerminalParameter.7")); //$NON-NLS-1$
		inoutProperty.setEditable(true);
		inoutProperty.setType(Boolean.class);
		inoutProperty.setName("inout"); //$NON-NLS-1$
		inoutProperty.setValue(tpms.isInput());

		valueProperty = new DefaultProperty();
		valueProperty.setDisplayName(Messages.getString("JConfigureTerminalParameter.9")); //$NON-NLS-1$
		valueProperty.setShortDescription(Messages.getString("JConfigureTerminalParameter.10")); //$NON-NLS-1$
		valueProperty.setEditable(true);
		valueProperty.setType(String.class);
		valueProperty.setName("value"); //$NON-NLS-1$
		
		propertySheetPanel.setProperties(new Property[] { nameProperty, typeProperty, inoutProperty, valueProperty });

		typeProperty.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt == null) { return; }
				if ("value".equals(evt.getPropertyName())) { //$NON-NLS-1$
					// adaptation du type de la propriété ..
					propertySheetPanel.removeProperty(valueProperty);
					JavaType newType = (JavaType) evt.getNewValue();
					valueProperty.setType(newType.getTargetedJavaType());
					valueProperty.setValue(null); // reset value
					propertySheetPanel.addProperty(valueProperty);
				}
			}
		});
	
		// beware translated type
		typeProperty.setValue(tpms.getModelType());
		valueProperty.setValue(tpms.getValue());


		// editors

		ComboBoxPropertyEditor typeComboEditor = new ComboBoxPropertyEditor();
		typeComboEditor.setAvailableValues(
				TYPE_COMBO_CHOICE);

		PropertyEditorRegistry pr = new PropertyEditorRegistry();
		pr.registerDefaults();
		pr.registerEditor(MidiFile.class, MidiFilePropertyEditor.class);
		pr.registerEditor(Scale.class, 
				new ScaleChooserPropertyEditor(env.getRepository()));
		pr.registerEditor(typeProperty, typeComboEditor);

		propertySheetPanel.setEditorFactory(pr);

		setLayout(new BorderLayout());
		add(propertySheetPanel, BorderLayout.CENTER);

	}

	@Override
	public boolean apply() throws Exception {

		tpms.setName((String) nameProperty.getValue());
		tpms.setInput((Boolean) inoutProperty.getValue());
		tpms.setType((ModelType) typeProperty.getValue());
		tpms.setValue((Serializable) valueProperty.getValue());

		return true;
	}

}
