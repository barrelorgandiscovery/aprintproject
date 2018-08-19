package org.barrelorgandiscovery.tracetools.punch;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.TrouTypeComboBoxPropertyEditor;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.DoublePropertyEditor;
import com.l2fprod.common.beans.editor.IntegerPropertyEditor;

public class PunchConverterOptimizerParametersBeanInfo extends BaseBeanInfo {

	public PunchConverterOptimizerParametersBeanInfo() {
		super(PunchConverterOptimizerParameters.class);

		addProperties(this);

	}
	

	public static void addProperties(BaseBeanInfo bi) {
		ExtendedPropertyDescriptor notPunchedIfLessThan = bi
				.addProperty("notPunchedIfLessThan"); //$NON-NLS-1$
		notPunchedIfLessThan
				.setShortDescription(Messages.getString("PunchConverterOptimizerParametersBeanInfo.1")); //$NON-NLS-1$
		notPunchedIfLessThan
				.setDisplayName(Messages.getString("PunchConverterOptimizerParametersBeanInfo.2")); //$NON-NLS-1$
		notPunchedIfLessThan.setPropertyEditorClass(DoublePropertyEditor.class);
		notPunchedIfLessThan.setCategory(Messages.getString("PunchConverterOptimizerParametersBeanInfo.3")); //$NON-NLS-1$

		ExtendedPropertyDescriptor overlap = bi.addProperty("overlap"); //$NON-NLS-1$
		overlap.setShortDescription(Messages.getString("PunchConverterOptimizerParametersBeanInfo.5")); //$NON-NLS-1$
		overlap.setDisplayName(Messages.getString("PunchConverterOptimizerParametersBeanInfo.6")); //$NON-NLS-1$
		overlap.setPropertyEditorClass(DoublePropertyEditor.class);
		overlap.setCategory(Messages.getString("PunchConverterOptimizerParametersBeanInfo.7")); //$NON-NLS-1$

//		ExtendedPropertyDescriptor typeTrous = bi.addProperty("typeTrous");
//		typeTrous.setShortDescription("Type de trous");
//		typeTrous.setDisplayName("Types de trous");
//		typeTrous.setPropertyEditorClass(TrouTypeComboBoxPropertyEditor.class);
//		typeTrous.setCategory("Trous");

		ExtendedPropertyDescriptor taillTrous = bi.addProperty("punchWidth"); //$NON-NLS-1$
		taillTrous.setShortDescription(Messages.getString("PunchConverterOptimizerParametersBeanInfo.9")); //$NON-NLS-1$
		taillTrous.setDisplayName(Messages.getString("PunchConverterOptimizerParametersBeanInfo.10")); //$NON-NLS-1$
		taillTrous.setPropertyEditorClass(DoublePropertyEditor.class);
		taillTrous.setCategory(Messages.getString("PunchConverterOptimizerParametersBeanInfo.11")); //$NON-NLS-1$

//		ExtendedPropertyDescriptor dimensionPonts = bi.addProperty("pont");
//		dimensionPonts.setShortDescription("Dimension des ponts (mm)");
//		dimensionPonts.setPropertyEditorClass(DoublePropertyEditor.class);
//		dimensionPonts.setDisplayName("Dimension des ponts (mm)");
//		dimensionPonts.setCategory("Trous");

	}

}
