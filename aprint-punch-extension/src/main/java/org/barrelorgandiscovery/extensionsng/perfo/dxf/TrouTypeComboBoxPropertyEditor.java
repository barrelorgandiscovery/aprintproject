package org.barrelorgandiscovery.extensionsng.perfo.dxf;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;

public class TrouTypeComboBoxPropertyEditor extends ComboBoxPropertyEditor {

	public TrouTypeComboBoxPropertyEditor() {
		super();
		setAvailableValues(TrouType.values());
	}

}
