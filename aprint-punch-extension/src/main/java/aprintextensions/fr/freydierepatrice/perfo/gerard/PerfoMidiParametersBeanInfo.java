package aprintextensions.fr.freydierepatrice.perfo.gerard;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.BooleanAsCheckBoxPropertyEditor;
import com.l2fprod.common.beans.editor.DoublePropertyEditor;

public class PerfoMidiParametersBeanInfo extends BaseBeanInfo {

	public PerfoMidiParametersBeanInfo() {
		super(PerfoMidiParameters.class);

		ExtendedPropertyDescriptor decalageproperty = addProperty("decalage");
		decalageproperty
				.setShortDescription("Demi tons de d�calage, si positif");
		decalageproperty.setDisplayName("Demi tons � ajouter");
		decalageproperty.setCategory("Transposition");

		ExtendedPropertyDescriptor minholesize = addProperty("minholesize");
		minholesize.setShortDescription("Taille minimum des trous");
		minholesize
				.setDisplayName("Taille Minimum des trous (pour agrandissement)");
		minholesize.setPropertyEditorClass(DoublePropertyEditor.class);
		minholesize
				.setCategory("Poin�on");

		ExtendedPropertyDescriptor preserveInterHoles = addProperty("preserveInterHoles");
		preserveInterHoles
				.setShortDescription("Impose les bride, quitte � r�duire la taille du trou pr�c�dent");
		preserveInterHoles
				.setPropertyEditorClass(BooleanAsCheckBoxPropertyEditor.class);
		preserveInterHoles.setDisplayName("Preserve les brides");
		preserveInterHoles.setCategory("Brides");

		ExtendedPropertyDescriptor mininterholesize = addProperty("mininterholesize");
		mininterholesize.setDisplayName("Taille des brides (mm)");
		mininterholesize
				.setShortDescription("Taille minimum des bride (en mm)");
		mininterholesize.setPropertyEditorClass(DoublePropertyEditor.class);
		mininterholesize.setCategory("Brides");

	}

}
