package org.barrelorgandiscovery.gui.etl.steps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;

import org.barrelorgandiscovery.gui.etl.JConfigurePanel;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.tools.BeanAsk;
import org.barrelorgandiscovery.tools.ImageCellRenderer;
import org.barrelorgandiscovery.tools.InstrumentChooserPropertyEditor;
import org.barrelorgandiscovery.tools.ScaleChooserPropertyEditor;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFilePropertyEditor;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Panneau de configuration par défaut, permettant la saisie des paramètres en
 * utilisant les propriétés Property lnf
 * 
 * @author pfreydiere
 * 
 */
public class JDefaultConfigurePanel extends JConfigurePanel {

	protected PropertySheetPanel propertySheetPanel;
	protected ModelStep ms;

	public JDefaultConfigurePanel(ModelStep ms, JConfigurePanelEnvironment env) {
		super();
		assert ms != null;
		this.ms = ms;

		Property[] inputs = convertParameters(ms.getConfigureParametersByRef());

		propertySheetPanel = new PropertySheetPanel();
		propertySheetPanel.setDescriptionVisible(true);
		propertySheetPanel.setSortingCategories(true);

		
		PropertyRendererRegistry prr = new PropertyRendererRegistry();
		prr.registerDefaults();
		prr.registerRenderer(Image.class, ImageCellRenderer.class);
		
		PropertyEditorRegistry pr = new PropertyEditorRegistry();
		pr.registerDefaults();
		pr.registerEditor(MidiFile.class, MidiFilePropertyEditor.class);

		pr.registerEditor(Scale.class,
				new ScaleChooserPropertyEditor(env.getRepository()));

		pr.registerEditor(Instrument.class,
				new InstrumentChooserPropertyEditor(env.getRepository()));

		
		propertySheetPanel.setEditorFactory(pr);
		propertySheetPanel.setRendererFactory(prr);

		propertySheetPanel.setProperties(inputs);

		setLayout(new BorderLayout());
		add(propertySheetPanel, BorderLayout.CENTER);

	}

	@Override
	public boolean apply() throws Exception {

		Property[] properties2 = propertySheetPanel.getProperties();
		putValues(properties2, ms.getConfigureParametersByRef());

		return true;
	}

	/**
	 * Cree un tableau de property associé aux paramètres de configuration
	 * 
	 * @param ms
	 * @return
	 */
	protected Property[] createConfigProperties(ModelStep ms) {
	
		ModelValuedParameter[] configureParametersByRef = ms
				.getConfigureParametersByRef();
	
		if (configureParametersByRef == null)
			return new Property[0];
	
		ArrayList<Property> ret = new ArrayList<Property>();
		for (int i = 0; i < configureParametersByRef.length; i++) {
			ModelValuedParameter modelValuedParameter = configureParametersByRef[i];
			ret.add(convertParameterIntoProperty(modelValuedParameter));
		}
	
		return ret.toArray(new Property[0]);
	
	}

}
