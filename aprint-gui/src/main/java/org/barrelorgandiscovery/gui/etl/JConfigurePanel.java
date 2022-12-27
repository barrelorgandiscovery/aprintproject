package org.barrelorgandiscovery.gui.etl;

import java.awt.LayoutManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ModelValuedParameter;
import org.barrelorgandiscovery.model.type.JavaType;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;

/**
 * Panneau contenant une ihm de saisie de la configuration,
 * 
 * @author pfreydiere
 * 
 */
public abstract class JConfigurePanel extends JPanel {

	public JConfigurePanel() {
		super();
	}

	public JConfigurePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public JConfigurePanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public JConfigurePanel(LayoutManager layout) {
		super(layout);
	}

	/**
	 * Cette methode applique de facon transactionnelle la configuration sur les
	 * parametres de configuration, si les parametres sont incorrects une
	 * exception est levee et la valeur des parametres ne doit pas etre prise en
	 * compte
	 * 
	 * @throws Exception
	 */
	public abstract boolean apply() throws Exception;

	/**
	 * Convert parameter into property element
	 * 
	 * @param p
	 * @return
	 */
	protected Property convertParameterIntoProperty(AbstractParameter p) {
		DefaultProperty dp = new DefaultProperty();
		dp.setName(p.getName());
	
		if (p.getType() instanceof JavaType) {
			JavaType jt = (JavaType) p.getType();
			dp.setType(jt.getTargetedJavaType());
		}
	
		dp.setDisplayName(p.getLabel());
		if (p instanceof ModelValuedParameter) {
			ModelValuedParameter vp = (ModelValuedParameter) p;
			dp.setValue(vp.getValue());
		}
	
		return dp;
	}

	/**
	 * Convert parameters into properties
	 * 
	 * @param p
	 * @return
	 */
	protected Property[] convertParameters(AbstractParameter[] p) {
		ArrayList<Property> ret = new ArrayList<Property>();
		for (int i = 0; i < p.length; i++) {
			AbstractParameter abstractParameter = p[i];
			ret.add(convertParameterIntoProperty(abstractParameter));
		}
		return ret.toArray(new Property[0]);
	}

	protected void putValues(Property[] properties,
			ModelValuedParameter[] parameters) {
	
		if (properties == null || parameters == null)
			return;
	
		HashMap<String, Serializable> v = new HashMap<String, Serializable>();
		for (int i = 0; i < properties.length; i++) {
			Property p = properties[i];
			v.put(p.getName(), (Serializable) p.getValue());
		}
	
		for (int i = 0; i < parameters.length; i++) {
			ModelValuedParameter modelValuedParameter = parameters[i];
			String name = modelValuedParameter.getName();
			Serializable value = null;
			if (v.containsKey(name)) {
				value = v.get(name);
	
			} else {
				value = null;
			}
			modelValuedParameter.setValue(value);
		}
	
	}

}
