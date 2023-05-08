package org.barrelorgandiscovery.model.steps.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.etl.JConfigurePanel;
import org.barrelorgandiscovery.gui.etl.steps.JConfigurePanelEnvironment;
import org.barrelorgandiscovery.gui.etl.steps.JDefaultConfigurePanel;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ContextVariables;
import org.barrelorgandiscovery.model.IModelStepContextAware;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelType;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.TerminalParameterModelStep;
import org.barrelorgandiscovery.model.type.CompositeType;
import org.barrelorgandiscovery.model.type.GenericSimpleType;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.tools.InstrumentNameChooserPropertyEditor;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Terminal for display a book at the end
 *
 * @author pfreydiere
 */
public class NewBookFrame extends TerminalParameterModelStep implements IModelStepContextAware {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3294758481433232042L;

	private static Logger logger = Logger.getLogger(NewBookFrame.class);

	private APrintNGGeneralServices services;
	private String instrumentName;
	private ModelValuedParameter instrumentParameter;

	public NewBookFrame() throws Exception {
		super(false,
				new CompositeType(
						new ModelType[] { new JavaType(VirtualBook.class),
								new GenericSimpleType(Collection.class, new Class[] { Hole.class }) },
						"virtualBook", Messages.getString("NewBookFrame.1")), //$NON-NLS-1$ //$NON-NLS-2$
				"virtualbook", Messages.getString("NewBookFrame.3"), null); //$NON-NLS-1$ //$NON-NLS-2$
		updateConfig();
		// fromConfig();
	}

	@Override
	public String getLabel() {
		return Messages.getString("NewBookFrame.4"); //$NON-NLS-1$
	}

	@Override
	public void defineContext(Map<String, Object> context) {

		if (context == null)
			return;

		super.defineContext(context);

		Object p = context.get(ContextVariables.CONTEXT_SERVICES);
		if (p != null && p instanceof APrintNGGeneralServices) {
			services = (APrintNGGeneralServices) p;
		}
	}

	@Override
	public ModelParameter[] getOutputParametersByRef() {
		return new ModelParameter[] {};
	}

	/**
	 * this method is call if present, for the moment, due to dependencies, there
	 * are no links between UI and core. @see 
	 * 
	 * @param env
	 * @return
	 */
	public JConfigurePanel getUIToConfigureStep(JConfigurePanelEnvironment env) {
		
		JDefaultConfigurePanel confPanel = new JDefaultConfigurePanel(this, env);
		PropertySheetPanel p = confPanel.getPropertySheetPanel();

		PropertyEditorRegistry ef = (PropertyEditorRegistry) p.getEditorFactory();

		Property[] properties = p.getProperties();
		logger.debug("properties :" + Arrays.asList(properties)); //$NON-NLS-1$
		assert properties.length == 1;

		ef.registerEditor(properties[0], new InstrumentNameChooserPropertyEditor(env.getRepository()));

		return confPanel;
	}

	@Override
	public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> values) throws Exception {
		Map<AbstractParameter, Object> result = super.execute(values);

		if (services != null && getValue() != null) {

			logger.debug("open the virtual book"); //$NON-NLS-1$

			Collection<Hole> h = null;
			if (getValue() instanceof VirtualBook) {
				h = ((VirtualBook) getValue()).getHolesCopy();
			} else {
				h = (Collection<Hole>) getValue();
			}
			assert h != null;

			logger.debug("get the instrument"); //$NON-NLS-1$
			log("get instrument :" + instrumentName); //$NON-NLS-1$
			Instrument instrument = services.getRepository().getInstrument(instrumentName);

			logger.debug("instrument loaded :" + instrumentName); //$NON-NLS-1$
			if (instrument == null)
				throw new Exception("instrument " + instrumentName + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("opening virtual book"); //$NON-NLS-1$

			VirtualBook vb = new VirtualBook(instrument.getScale());
			vb.addHole(h);
			logger.debug("open frame"); //$NON-NLS-1$
			APrintNGVirtualBookFrame frame = services.newVirtualBook(vb, instrument);

		} else {
			logger.info("context variable services not defined, or null value"); //$NON-NLS-1$
		}

		return result;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	@Override
	public void applyConfig() throws Exception {
		// super.applyConfigOnParameters();

		fromConfig();
	}

	public void updateConfig() {

		instrumentParameter = new ModelValuedParameter();
		instrumentParameter.setName("instrumentname"); //$NON-NLS-1$
		instrumentParameter.setLabel(Messages.getString("NewBookFrame.16")); //$NON-NLS-1$
		instrumentParameter.setType(new JavaType(String.class));
		instrumentParameter.setValue(instrumentName);
		instrumentParameter.setStep(this);

		configureParameters = new ModelValuedParameter[] { instrumentParameter };
	}

	private void fromConfig() throws Exception {
		this.instrumentName = (String) instrumentParameter.getValue();
	}

	@Override
	protected ParameterError[] validateConfigValues() {
		assert instrumentParameter != null;

		logger.debug("validate config values"); //$NON-NLS-1$

		ParameterError[] validateConfigValues = super.validateConfigValues();
		if (validateConfigValues != null && validateConfigValues.length > 0) {
			return validateConfigValues;
		}

		if (configureParameters == null || instrumentParameter.getValue() == null
				|| !(instrumentParameter.getValue() instanceof String)
				|| (((String) instrumentParameter.getValue()).trim().equals(""))) { //$NON-NLS-1$
			return new ParameterError[] { new ParameterError(instrumentParameter, "no instrument selected") }; //$NON-NLS-1$
		}
		return new ParameterError[0];
	}
}
