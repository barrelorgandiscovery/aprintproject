package org.barrelorgandiscovery.gui.aprintng;

import org.barrelorgandiscovery.tools.DateStringConverterPropertyEditor;
import org.barrelorgandiscovery.tools.ImageCellRenderer;
import org.barrelorgandiscovery.tools.ImagePropertyEditor;

import org.barrelorgandiscovery.virtualbook.VirtualBookMetadata;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.StringPropertyEditor;

/**
 * Bean info for properties editing on virtual book metadata
 * 
 * @author pfreydiere
 * 
 */
public class VirtualBookMetadataBeanInfo extends BaseBeanInfo {

	public VirtualBookMetadataBeanInfo() {
		super(VirtualBookMetadata.class);

		ExtendedPropertyDescriptor name = addProperty("name");
		name.setDisplayName("Nom du carton");
		name.setShortDescription("Nom long du carton, indépendamment du nom de fichier");
		// creationDate.setPropertyEditorClass(JCalendarDatePropertyEditor.class);
		name.setCategory("General");

		ExtendedPropertyDescriptor creationDate = addProperty("creationDate");
		creationDate.setDisplayName("Date de création");
		creationDate.setShortDescription("Date de création du morceau");
		creationDate
				.setPropertyEditorClass(DateStringConverterPropertyEditor.class);
		creationDate.setCategory("General");

		ExtendedPropertyDescriptor description = addProperty("description");
		description.setShortDescription("Description de la musique");
		description.setDisplayName("Description");
		description.setPropertyEditorClass(StringPropertyEditor.class);
		description.setCategory("General");

		ExtendedPropertyDescriptor arranger = addProperty("arranger");
		arranger.setShortDescription("Arrangeur");
		arranger.setDisplayName("Arrangeur");
		arranger.setCategory("Arrangement");

		ExtendedPropertyDescriptor auteur = addProperty("author");
		auteur.setShortDescription("Auteur(s) de la musique");
		auteur.setDisplayName("Auteur");
		auteur.setCategory("Musique");

		ExtendedPropertyDescriptor genre = addProperty("genre");
		genre.setShortDescription("Genre de la musique");
		genre.setDisplayName("Genre");
		genre.setCategory("Musique");

		ExtendedPropertyDescriptor cover = addProperty("cover");
		cover.setShortDescription("Couverture du carton");
		cover.setDisplayName("Couverture");
		cover.setCategory("Image");
		cover.setPropertyEditorClass(ImagePropertyEditor.class);
		cover.setPropertyTableRendererClass(ImageCellRenderer.class);

	}

}
