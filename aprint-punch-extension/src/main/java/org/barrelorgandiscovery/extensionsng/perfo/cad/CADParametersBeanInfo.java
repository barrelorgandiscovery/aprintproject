package org.barrelorgandiscovery.extensionsng.perfo.cad;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.DoublePropertyEditor;
import com.l2fprod.common.beans.editor.IntegerPropertyEditor;

/**
 * cad parameters shown in the parameter panel
 * 
 * @author pfreydiere
 * 
 */
public class CADParametersBeanInfo extends BaseBeanInfo {

	public CADParametersBeanInfo() {
		super(CADParameters.class);

		ExtendedPropertyDescriptor startBookAdjustementFromBeginning = addProperty("startBookAdjustementFromBeginning");
		startBookAdjustementFromBeginning
				.setShortDescription("D�finition de la marge entre le premier trou du carton et le debut, en mm");
		startBookAdjustementFromBeginning.setDisplayName("Marge de debut du carton (mm)");
		startBookAdjustementFromBeginning.setPropertyEditorClass(DoublePropertyEditor.class);
		startBookAdjustementFromBeginning.setCategory("Carton");

		ExtendedPropertyDescriptor nombreDePlisAAjouterAuDebut = addProperty("nombreDePlisAAjouterAuDebut");
		nombreDePlisAAjouterAuDebut
				.setShortDescription("Plis suppl�mentaires � ajouter avant le debut du carton (nombre de plis)");
		nombreDePlisAAjouterAuDebut.setDisplayName("Nombre de plis suppl�mentaires de debut de carton");
		nombreDePlisAAjouterAuDebut.setPropertyEditorClass(IntegerPropertyEditor.class);
		nombreDePlisAAjouterAuDebut.setCategory("Carton");

		ExtendedPropertyDescriptor nombreDePlisAAjouterFin = addProperty("nombreDePlisAAjouterFin");
		nombreDePlisAAjouterFin
				.setShortDescription("Plis suppl�mentaires � ajouter en fin de carton (nombre de plis)");
		nombreDePlisAAjouterFin.setDisplayName("Nombre de plis suppl�mentaires en fin de carton");
		nombreDePlisAAjouterFin.setPropertyEditorClass(IntegerPropertyEditor.class);
		nombreDePlisAAjouterFin.setCategory("Carton");
		
		ExtendedPropertyDescriptor typeTrous = addProperty("typeTrous");
		typeTrous.setShortDescription("Type de trous");
		typeTrous.setDisplayName("Types de trous");
		typeTrous.setPropertyEditorClass(TrouTypeComboBoxPropertyEditor.class);
		typeTrous.setCategory("Trous");

		ExtendedPropertyDescriptor taillTrous = addProperty("tailleTrous");
		taillTrous.setShortDescription(
				"Taille Maximum des trous cr��s, si le trou est plus grand, un pont est cr��.\nSi vous ne souhaitez pas de ponts, mettez un grand nombre ici");
		taillTrous.setDisplayName("Taille Maximum des d�coupes (mm)");
		taillTrous.setPropertyEditorClass(DoublePropertyEditor.class);
		taillTrous.setCategory("Trous");

		ExtendedPropertyDescriptor dimensionPonts = addProperty("pont");
		dimensionPonts.setShortDescription("Dimension des ponts (mm)");
		dimensionPonts.setPropertyEditorClass(DoublePropertyEditor.class);
		dimensionPonts.setDisplayName("Dimension des ponts (mm)");
		dimensionPonts.setCategory("Trous");

//		ExtendedPropertyDescriptor typePont = addProperty("typePonts");
//		typePont.setShortDescription("Forme des ponts");
//		typePont.setPropertyEditorClass(TrouTypeComboBoxPropertyEditor.class);
//		typePont.setDisplayName("Forme des ponts");
//		typePont.setCategory("Trous");

		ExtendedPropertyDescriptor silreste = addProperty("pasDePontSiIlReste");
		silreste.setShortDescription(
				"On abandonne le dernier pont s'il reste un petite distance int�rieure � la valeure saisie (en mm)");
		silreste.setPropertyEditorClass(DoublePropertyEditor.class);
		silreste.setDisplayName("Pas de pont s'il reste (mm)");
		silreste.setCategory("Trous");
		

		ExtendedPropertyDescriptor surchargeLargeurTrous = addProperty("surchargeLargeurTrous");
		surchargeLargeurTrous.setShortDescription(
				"Utiliser le param�tre Largeur Trous, ici au lieu de l'utilisation des m�triques dans la gamme");
		surchargeLargeurTrous.setDisplayName("Surcharge de largeur des trous (activation)");
		surchargeLargeurTrous.setCategory("Trous");
		
		ExtendedPropertyDescriptor hauteurDesTrous = addProperty("largeurTrous");
		hauteurDesTrous.setShortDescription(
				"Largeur des trous sp�cifi�e si la surcharge par rapport � la gamme est activ�e");
		hauteurDesTrous.setDisplayName("Surcharge de largeur des trous (mm)");
		hauteurDesTrous.setPropertyEditorClass(DoublePropertyEditor.class);
		hauteurDesTrous.setCategory("Trous");
		

		ExtendedPropertyDescriptor exportPliures = addProperty("exportPliures");
		exportPliures.setShortDescription("Export des traits de pliures dans le fichier DXF");
		// taillePage.setPropertyEditorClass(Check.class);
		exportPliures.setDisplayName("Export Pliures");
		exportPliures.setCategory("Pliures");

		ExtendedPropertyDescriptor taillePage = addProperty("taillePagePourPliure");
		taillePage.setShortDescription(
				"Si les pliures sont s�lectionn�es, Taille de la page du carton pour r�aliser les pliures en mm");
		taillePage.setPropertyEditorClass(DoublePropertyEditor.class);
		taillePage.setDisplayName("Taille page carton (mm)");
		taillePage.setCategory("Pliures");

		ExtendedPropertyDescriptor typePliure = addProperty("typePliure");
		typePliure.setShortDescription("Dessin des pliures (continue, ou pointill�)");
		typePliure.setPropertyEditorClass(TypePliuresComboBoxPropertyEditor.class);
		typePliure.setDisplayName("Type de pliures");
		typePliure.setCategory("Pliures");

		ExtendedPropertyDescriptor exportDecoupeDesBords = addProperty("exportDecoupeDesBords");
		exportDecoupeDesBords.setShortDescription("Export de la d�coupe des bords");
		exportDecoupeDesBords.setDisplayName("Export Bords du carton");
		exportDecoupeDesBords.setCategory("Export");

	}

}
