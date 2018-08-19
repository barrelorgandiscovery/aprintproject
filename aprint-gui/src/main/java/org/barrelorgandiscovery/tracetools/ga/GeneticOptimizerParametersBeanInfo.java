package org.barrelorgandiscovery.tracetools.ga;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tracetools.punch.PunchConverterOptimizerParametersBeanInfo;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.DoublePropertyEditor;

public class GeneticOptimizerParametersBeanInfo extends BaseBeanInfo {

	public GeneticOptimizerParametersBeanInfo() {
		super(GeneticOptimizerParameters.class);

		PunchConverterOptimizerParametersBeanInfo.addProperties(this);

		ExtendedPropertyDescriptor notPunchedIfLessThan = addProperty("pageSize"); //$NON-NLS-1$
		notPunchedIfLessThan
				.setShortDescription("Taille de page, pour l'optimisation, définit le retour toléré dans l'optimisation");
		notPunchedIfLessThan
				.setDisplayName("Taille de page"); 
		notPunchedIfLessThan.setPropertyEditorClass(DoublePropertyEditor.class);
		notPunchedIfLessThan.setCategory("Optimisation");

		
	}

}
