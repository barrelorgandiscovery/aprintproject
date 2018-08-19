package org.barrelorgandiscovery.extensionsng.perfo.dxf;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;

public class TypePliuresComboBoxPropertyEditor extends ComboBoxPropertyEditor {

	public TypePliuresComboBoxPropertyEditor() {
		super();
		setAvailableValues(TypePliure.values());
	}

}
